package ru.nev.chat.commands;

import ru.nev.chat.messages.TextMessage;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class HelpCmd implements Cmd<List<TextMessage>> {

  private final CommandParser<Cmd<List<TextMessage>>> parser;

  public HelpCmd(CommandParser<Cmd<List<TextMessage>>> parser) {
    this.parser = parser;
  }

  @Override
  public List<TextMessage> execute() {
    String commands = parser.getCommands().stream()
      .map(cmdCliCommand -> "\t" + "/" + cmdCliCommand.getName() + " - " + cmdCliCommand.getDescription())
      .collect(Collectors.joining("\n"));

    return singletonList(new TextMessage("available commands:\n" + commands));
  }
}
