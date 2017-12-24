package ru.nev.chat.transport;

public class Session {

  private final Object key;
  private String name;

  public Session(Object key) {
    this.key = key;
  }

  public Object getKey() {
    return key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean hasName() {
    return name != null;
  }

  @Override
  public String toString() {
    return "Session{" +
      "key=" + key +
      ", name='" + name + '\'' +
      '}';
  }
}
