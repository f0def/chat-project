package ru.nev.chat.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nev.chat.converter.MessageConverter;
import ru.nev.chat.converter.OutputMessage;
import ru.nev.chat.converter.TypeReference;
import ru.nev.chat.util.Utils;

import java.io.ByteArrayInputStream;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public abstract class AbstractSocketTransport<M> extends RunnableTransport<SelectionKey, M> {

  private final Logger logger = LoggerFactory.getLogger(AbstractSocketTransport.class);

  private final ByteBuffer buf;
  protected final Selector selector;
  protected final AbstractSelectableChannel channel;
  protected final InetSocketAddress address;
  protected final MessageConverter<M> converter;
  protected final List<TransportConsumer<SelectionKey, M>> consumers;
  protected final TypeReference<M> mTypeRef;

  public AbstractSocketTransport(String id, int port, MessageConverter<M> converter) throws IOException {
    this(id, new InetSocketAddress(port), converter);
  }

  public AbstractSocketTransport(String id, InetSocketAddress address, MessageConverter<M> converter)
    throws IOException {
    super(id);
    this.address = address;
    this.converter = converter;
    this.buf = ByteBuffer.allocate(256);
    this.channel = channel(address);
    this.selector = selector();
    this.consumers = new ArrayList<>();
    this.mTypeRef = new TypeReference<M>() {
    };
  }

  protected abstract AbstractSelectableChannel channel(InetSocketAddress address) throws IOException;

  protected abstract Selector selector() throws IOException;

  protected void handleIncomingMessage(SelectionKey sender, M message) throws IOException {
    for (TransportConsumer<SelectionKey, M> consumer : consumers) {
      consumer.read(sender, message);
    }
  }

  protected void handleException(SelectionKey sender, Exception e) {
    for (TransportConsumer<SelectionKey, M> consumer : consumers) {
      consumer.exception(sender, e);
    }
  }

  protected abstract void write(SelectionKey key) throws IOException;

  @Override
  public void addConsumer(TransportConsumer<SelectionKey, M> consumer) {
    consumers.add(consumer);
  }

  @Override
  public void write(SelectionKey receiver, M message) {
    SocketChannel socketChannel = (SocketChannel) receiver.channel();
    try {
      socketChannel.write(makeByteBuffer(message));
    } catch (IOException e) {
      handleException(receiver, e);
      translate(e);
    }
  }

  @Override
  public Object getKey(SelectionKey sender) {
    return sender.attachment();
  }

  protected ByteBuffer makeByteBuffer(M msg) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      OutputMessage outputMessage = () -> baos;
      converter.write(msg, outputMessage);

      return ByteBuffer.wrap(baos.toByteArray());
    }
  }

  protected ByteBuffer checkedMakeByteBuffer(M msg) {
    try {
      return makeByteBuffer(msg);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void cleanUp() {
    for (SelectionKey key : selector.keys()) {
      Utils.close(key.channel());
    }
    Utils.close(selector);
  }

  @Override
  protected void runMainLoop() {
    try {
      select();

      Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

      while (iterator.hasNext()) {
        SelectionKey key = iterator.next();
        iterator.remove();

        if (!key.isValid()) {
          continue;
        }

        if (key.isConnectable()) {
          connect(key);
        } else if (key.isAcceptable()) {
          accept(key);
        } else if (key.isReadable()) {
          read(key);
        } else if (key.isWritable()) {
          write(key);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected int select() throws IOException {
    return selector.select(1000);
  }

  protected void accept(SelectionKey sender) throws IOException {
    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) sender.channel();
    SocketChannel socketChannel = serverSocketChannel.accept();
    socketChannel.configureBlocking(false);

    String address = getAddress(socketChannel.socket());
    socketChannel.register(selector, OP_READ, address);

    logger.debug("Accepted connection from: " + address);

    for (TransportConsumer<SelectionKey, M> consumer : consumers) {
      M message = consumer.accept(sender, address);
      if (message != null) {
        socketChannel.write(makeByteBuffer(message));
      }
    }
  }

  protected String getAddress(Socket socket) {
    return (new StringBuilder(socket.getInetAddress().toString())).append(":").append(socket.getPort()).toString();
  }

  protected void connect(SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel) key.channel();
    try {
      channel.finishConnect();
      channel.configureBlocking(false);
      channel.register(selector, OP_WRITE);
    } catch (IOException e) {
      e.printStackTrace();
      key.channel().close();
      key.cancel();
    }
  }

  protected void read(SelectionKey key) throws IOException {
    //readQueueHandler.add(key);
    //readQueueHandler.read(key);

    SocketChannel channel = (SocketChannel) key.channel();
    buf.clear();

    List<M> messages = new ArrayList<>();

    try {
      int read;
      //refactor to Piped?
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        while ((read = channel.read(buf)) > 0) {
          buf.flip();
          byte[] bytes = new byte[buf.limit()];
          buf.get(bytes);
          out.write(bytes);
          buf.clear();
        }

        if (read == -1) {
          //
        } else {
          try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {
            M message;
            while (in.available() > 0 && (message = converter.read(mTypeRef, () -> in)) != null) {
              messages.add(message);
            }
          }
        }
      }
    } catch (IOException e) {
      //logger.warn("Clear messages");
      messages.clear();
      handleException(key, e);
      translate(e);
    }

    if (messages.isEmpty()) {
      key.channel().close();
      key.cancel();
    } else {
      for (M message : messages) {
        handleIncomingMessage(key, message);
      }
    }
  }

  protected void translate(IOException e) {
    String e1 = "Программа на вашем хост-компьютере разорвала установленное подключение";
    String e2 = "Удаленный хост принудительно разорвал существующее подключение";
    if (e1.equals(e.getMessage()) || e2.equals(e.getMessage())) {
      //ignore
    } else {
      logger.warn("Could not read: " + e.getMessage());
    }
  }
}
