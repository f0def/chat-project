package ru.nev.chat.converter;

import java.io.IOException;

public interface MessageConverter<T> {

  T read(Class<? extends T> clazz, InputMessage inputMessage) throws IOException;

  void write(T t, OutputMessage outputMessage) throws IOException;
}
