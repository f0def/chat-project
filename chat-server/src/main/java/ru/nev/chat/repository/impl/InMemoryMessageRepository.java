package ru.nev.chat.repository.impl;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import ru.nev.chat.repository.MessageRepository;

import java.util.Queue;

public class InMemoryMessageRepository<M> implements MessageRepository<M> {

  private final Queue<M> lastMessages;

  public InMemoryMessageRepository(int maxSize) {
    EvictingQueue<M> q = EvictingQueue.create(maxSize);
    this.lastMessages = Queues.synchronizedQueue(q);
  }

  @Override
  public void add(M message) {
    lastMessages.add(message);
  }

  @Override
  public Queue<M> getLastMessages() {
    return lastMessages;
  }
}
