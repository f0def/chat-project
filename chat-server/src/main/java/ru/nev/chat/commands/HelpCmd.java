package ru.nev.chat.commands;

import ru.nev.chat.messages.Message;
import ru.nev.chat.messages.TextMessage;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class HelpCmd implements Cmd<List<Message>> {

  private final CommandParser<Cmd<List<Message>>> parser;

  public HelpCmd(CommandParser<Cmd<List<Message>>> parser) {
    this.parser = parser;
  }

  @Override
  public List<Message> execute() {
    String commands = parser.getCommands().stream()
      .map(cmdCliCommand -> "\t" + "/" + cmdCliCommand.getName() + " - " + cmdCliCommand.getDescription())
      .collect(Collectors.joining("\n"));

    return singletonList(new TextMessage("available commands:\n" + commands));
  }
}
