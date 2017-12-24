package ru.nev.chat.util;

import java.io.Closeable;
import java.io.IOException;

public class Utils {

  public static void close(Closeable... closeables) {
    for (Closeable closeable : closeables) {
      try {
        closeable.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
