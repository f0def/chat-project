package ru.nev.chat.transport;

public interface TransportConsumer<P, M> {

  M accept(P sender, Object key);

  void read(P sender, M message);

  void exception(P sender, Exception e);
}
