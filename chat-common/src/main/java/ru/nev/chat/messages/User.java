package ru.nev.chat.messages;

import java.io.Serializable;

public class User implements Serializable {

  public static final User SERVER = new User("Server");

  private String name;

  public User(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "User{" +
      "name='" + name + '\'' +
      '}';
  }
}
