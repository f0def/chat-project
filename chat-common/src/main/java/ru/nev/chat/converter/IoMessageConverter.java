package ru.nev.chat.converter;

import ru.nev.chat.messages.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IoMessageConverter implements MessageConverter<Message> {

  @Override
  public Message read(Class<? extends Message> clazz, InputMessage inputMessage) throws IOException {
    try {
      return (Message) new ObjectInputStream(inputMessage.getBody()).readObject();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Message read(TypeReference<? extends Message> ref, InputMessage inputMessage) throws IOException {
    try {
      return (Message) new ObjectInputStream(inputMessage.getBody()).readObject();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void write(Message message, OutputMessage outputMessage) throws IOException {
    //TODO refactor to non-blocking! use header with object size
    //new ObjectOutputStream(outputMessage.getBody()).writeObject(message);

    try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
      new ObjectOutputStream(buffer).writeObject(message);
      outputMessage.getBody().write(buffer.toByteArray());
    }
  }
}
