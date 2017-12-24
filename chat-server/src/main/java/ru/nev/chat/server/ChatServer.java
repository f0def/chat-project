package ru.nev.chat.server;

import ru.nev.chat.messages.Message;
import ru.nev.chat.messages.NotAuthenticatedMessage;
import ru.nev.chat.repository.SessionRepository;
import ru.nev.chat.transport.MessageProcessQueueHandler;
import ru.nev.chat.transport.RunnableTransport;

public class ChatServer<P> extends AbstractChatServer<P, Message> {

  private final Message welcomeMessage;

  public ChatServer(RunnableTransport<P, Message> transport, MessageProcessQueueHandler<P> readQueueHandler,
                    SessionRepository sessionRepository) {
    super("chat-srv", transport, readQueueHandler, sessionRepository);
    this.welcomeMessage = new NotAuthenticatedMessage("Welcome to Chat! What is your name?");
  }

  @Override
  public Message accept(P sender, Object key) {
    super.accept(sender, key);
    return welcomeMessage;
  }
}
