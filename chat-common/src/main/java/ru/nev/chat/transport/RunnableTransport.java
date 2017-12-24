package ru.nev.chat.transport;

import ru.nev.chat.RunnableComponent;

public class RunnableTransport<P, M> extends RunnableComponent implements Transport<P, M> {

  protected RunnableTransport(String id) {
    super(id);
  }

  @Override
  protected void runMainLoop() {

  }

  @Override
  public void addConsumer(TransportConsumer<P, M> consumer) {

  }

  @Override
  public void write(P receiver, M message) {

  }

  @Override
  public void write(M message) {

  }

  @Override
  public void writeAllExcept(M message, P except) {

  }

  @Override
  public Object getKey(P sender) {
    return null;
  }
}
