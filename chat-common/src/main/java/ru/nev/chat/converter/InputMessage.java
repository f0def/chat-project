package ru.nev.chat.converter;

import java.io.IOException;
import java.io.InputStream;

public interface InputMessage {
  InputStream getBody() throws IOException;
}
