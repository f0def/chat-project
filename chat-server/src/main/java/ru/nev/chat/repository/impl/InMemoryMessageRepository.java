package ru.nev.chat.repository.impl;

import com.google.common.collect.EvictingQueue;
import ru.nev.chat.messages.TextMessage;
import ru.nev.chat.repository.MessageRepository;

import java.util.Queue;

public class InMemoryMessageRepository implements MessageRepository {

  private EvictingQueue<TextMessage> lastMessages = EvictingQueue.create(100);

  @Override
  public synchronized void add(TextMessage textMessage) {
    lastMessages.add(textMessage);
  }

  @Override
  public Queue<TextMessage> getLastMessages() {
    return lastMessages;
  }
}
