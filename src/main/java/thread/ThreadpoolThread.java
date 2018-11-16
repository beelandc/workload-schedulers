package thread;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadpoolThread extends Thread {
  private AtomicBoolean active;
  private ConcurrentLinkedQueue<Runnable> runnableQueue;

  public ThreadpoolThread(AtomicBoolean enabled, ConcurrentLinkedQueue<Runnable> threadPoolRunnableQueue) {
    active = enabled;
    runnableQueue = threadPoolRunnableQueue;
  }

  @Override
  public void run() {

    // If active and runnable in queue, run it
    while (active.get() || !runnableQueue.isEmpty()) {
      Runnable runnable;
      while ((runnable = runnableQueue.poll()) != null) {
        runnable.run();
      }

      // Minimize load on CPU when queue is empty
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        // Ignore
      }
    }
  }
}
