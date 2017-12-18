package ru.nev.chat.repository;

import ru.nev.chat.messages.TextMessage;
import ru.nev.chat.repository.impl.InMemoryMessageRepository;

import java.util.Queue;

public interface MessageRepository {

  static MessageRepository make(){
    return new InMemoryMessageRepository();
  }

  void add(TextMessage textMessage);

  Queue<TextMessage> getLastMessages();
}
