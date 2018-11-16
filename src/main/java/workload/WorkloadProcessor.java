package workload;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exception.DataValidationException;
import scheduler.FixedSizeThreadPoolExecutor;

public class WorkloadProcessor {

  private final String CUSTOM_FIXED_SCHEDULER = "Custom Fixed Pool Scheduler";
  private final String SEDA_SCHEDULER = "SEDA Architecture";
  private final String DEFAULT_SCHEDULER = "Default Scheduler";


  private int workloadItemCount;
  private int fixedSizeThreadPoolThreadCount;
  private int sedaPrimeSchedulerThreadCount;
  private int sedaSleepSchedulerThreadCount;
  private int sedaPrintSchedulerThreadCount;
  private int sedaBatchSize;

  private String executionType;
  private Date executionStartTime;
  private Date executionEndTime;

  public WorkloadProcessor(int itemCount, int fsThreadCount, int sedaPrimeThreadCount, int sedaSleepThreadCount, int sedaPrintThreadCount, int sedaBatch) {
    workloadItemCount = itemCount;
    fixedSizeThreadPoolThreadCount = fsThreadCount;
    sedaPrimeSchedulerThreadCount = sedaPrimeThreadCount;
    sedaSleepSchedulerThreadCount = sedaSleepThreadCount;
    sedaPrintSchedulerThreadCount = sedaPrintThreadCount;
    sedaBatchSize = sedaBatch;
  }

  public void processWorkload_CustomFixedSizeThreadPoolScheduler() throws InterruptedException, ExecutionException, DataValidationException {
    FixedSizeThreadPoolExecutor fixedSizeThreadPoolScheduler = new FixedSizeThreadPoolExecutor(fixedSizeThreadPoolThreadCount);

    processWorkload(CUSTOM_FIXED_SCHEDULER, fixedSizeThreadPoolScheduler);

    fixedSizeThreadPoolScheduler.terminateThreadPool();
  }

  public void processWorkload_SEDA() throws InterruptedException, ExecutionException, DataValidationException {

    FixedSizeThreadPoolExecutor primeScheduler = new FixedSizeThreadPoolExecutor(sedaPrimeSchedulerThreadCount);
    FixedSizeThreadPoolExecutor sleepScheduler = new FixedSizeThreadPoolExecutor(sedaSleepSchedulerThreadCount);
    FixedSizeThreadPoolExecutor printScheduler = new FixedSizeThreadPoolExecutor(sedaPrintSchedulerThreadCount);

    processWorkload_SEDA(primeScheduler, sleepScheduler, printScheduler);

    primeScheduler.terminateThreadPool();
    sleepScheduler.terminateThreadPool();
    printScheduler.terminateThreadPool();
  }

  public void processWorkload_DefaultScheduler() throws InterruptedException, ExecutionException, DataValidationException {
    ExecutorService defaultThreadPoolScheduler = Executors.newFixedThreadPool(fixedSizeThreadPoolThreadCount); // new
                                                                                                               // ForkJoinPool(fixedSizeThreadPoolThreadCount);

    processWorkload(DEFAULT_SCHEDULER, defaultThreadPoolScheduler);

    defaultThreadPoolScheduler.shutdown();
  }

  private void processWorkload(String processType, Executor scheduler) throws InterruptedException, ExecutionException {
    executionType = processType;
    executionStartTime = new Date();

    List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
    for (int i = 0; i < workloadItemCount; ++i) {
      final int n = i;
      futures.add(CompletableFuture.runAsync(() -> processTask_singleThread(n), scheduler));
    }
    for (CompletableFuture<Void> future : futures) {
      // Ensures that the entire job is executed to completion
      future.get();
    }

    executionEndTime = new Date();
  }

  private void processWorkload_SEDA(Executor primeScheduler, Executor sleepScheduler, Executor printScheduler) throws InterruptedException, ExecutionException {

    executionType = SEDA_SCHEDULER;
    executionStartTime = new Date();

    List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();

    for (int i = 0; i < workloadItemCount; ++i) {

      final int n = i;

      // Only perform sleep once per batch
      if ((i % sedaBatchSize) == 0) {
        futures.add(CompletableFuture.supplyAsync(() -> calculateNthPrime(n), primeScheduler).thenApplyAsync((Long p) -> {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
          }
          return p;
        }, sleepScheduler).thenAcceptAsync((Long p) -> System.out.println(p), printScheduler));
      } else {
        futures
            .add(CompletableFuture.supplyAsync(() -> calculateNthPrime(n), primeScheduler).thenAcceptAsync((Long p) -> System.out.println(p), printScheduler));
      }

    }

    for (CompletableFuture<Void> future : futures) {
      // Ensures that the entire job is executed to completion
      future.get();
    }

    executionEndTime = new Date();
  }

  public String printPreviousExecutionReport() {

    long executionDuration = executionEndTime.getTime() - executionStartTime.getTime();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss");

    int threadCount;
    if (executionType == SEDA_SCHEDULER) {
      threadCount = sedaPrimeSchedulerThreadCount + sedaSleepSchedulerThreadCount + sedaPrintSchedulerThreadCount;
    } else {
      threadCount = fixedSizeThreadPoolThreadCount;
    }

    StringBuilder sb = new StringBuilder();

    sb.append("Previous Execution Report\n");
    sb.append("-----------------------------------------------\n");
    sb.append("Executor Type: " + executionType + "\n");
    sb.append("Thread Count: " + threadCount + "\n");
    sb.append("Workload Count: " + workloadItemCount + "\n");
    if (executionType == SEDA_SCHEDULER) {
      sb.append("Batch Size: " + sedaBatchSize + "\n");
    }
    sb.append("Execution Start Time: " + sdf.format(executionStartTime) + "\n");
    sb.append("Execution End Time: " + sdf.format(executionEndTime) + "\n");
    sb.append("Total Duration: " + executionDuration + " ms" + "\n");
    sb.append("Average Throughput: " + (workloadItemCount / (executionDuration / 1000)) + " tasks/sec" + "\n");
    sb.append("Average Latency: " + (executionDuration / workloadItemCount) + " ms" + "\n");
    sb.append("-----------------------------------------------\n");

    return sb.toString();
  }

  private Long calculateNthPrime(int nth) {

    int num, count, i;
    num = 1;
    count = 0;

    while (count < nth) {
      num = num + 1;
      for (i = 2; i <= num; i++) {
        if (num % i == 0) {
          break;
        }
      }
      if (i == num) {
        count = count + 1;
      }
    }

    return Long.valueOf(num);
  }

  private void processTask_singleThread(int n) {
    try {
      Long nthPrime = calculateNthPrime(n);
      Thread.sleep(10);
      System.out.println(nthPrime);
    } catch (InterruptedException e) {
      // Ignore
    }
  }

}
