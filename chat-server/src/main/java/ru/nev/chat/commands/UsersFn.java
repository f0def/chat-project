package ru.nev.chat.commands;

import ru.nev.chat.messages.Message;
import ru.nev.chat.repository.SessionRepository;

import java.util.List;

public class UsersFn implements Fn<String, Cmd<List<Message>>> {

  private final UsersCmd usersCmd;

  public UsersFn(SessionRepository sessionRepository) {
    this.usersCmd = new UsersCmd(sessionRepository);
  }

  public Cmd<List<Message>> apply(String option) {
    return usersCmd;
  }
}
