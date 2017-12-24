package ru.nev.chat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nev.chat.messages.Message;
import ru.nev.chat.repository.SessionRepository;
import ru.nev.chat.transport.AbstractProcessQueueHandler;
import ru.nev.chat.transport.MessageProcessQueueHandler;
import ru.nev.chat.transport.RunnableTransport;
import ru.nev.chat.transport.Session;
import ru.nev.chat.transport.TransportConsumer;

public abstract class AbstractChatServer<P, M> implements TransportConsumer<P, M> {

  private final Logger logger = LoggerFactory.getLogger(AbstractChatServer.class);

  protected final RunnableTransport<P, M> transport;
  private final SessionRepository sessionRepository;
  private final AbstractProcessQueueHandler<P, M> readQueueHandler;

  public AbstractChatServer(String id, RunnableTransport<P, M> transport,
                            AbstractProcessQueueHandler<P, M> readQueueHandler,
                            SessionRepository sessionRepository) {
    this.transport = transport;
    this.transport.addConsumer(this);

    this.readQueueHandler = readQueueHandler;
    this.sessionRepository = sessionRepository;

    logger.debug("Starting chat server");
  }

  @Override
  public M accept(P sender, Object key) {
    Session session = new Session(key);
    sessionRepository.put(key, session);
    return null;
  }

  @Override
  public void read(P sender, M message) {
    Object key = transport.getKey(sender);
    Session session = sessionRepository.get(key);
    if (session == null) {
      throw new RuntimeException("Could not get sender for key " + key);
    }

    //readQueueHandler.addRequest(new Request<>(sender, session, message));
    ((MessageProcessQueueHandler) readQueueHandler).processMessage(session, sender, (Message) message);
  }

  @Override
  public void exception(P sender, Exception e) {
    Object key = transport.getKey(sender);
    logger.debug(key + " left the chat");
    sessionRepository.remove(key);
  }

  public void start() {
    transport.start();
  }

  public void stop() {
    transport.stop();
  }
}
