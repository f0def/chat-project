package ru.nev.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nev.chat.transport.RunnableTransport;
import ru.nev.chat.transport.TransportConsumer;

public abstract class AbstractChatClient<P, M> implements TransportConsumer<P, M> {

  private final Logger logger = LoggerFactory.getLogger(AbstractChatClient.class);

  protected final RunnableTransport<P, M> transport;
  private final ChatResponseHandler<M> handler;

  public AbstractChatClient(String id, RunnableTransport<P, M> transport, ChatResponseHandler<M> handler) {
    this.transport = transport;
    this.transport.addConsumer(this);
    this.handler = handler;
  }

  @Override
  public M accept(P sender, Object key) {
    return null;
  }

  @Override
  public void read(P sender, M message) {
    logger.trace("{}", message);
    handler.onMessage(message);
  }

  @Override
  public void exception(P sender, Exception e) {
    logger.debug("Ignore " + sender + ": " + e.getMessage());
  }

  public void sendMessage(M message) {
    transport.write(message);
  }

  public void start() {
    transport.start();
  }

  public void stop() {
    transport.stop();
  }
}
