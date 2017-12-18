package ru.nev.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nev.chat.converter.MessageConverter;
import ru.nev.chat.messages.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.State.NEW;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public abstract class AbstractChatComponent implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(AbstractChatComponent.class);

  protected final ByteBuffer buf;
  protected final Selector selector;
  protected final AbstractSelectableChannel channel;
  protected final InetSocketAddress address;
  protected final MessageConverter<Message> converter;
  private volatile boolean running;
  protected final ExecutorService pool;

  public AbstractChatComponent(int port, MessageConverter<Message> converter) throws IOException {
    this(new InetSocketAddress(port), converter);
  }

  public AbstractChatComponent(InetSocketAddress address, MessageConverter<Message> converter) throws IOException {
    this.address = address;
    this.converter = converter;
    this.buf = ByteBuffer.allocate(256);
    this.channel = channel(address);
    this.selector = selector();
    pool = Executors.newFixedThreadPool(8);
  }

  protected abstract AbstractSelectableChannel channel(InetSocketAddress address) throws IOException;

  protected abstract Selector selector() throws IOException;

  protected abstract void handleIncomingMessage(SelectionKey sender, Message message) throws IOException;

  protected abstract void write(SelectionKey key) throws IOException;

  public synchronized void start() {
    Thread executionThread = new Thread(this);
    running = true;
    executionThread.start();
    while (executionThread.getState() == NEW) {
      //
    }
  }

  public void stop() {
    running = false;
  }

  public boolean isRunning() {
    return running;
  }

  @Override
  public void run() {
    while (running) {
      runMainLoop();
    }
    cleanUp();
  }

  private void cleanUp() {
    for (SelectionKey key : selector.keys()) {
      try {
        key.channel().close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      selector.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void runMainLoop() {
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
      e.printStackTrace();
    }
  }

  protected int select() throws IOException {
    return selector.select(1000);
  }

  protected void accept(SelectionKey key) throws IOException {
    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
    SocketChannel socketChannel = serverSocketChannel.accept();
    socketChannel.configureBlocking(false);
    socketChannel.register(selector, OP_READ);
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

  protected boolean read(SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel) key.channel();
    buf.clear();

    List<Message> messages = new ArrayList<>();

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
            Message message;
            while (in.available() > 0 && (message = converter.read(Message.class, () -> in)) != null) {
              messages.add(message);
            }
          }
        }
      }
    } catch (IOException e) {
      translate(e);
    }

    if (messages.isEmpty()) {
      key.channel().close();
      key.cancel();
      return false;
    } else {
      for (Message message : messages) {
        handleIncomingMessage(key, message);
      }
      return true;
    }
  }

  protected void translate(IOException e) {
    if ("Программа на вашем хост-компьютере разорвала установленное подключение".equals(e.getMessage())) {
      //ignore
    } else {
      logger.warn("Could not read: " + e.getMessage());
    }
  }
}
