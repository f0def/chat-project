package ru.nev.chat.client;

import ru.nev.chat.messages.Message;
import ru.nev.chat.transport.RunnableTransport;

public class ChatClient<P> extends AbstractChatClient<P, Message> {

  public ChatClient(RunnableTransport<P, Message> transport, ChatResponseHandler<Message> handler) {
    super("chat-clt", transport, handler);
  }

}
