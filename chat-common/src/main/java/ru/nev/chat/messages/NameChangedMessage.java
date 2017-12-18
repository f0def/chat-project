package ru.nev.chat.messages;

public class NameChangedMessage extends TextMessage {

  public NameChangedMessage(String name) {
    super(User.SERVER, "You name successfully changed to @" + name);
  }

  @Override
  public String toString() {
    return "NameChangedMessage{" +
      "user='" + getUser() + '\'' +
      "text='" + getText() + '\'' +
      '}';
  }
}
