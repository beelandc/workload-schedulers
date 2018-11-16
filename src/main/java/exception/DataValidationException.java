package exception;

public class DataValidationException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -6309233787245772248L;

  public DataValidationException(String msg) {
    super(msg);
  }

  public DataValidationException(String msg, Throwable x) {
    super(msg, x);
  }
}
