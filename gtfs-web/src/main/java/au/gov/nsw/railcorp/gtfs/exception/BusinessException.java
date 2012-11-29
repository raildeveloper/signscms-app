// RailCorp 2012
package au.gov.nsw.railcorp.gtfs.exception;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents business level failures. Examples are csv validation failure,
 * invalid feed request, etc.
 */
public abstract class BusinessException extends RuntimeException {

 private static final long serialVersionUID = -7706226899002038081L;

 /**
  * Creates an instance.
  * @param message
  *         the message for the exception.
  */
 protected BusinessException(String message) {

  super(checkNotNull(message));
 }

 /**
  * Creates an instance.
  * @param message
  *         the message for the exception.
  * @param cause
  *         the cause of the exception
  */
 protected BusinessException(String message, Throwable cause) {

  super(checkNotNull(message), checkNotNull(cause));
 }
}
