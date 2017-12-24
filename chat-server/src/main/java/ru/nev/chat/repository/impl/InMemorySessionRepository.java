package ru.nev.chat.repository.impl;

import ru.nev.chat.messages.User;
import ru.nev.chat.repository.SessionRepository;
import ru.nev.chat.transport.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySessionRepository implements SessionRepository {

  private Map<Object, Session> keySessionMap = new ConcurrentHashMap<>();
  private Map<String, Session> nameSessionMap = new ConcurrentHashMap<>();

  @Override
  public void put(Object key, Session session) {
    keySessionMap.put(key, session);
  }

  @Override
  public Session get(Object key) {
    return keySessionMap.get(key);
  }

  @Override
  public int getSessionCount() {
    return keySessionMap.size();
  }

  @Override
  public void putName(Session session, String name) {
    nameSessionMap.put(name, session);
    session.setName(name);
  }

  @Override
  public void remove(Object key) {
    Session session = get(key);
    if (session == null) {
      return;
    }
    keySessionMap.remove(key);
    if (session.getName() == null) {
      return;
    }
    nameSessionMap.remove(session.getName());
  }

  @Override
  public String prepareName(String name) {
    if (name == null) {
      return null;
    }
    return name.trim().toLowerCase();
  }

  @Override
  public boolean isCorrectUserName(String name) {
    if (name.startsWith("/")) {
      return false;
    }
    if (User.SERVER.getName().equalsIgnoreCase(name)) {
      return false;
    }
    return true;
  }

  @Override
  public boolean isUserNameExists(String name) {
    return nameSessionMap.containsKey(name);
  }
}
