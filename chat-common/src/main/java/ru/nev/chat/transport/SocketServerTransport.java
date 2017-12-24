package ru.nev.chat.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nev.chat.converter.MessageConverter;

import java.io.IOException;
import java.net.InetSocketAddress;
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

public class SocketServerTransport<M> extends AbstractSocketTransport<M> {

  private final Logger logger = LoggerFactory.getLogger(SocketServerTransport.class);

  public SocketServerTransport(String id, int port, MessageConverter<M> converter) throws IOException {
    super(id, port, converter);
  }

  @Override
  public void run() {
    logger.debug("Socket server transport started at {} ", this.address);
    super.run();
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
  protected void handleIncomingMessage(SelectionKey sender, M message) throws IOException {
    logger.debug("{} sent {}", sender.attachment(), message);
    super.handleIncomingMessage(sender, message);
  }

  @Override
  protected void write(SelectionKey key) throws IOException {
    ByteBuffer buffer = (ByteBuffer) key.attachment();
    SocketChannel channel = (SocketChannel) key.channel();
    channel.write(buffer);
    key.interestOps(OP_READ);
  }

  @Override
  public void write(SelectionKey receiver, M message) {
    logger.debug("Private message to {}: {}", getKey(receiver), message);
    super.write(receiver, message);
  }

  @Override
  public void write(M message) {
    logger.debug("Public message to all {}", message);

    ByteBuffer byteBuffer = checkedMakeByteBuffer(message);

    for (SelectionKey key : selector.keys()) {
      send(key, byteBuffer);
    }
  }

  @Override
  public void writeAllExcept(M message, SelectionKey except) {
    logger.debug("Public message to all {} except {}", message, getKey(except));

    ByteBuffer byteBuffer = checkedMakeByteBuffer(message);

    for (SelectionKey key : selector.keys()) {
      if (Objects.equals(key, except)) {
        continue;
      }

      send(key, byteBuffer);
    }
  }

  private void send(SelectionKey key, ByteBuffer buf) {
    if (key.isValid() && key.channel() instanceof SocketChannel) {
      SocketChannel sc = (SocketChannel) key.channel();
      try {
        sc.write(buf);
      } catch (IOException e) {
        translate(e);
      }
      buf.rewind();
    }
  }
}
