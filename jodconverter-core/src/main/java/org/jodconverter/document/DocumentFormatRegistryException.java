package org.jodconverter.document;

/**
 * Exception thrown when a {@link DefaultDocumentFormatRegistry} cannot be created and initialized
 * properly.
 */
public class DocumentFormatRegistryException extends RuntimeException {
  private static final long serialVersionUID = -4334974313547581948L;

  /**
   * Constructs a exception with the specified detail message and cause.
   *
   * @param message the detail message.
   * @param cause the cause.
   */
  public DocumentFormatRegistryException(String message, Throwable cause) {
    super(message, cause);
  }
}
