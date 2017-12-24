package ru.nev.chat.commands;

import ru.nev.chat.messages.Message;
import ru.nev.chat.messages.TextMessage;
import ru.nev.chat.repository.SessionRepository;

import java.util.List;

import static java.util.Collections.singletonList;

public class UsersCmd implements Cmd<List<Message>> {

  private final SessionRepository sessionRepository;

  public UsersCmd(SessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  @Override
  public List<Message> execute() {
    return singletonList(new TextMessage("Number of connected users: " + sessionRepository.getSessionCount()));
  }
}
