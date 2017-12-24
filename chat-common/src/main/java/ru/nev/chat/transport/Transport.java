package ru.nev.chat.transport;

public interface Transport<P, M> {

  void addConsumer(TransportConsumer<P, M> consumer);

  /**
   * Write message to one receiver.
   */
  void write(P receiver, M message);

  /**
   * Write message to all receivers.
   */
  void write(M message);

  void writeAllExcept(M message, P except);

  Object getKey(P sender);
}
