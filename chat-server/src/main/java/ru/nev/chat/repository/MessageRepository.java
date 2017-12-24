package ru.nev.chat.repository;

import ru.nev.chat.repository.impl.InMemoryMessageRepository;

import java.util.Queue;

public interface MessageRepository<M> {

  static <M> MessageRepository<M> make() {
    return new InMemoryMessageRepository<>(100);
  }

  void add(M message);

  Queue<M> getLastMessages();
}
