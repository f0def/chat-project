package ru.nev.chat.converter;

import java.io.IOException;
import java.io.OutputStream;

public interface OutputMessage {
  OutputStream getBody() throws IOException;
}
