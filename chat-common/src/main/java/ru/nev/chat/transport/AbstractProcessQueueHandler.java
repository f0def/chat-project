package ru.nev.chat.transport;

import ru.nev.chat.RunnableComponent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractProcessQueueHandler<P, M> extends RunnableComponent {

  private final BlockingQueue<Request<P, M>> requestBlockingQueue;
//  private final ExecutorService threadPool;
  private int threadPoolSize;

  public AbstractProcessQueueHandler(String id, int threadPoolSize) {
    super(id);
    this.threadPoolSize = threadPoolSize;
//    ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(id + "-%d").build();
//    this.threadPool = Executors.newFixedThreadPool(threadPoolSize, threadFactory);
    this.requestBlockingQueue = new LinkedBlockingQueue<>();

//    initThreadPool();
  }

//  private void initThreadPool() {
//    for (int i = 0; i < this.threadPoolSize; i++) {
//      this.threadPool.execute(this);
//    }
//  }

  public void addRequest(Request<P, M> request) {
    this.requestBlockingQueue.add(request);
  }

  public abstract void processMessage(Session session, P sender, M message);

  @Override
  protected void runMainLoop() {
    try {
      Request<P, M> request = this.requestBlockingQueue.take();
      processMessage(request.getSession(), request.getSender(), request.getMessage());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
