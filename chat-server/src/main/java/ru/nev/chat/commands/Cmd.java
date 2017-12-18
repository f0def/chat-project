package ru.nev.chat.commands;

public interface Cmd<R> {

  R execute();
}
