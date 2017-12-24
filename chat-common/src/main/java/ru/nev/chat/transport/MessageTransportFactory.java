package ru.nev.chat.transport;

import ru.nev.chat.converter.MessageConverter;

import java.io.IOException;

public class MessageTransportFactory {

  public static <M> SocketServerTransport<M> socketServer(int port, MessageConverter<M> converter) {
    try {
      return new SocketServerTransport<>("sock-srv-trst", port, converter);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <M> SocketClientTransport<M> socketClient(String host, int port, MessageConverter<M> converter) {
    try {
      return new SocketClientTransport<>("sock-clt-trst", host, port, converter);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
