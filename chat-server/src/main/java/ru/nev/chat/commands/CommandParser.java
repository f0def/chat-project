package ru.nev.chat.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommandParser<A> {

  private Map<String, ChatCommand<A>> map = new HashMap<>();

  public CommandParser<A> add(ChatCommand<A> command) {
    map.put(command.getName(), command);
    return this;
  }

  public A parse(String arg) {
    String cmd = getName(arg);
    if (cmd == null) {
      return null;
    }

    String args = getArgs(arg);

    ChatCommand<A> chatCommand = map.get(cmd);
    if (chatCommand == null) {
      return null;
    }

    return chatCommand.parse(args);
  }

  protected String getName(String arg) {
    if (arg == null || !arg.startsWith("/")) {
      return null;
    }
    if (arg.indexOf(" ") == -1) {
      return arg.substring(1);
    }
    return arg.substring(1, arg.indexOf(" "));
  }

  protected String getArgs(String arg) {
    if (arg.indexOf(" ") == -1) {
      return null;
    }
    return arg.substring(arg.indexOf(" ") + 1);
  }

  public Collection<ChatCommand<A>> getCommands() {
    return map.values();
  }
}
