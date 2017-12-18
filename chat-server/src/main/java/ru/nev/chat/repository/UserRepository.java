package ru.nev.chat.repository;

import ru.nev.chat.repository.impl.InMemoryUserRepository;

import java.nio.channels.SelectionKey;

public interface UserRepository {

  static UserRepository make() {
    return new InMemoryUserRepository();
  }

  SelectionKey put(String name, SelectionKey key);

  void remove(String name);

  boolean isUserNameExists(String name);
}
