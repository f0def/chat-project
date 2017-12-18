package ru.nev.chat.messages;

import java.io.Serializable;

public abstract class Message implements Serializable {

  private User user;

  public Message(User user) {
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public abstract String toChat();

  @Override
  public String toString() {
    return "Message{" +
      "user=" + user +
      '}';
  }
}
