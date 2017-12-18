package ru.nev.chat.commands;

import ru.nev.chat.messages.TextMessage;
import ru.nev.chat.server.ChatServer;

import java.util.List;

import static java.util.Collections.singletonList;

public class UsersCmd implements Cmd<List<TextMessage>> {

  private final ChatServer chatServer;

  public UsersCmd(ChatServer chatServer) {
    this.chatServer = chatServer;
  }

  @Override
  public List<TextMessage> execute() {
    return singletonList(new TextMessage("Number of connected users: " + chatServer.getNumberOfConnectedUsers()));
  }
}
