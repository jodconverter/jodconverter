package net.sf.jodconverter.util;

/**
 * Represents an error condition that can be temporary, i.e. that could go
 * away by simply retrying the same operation after an interval.  
 */
public class TemporaryException extends Exception {

    private static final long serialVersionUID = 7237380113208327295L;

    public TemporaryException(Throwable cause) {
        super(cause);
    }

}
