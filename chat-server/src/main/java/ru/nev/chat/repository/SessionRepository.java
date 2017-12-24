package ru.nev.chat.repository;

import ru.nev.chat.repository.impl.InMemorySessionRepository;
import ru.nev.chat.transport.Session;

public interface SessionRepository {

  static SessionRepository make() {
    return new InMemorySessionRepository();
  }

  void put(Object key, Session session);

  Session get(Object key);

  int getSessionCount();

  void putName(Session session, String name);

  void remove(Object key);

  String prepareName(String name);

  boolean isCorrectUserName(String name);

  boolean isUserNameExists(String name);
}
