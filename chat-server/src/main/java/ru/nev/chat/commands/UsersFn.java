package ru.nev.chat.commands;

import ru.nev.chat.messages.TextMessage;
import ru.nev.chat.server.ChatServer;

import java.util.List;

public class UsersFn implements Fn<String, Cmd<List<TextMessage>>> {

  private final UsersCmd usersCmd;

  public UsersFn(ChatServer chatServer) {
    this.usersCmd = new UsersCmd(chatServer);
  }

  public Cmd<List<TextMessage>> apply(String option) {
    return usersCmd;
  }
}
