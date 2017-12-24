package ru.nev.chat.client;

public interface ChatResponseHandler<M> {
  void onMessage(M message);
}
