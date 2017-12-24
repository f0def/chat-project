package ru.nev.chat.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nev.chat.converter.MessageConverter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.LinkedList;

import static java.nio.channels.SelectionKey.*;

public class SocketClientTransport<M> extends AbstractSocketTransport<M> {

  private final Logger logger = LoggerFactory.getLogger(SocketClientTransport.class);

  private final LinkedList<M> messages;

  public SocketClientTransport(String id, String host, int port, MessageConverter<M> converter) throws IOException {
    super(id, new InetSocketAddress(host, port), converter);
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
  protected void write(SelectionKey key) throws IOException {
    while (!messages.isEmpty()) {
      M message = messages.poll();

      logger.trace("Write: " + message + ". Left " + messages.size());

      write(key, message);
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

  @Override
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

  @Override
  public void write(M message) {
    messages.add(message);
    SelectionKey key = null;
    try {
      key = channel.keyFor(selector);
      key.interestOps(OP_WRITE);
    } catch (Exception e) {
      handleException(key, e);
    }
  }

  @Override
  public void writeAllExcept(M message, SelectionKey except) {
    //
  }

}
