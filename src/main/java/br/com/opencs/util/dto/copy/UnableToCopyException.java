package br.com.opencs.util.dto.copy;

public class UnableToCopyException extends AutoCopyException {
	
	private static final long serialVersionUID = 1L;
	
	public UnableToCopyException() {
	}

	public UnableToCopyException(String message) {
		super(message);
	}

	public UnableToCopyException(Throwable message) {
		super(message);
	}

	public UnableToCopyException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnableToCopyException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
