// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.exception;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents system level failures. Examples are running out of memory, etc.
 */
public abstract class SystemException extends RuntimeException {

 private static final long serialVersionUID = -2986772351198778308L;

 /**
  * Creates an instance.
  * @param message
  *         the message for the exception.
  */
 protected SystemException(String message) {

  super(checkNotNull(message));
 }

 /**
  * Creates an instance.
  * @param message
  *         the message for the exception.
  * @param cause
  *         the cause of the exception
  */
 protected SystemException(String message, Throwable cause) {

  super(checkNotNull(message), checkNotNull(cause));
 }
}
