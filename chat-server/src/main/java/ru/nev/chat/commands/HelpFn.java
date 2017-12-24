package ru.nev.chat.commands;

import ru.nev.chat.messages.Message;

import java.util.List;

public class HelpFn implements Fn<String, Cmd<List<Message>>> {

  private final HelpCmd helpCmd;

  public HelpFn(CommandParser<Cmd<List<Message>>> parser) {
    this.helpCmd = new HelpCmd(parser);
  }

  public Cmd<List<Message>> apply(String option) {
    return helpCmd;
  }
}
