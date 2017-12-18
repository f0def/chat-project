package ru.nev.chat.messages;

public class UserJoinedMessage extends TextMessage {

  public UserJoinedMessage(String name) {
    super(User.SERVER, "Greetings to @" + name);
  }
}
