package ru.nev.chat.jmeter.cli;

import ru.nev.chat.jmeter.SocketTestSampler;

import java.io.IOException;

public class Sampler {

  public static void main(String args[]) {
    SocketTestSampler sampler = new SocketTestSampler();
    try {
      sampler.process("localhost", 3128, 5, 15, 40, 80);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
