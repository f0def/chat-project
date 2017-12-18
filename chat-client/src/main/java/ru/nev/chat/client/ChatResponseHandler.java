package ru.nev.chat.client;

import ru.nev.chat.messages.Message;

public interface ChatResponseHandler {
  void onMessage(Message message);
}
