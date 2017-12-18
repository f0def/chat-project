package ru.nev.chat.commands;

public class ChatCommand<A> {

  private final String name;
  private final String description;
  private final Fn<String, A> parseFn;

  public ChatCommand(String name, String description, Fn<String, A> parseFn) {
    this.name = name;
    this.description = description;
    this.parseFn = parseFn;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public A parse(String arg) {
    return parseFn.apply(arg);
  }
}
