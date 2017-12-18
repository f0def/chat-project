package ru.nev.chat.commands;

import ru.nev.chat.messages.TextMessage;

import java.util.List;

public class HelpFn implements Fn<String, Cmd<List<TextMessage>>> {

  private final CommandParser<Cmd<List<TextMessage>>> parser;

  public HelpFn(CommandParser<Cmd<List<TextMessage>>> parser) {
    this.parser = parser;
  }

  public Cmd<List<TextMessage>> apply(String option) {
    return new HelpCmd(parser);
  }
}
