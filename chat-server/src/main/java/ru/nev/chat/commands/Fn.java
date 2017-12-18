package ru.nev.chat.commands;

public interface Fn<I, R> {

  R apply(I input);
}
