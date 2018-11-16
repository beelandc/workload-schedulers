package scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import exception.DataValidationException;
import thread.ThreadpoolThread;

public class FixedSizeThreadPoolExecutor implements Executor {
  
  private int threadCount;
  private AtomicBoolean active;
  private ConcurrentLinkedQueue<Runnable> runnableQueue;
  private List<ThreadpoolThread> threadPool;
  

  public FixedSizeThreadPoolExecutor(int threadCount) throws DataValidationException {
    if(threadCount < 1){
      throw new DataValidationException("Thread count must be greater than 0.");
    }
    
    active = new AtomicBoolean(true);
    runnableQueue = new ConcurrentLinkedQueue<Runnable>();
    threadPool = new ArrayList<ThreadpoolThread>(threadCount);
    this.threadCount = threadCount;
    addThreads();
  }

  public void execute(Runnable runnable) {
    if (active.get()) {
      runnableQueue.add(runnable);
    } else {
      throw new IllegalStateException("Threadpool terminating, unable to execute runnable");
    }
  }

  private void addThreads() {
    for (int x = 0; x < threadCount; x++) {
      ThreadpoolThread t = new ThreadpoolThread(active, runnableQueue);
      t.start();
      threadPool.add(t);

    }
  }
  
  public int getThreadCount(){
    return threadCount;
  }

  public void terminateThreadPool() {
    active.set(false);
  }

}
