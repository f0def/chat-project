package ru.nev.chat.messages;

public class TextMessage extends Message {

  private String text;

  public TextMessage(String text) {
    super(null);
    this.text = text;
  }

  public TextMessage(User user, String text) {
    super(user);
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String toChat() {
    return text;
  }

  @Override
  public String toString() {
    return "TextMessage{" +
      "user='" + getUser() + '\'' +
      "text='" + text + '\'' +
      '}';
  }
}
