package ru.nev.chat.transport;

public class Request<P, M> {

  private final P sender;
  private final Session session;
  private final M message;

  public Request(P sender, Session session, M message) {
    this.sender = sender;
    this.session = session;
    this.message = message;
  }

  public P getSender() {
    return sender;
  }

  public Session getSession() {
    return session;
  }

  public M getMessage() {
    return message;
  }
}
