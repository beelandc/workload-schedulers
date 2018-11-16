package driver;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import exception.DataValidationException;
import workload.WorkloadProcessor;

public class Cli {
  private static final Logger log = Logger.getLogger(Cli.class.getName());
  private String[] args = null;
  private Options options = new Options();

  private int workloadItemCount;
  private int fixedSizeThreadPoolThreadCount;
  private int sedaPrimeSchedulerThreadCount;
  private int sedaSleepSchedulerThreadCount;
  private int sedaPrintSchedulerThreadCount;
  private int sedaBatchSize;

  private String customExecutionReport;
  private String sedaExecutionReport;
  private String defaultExecutionReport;

  public Cli(String[] args) {

    this.args = args;

    options.addOption("h", "help", false, "show help.");
    options.addOption("wc", "workloadItemCount", true, "The number of workitems to process");
    options.addOption("fs", "fixedSizeThreadPoolThreadCount", true, "The number of threads to generate for the fixed-size thread pools");
    options.addOption("sps", "sedaPrimeSchedulerThreadCount", true, "The number of threads to generate for the Prime Generation stage of the SEDA processor");
    options.addOption("sss", "sedaSleepSchedulerThreadCount", true, "The number of threads to generate for the Sleep stage of the SEDA processor");
    options.addOption("sprs", "sedaPrintSchedulerThreadCount", true, "The number of threads to generate for the Print stage of the SEDA processor");
    options.addOption("sbs", "sedaBatchSize", true, "The number of workitems to batch together when using the SEDA processor");
  }

  public void parse() {
    CommandLineParser parser = new BasicParser();

    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);

      if (cmd.hasOption("h")) {
        help();
      }

      if (cmd.hasOption("wc")) {
        workloadItemCount = Integer.valueOf(cmd.getOptionValue("wc"));
      } else {
        log.log(Level.SEVERE, "Missing wc option");
        help();
      }

      if (cmd.hasOption("fs")) {
        fixedSizeThreadPoolThreadCount = Integer.valueOf(cmd.getOptionValue("fs"));
      } else {
        log.log(Level.SEVERE, "Missing fs option");
        help();
      }

      if (cmd.hasOption("sps")) {
        sedaPrimeSchedulerThreadCount = Integer.valueOf(cmd.getOptionValue("sps"));
      } else {
        log.log(Level.SEVERE, "Missing sps option");
        help();
      }

      if (cmd.hasOption("sss")) {
        sedaSleepSchedulerThreadCount = Integer.valueOf(cmd.getOptionValue("sss"));
      } else {
        log.log(Level.SEVERE, "Missing sss option");
        help();
      }

      if (cmd.hasOption("sprs")) {
        sedaPrintSchedulerThreadCount = Integer.valueOf(cmd.getOptionValue("sprs"));
      } else {
        log.log(Level.SEVERE, "Missing sprs option");
        help();
      }

      if (cmd.hasOption("sbs")) {
        sedaBatchSize = Integer.valueOf(cmd.getOptionValue("sbs"));
      } else {
        log.log(Level.SEVERE, "Missing sbs option");
        help();
      }

      WorkloadProcessor wp = new WorkloadProcessor(workloadItemCount, fixedSizeThreadPoolThreadCount, sedaPrimeSchedulerThreadCount,
          sedaSleepSchedulerThreadCount, sedaPrintSchedulerThreadCount, sedaBatchSize);
      wp.processWorkload_CustomFixedSizeThreadPoolScheduler();
      customExecutionReport = wp.printPreviousExecutionReport();
      wp.processWorkload_SEDA();
      sedaExecutionReport = wp.printPreviousExecutionReport();
      wp.processWorkload_DefaultScheduler();
      defaultExecutionReport = wp.printPreviousExecutionReport();

      System.out.println(customExecutionReport);
      System.out.println(sedaExecutionReport);
      System.out.println(defaultExecutionReport);


    } catch (ParseException e) {
      log.log(Level.SEVERE, "Failed to parse comand line properties", e);
      help();
    } catch (InterruptedException e) {
      log.log(Level.SEVERE, "InterruptedException occurred", e);
      e.printStackTrace();
    } catch (ExecutionException e) {
      log.log(Level.SEVERE, "ExecutionException occurred", e);
      e.printStackTrace();
    } catch (DataValidationException e) {
      log.log(Level.SEVERE, "DataValidationException occurred", e);
      e.printStackTrace();
    }


  }

  private void help() {
    // This prints out some help
    HelpFormatter formater = new HelpFormatter();

    formater.printHelp("Main", options);
    System.exit(0);
  }
}
