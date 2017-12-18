package ru.nev.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nev.chat.AbstractChatComponent;
import ru.nev.chat.converter.MessageConverter;
import ru.nev.chat.converter.OutputMessage;
import ru.nev.chat.messages.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.LinkedList;

import static java.nio.channels.SelectionKey.*;

public class ChatClient extends AbstractChatComponent {

  private final Logger logger = LoggerFactory.getLogger(ChatClient.class);

  private final LinkedList<Message> messages;
  private final String host;
  private final int port;
  private final ChatResponseHandler handler;

  public ChatClient(String host, int port, MessageConverter<Message> converter, ChatResponseHandler handler) throws IOException {
    super(new InetSocketAddress(host, port), converter);
    this.host = host;
    this.port = port;
    this.handler = handler;
    this.messages = new LinkedList<>();
  }

  @Override
  protected AbstractSelectableChannel channel(InetSocketAddress address) throws IOException {
    SocketChannel channel = SocketChannel.open();
    channel.configureBlocking(false);
    channel.connect(address);
    return channel;
  }

  @Override
  protected Selector selector() throws IOException {
    Selector selector = Selector.open();
    channel.register(selector, OP_CONNECT);
    return selector;
  }

  @Override
  protected void handleIncomingMessage(SelectionKey sender, Message message) throws IOException {
    logger.trace("{}", message);
    handler.onMessage(message);
  }

  @Override
  protected void write(SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel) key.channel();
    while (!messages.isEmpty()) {
      Message message = messages.poll();

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        OutputMessage outputMessage = () -> baos;
        converter.write(message, outputMessage);

        logger.trace("Write: " + message + ". Left " + messages.size());
        channel.write(ByteBuffer.wrap(baos.toByteArray()));
      }
    }
    key.interestOps(OP_READ);
  }

  @Override
  protected int select() throws IOException {
    int count = super.select();
    if (count == 0) {
//      logger.debug("Could not establish connection?");
//      stop();
    }
    return count;
  }

  protected void connect(SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel) key.channel();
    try {
      channel.finishConnect();
      channel.configureBlocking(false);
      channel.register(selector, SelectionKey.OP_WRITE);
    } catch (IOException e) {
      e.printStackTrace();
      key.channel().close();
      key.cancel();

      //is correct?
      stop();
    }
  }

  public void sendMessage(Message message) {
    messages.add(message);
    SelectionKey key = channel.keyFor(selector);
    key.interestOps(OP_WRITE);
  }

}
