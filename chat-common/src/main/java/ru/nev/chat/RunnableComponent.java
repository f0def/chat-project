package ru.nev.chat;

import static java.lang.Thread.State.NEW;

public abstract class RunnableComponent implements Runnable {

  private volatile boolean running;
  private final String id;

  protected RunnableComponent(String id) {
    this.id = id;
  }

  public synchronized void start() {
    Thread executionThread = new Thread(this, id);
    running = true;
    executionThread.start();
    while (executionThread.getState() == NEW) {
      //
    }
  }

  public void stop() {
    running = false;
  }

  public boolean isRunning() {
    return running;
  }

  protected abstract void runMainLoop();

  protected void cleanUp() {
  }

  @Override
  public void run() {
    while (running) {
      runMainLoop();
    }
    cleanUp();
  }
}
