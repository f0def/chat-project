package ru.nev.chat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nev.chat.AbstractChatComponent;
import ru.nev.chat.converter.MessageConverter;
import ru.nev.chat.converter.OutputMessage;
import ru.nev.chat.domain.ServerUser;
import ru.nev.chat.messages.*;
import ru.nev.chat.repository.MessageRepository;
import ru.nev.chat.repository.UserRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Objects;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class ChatServer extends AbstractChatComponent {

  private final Logger logger = LoggerFactory.getLogger(ChatServer.class);

  private final ByteBuffer welcomeBuf;
  private final UserRepository userRepository;
  private final MessageRepository messageRepository;

  public ChatServer(int port, MessageConverter<Message> converter,
                    UserRepository userRepository, MessageRepository messageRepository) throws IOException {
    super(port, converter);
    this.userRepository = userRepository;
    this.messageRepository = messageRepository;
    this.welcomeBuf = makeByteBuffer(new NotAuthenticatedMessage("Welcome to Chat! What is your name?"));
  }

  @Override
  public void run() {
    logger.debug("Server started at {} ", this.address);
    super.run();
  }

  @Override
  protected void accept(SelectionKey key) throws IOException {
    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
    SocketChannel socketChannel = serverSocketChannel.accept();

    ServerUser serverUser = new ServerUser(getAddress(socketChannel.socket()));

    socketChannel.configureBlocking(false);
    socketChannel.register(selector, OP_READ, serverUser);
    socketChannel.write(welcomeBuf);
    welcomeBuf.rewind();

    logger.debug("Accepted connection from: " + serverUser.getAddress());
  }

  @Override
  protected boolean read(SelectionKey key) throws IOException {
    boolean success = super.read(key);

    if (!success) {
      ServerUser serverUser = (ServerUser) key.attachment();
      logger.debug(key.attachment() + " left the chat");
      if (serverUser.getName() != null) {
        userRepository.remove(serverUser.getName());
      }
    }

    return success;
  }

  @Override
  protected AbstractSelectableChannel channel(InetSocketAddress address) throws IOException {
    ServerSocketChannel channel = ServerSocketChannel.open();
    channel.configureBlocking(false);
    channel.socket().bind(address);
    return channel;
  }

  @Override
  protected Selector selector() throws IOException {
    AbstractSelector selector = SelectorProvider.provider().openSelector();
    channel.register(selector, OP_ACCEPT);
    return selector;
  }

  @Override
  protected void handleIncomingMessage(SelectionKey sender, Message message) throws IOException {
    logger.trace("{} sent {}", sender.attachment(), message);

    processMessage(sender, message);
  }

  private void processMessage(SelectionKey sender, Message message) throws IOException {
    ServerUser serverUser = (ServerUser) sender.attachment();

    if (message instanceof TextMessage) {
      TextMessage textMessage = (TextMessage) message;

      if (textMessage.getUser() == null) {
        User user = new User(serverUser.getName());
        textMessage.setUser(user);
      }

      String text = textMessage.getText();
      if (serverUser.hasName()) {
        broadcastAsUser(text, sender);
        messageRepository.add(textMessage);
      } else {
        authenticate(sender, serverUser, text);
      }
    }
  }

  @Override
  protected void write(SelectionKey key) throws IOException {
    ByteBuffer buffer = (ByteBuffer) key.attachment();
    SocketChannel channel = (SocketChannel) key.channel();
    channel.write(buffer);
    key.interestOps(OP_READ);
  }

  private String getAddress(Socket socket) {
    return (new StringBuilder(socket.getInetAddress().toString())).append(":").append(socket.getPort()).toString();
  }

  private void authenticate(SelectionKey key, ServerUser serverUser, String name) throws IOException {
    if (userRepository.isUserNameExists(name)) {
      String msg = String.format("Name \"%s\" already exists, select a different name", name);
      sendToUser(key, new NotAuthenticatedMessage(msg));
    } else {
      userRepository.put(name, key);
      serverUser.setName(name);

      sendToUser(key, new NameChangedMessage(name));
      broadcast(new UserJoinedMessage(name), key);

      for (TextMessage tm : messageRepository.getLastMessages()) {
        sendToUser(key, tm);
      }
    }
  }

  private void broadcastAsUser(String text, SelectionKey key) throws IOException {
    ServerUser serverUser = (ServerUser) key.attachment();

    logger.debug("Public message to all as {}: {}", serverUser.getName(), text);

    User user = new User(serverUser.getName());
    broadcast(makeByteBuffer(new TextMessage(user, text)), key);
  }

  private void sendToUser(SelectionKey key, Message message) throws IOException {
    ServerUser serverUser = (ServerUser) key.attachment();

    logger.debug("Private message to {}: {}", serverUser, message.toChat());

    SocketChannel sc = (SocketChannel) key.channel();
    sc.write(makeByteBuffer(message));
  }

  private void broadcast(Message message, SelectionKey exceptKey) throws IOException {
    broadcast(makeByteBuffer(message), exceptKey);
  }

  private void broadcast(ByteBuffer buf, SelectionKey exceptKey) throws IOException {
    for (SelectionKey key : selector.keys()) {
      if (Objects.equals(key, exceptKey)) {
        continue;
      }

      send(buf, key);
    }
  }

  private void send(ByteBuffer buf, SelectionKey key) {
    if (key.isValid() && key.channel() instanceof SocketChannel && ((ServerUser) key.attachment()).hasName()) {
      SocketChannel sc = (SocketChannel) key.channel();
      try {
        sc.write(buf);
      } catch (IOException e) {
        translate(e);
      }
      buf.rewind();
    }
  }

  private ByteBuffer makeByteBuffer(Message msg) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    OutputMessage outputMessage = () -> byteArrayOutputStream;
    converter.write(msg, outputMessage);

    return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
  }

}
