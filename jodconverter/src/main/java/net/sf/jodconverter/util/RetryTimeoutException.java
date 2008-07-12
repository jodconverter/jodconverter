package net.sf.jodconverter.util;

public class RetryTimeoutException extends Exception {

    private static final long serialVersionUID = -3704437769955257514L;

    public RetryTimeoutException(Throwable cause) {
        super(cause);
    }

}
