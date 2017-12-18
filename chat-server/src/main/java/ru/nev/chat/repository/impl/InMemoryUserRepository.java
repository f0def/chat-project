package ru.nev.chat.repository.impl;

import ru.nev.chat.messages.User;
import ru.nev.chat.repository.UserRepository;

import java.nio.channels.SelectionKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {

  private volatile Map<String, SelectionKey> users = new ConcurrentHashMap<>();

  @Override
  public SelectionKey put(String name, SelectionKey key) {
    return users.put(name, key);
  }

  @Override
  public void remove(String name) {
    users.remove(name);
  }

  @Override
  public boolean isUserNameExists(String name) {
    name = name.trim();
    if (User.SERVER.getName().equalsIgnoreCase(name)) {
      return true;
    }
    return users.containsKey(name);
  }
}
