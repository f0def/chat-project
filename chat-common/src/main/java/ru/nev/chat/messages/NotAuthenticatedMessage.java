package ru.nev.chat.messages;

public class NotAuthenticatedMessage extends TextMessage {

  public NotAuthenticatedMessage(String text) {
    super(User.SERVER, text);
  }
}
