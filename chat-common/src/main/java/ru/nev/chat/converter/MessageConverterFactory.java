package ru.nev.chat.converter;

public class MessageConverterFactory {

  public static final MessageConverterFactory INSTANCE = new MessageConverterFactory();

  private MessageConverterFactory() {
  }

  public <T> MessageConverter<T> make() {
    return (MessageConverter<T>) new IoMessageConverter();
  }
}
