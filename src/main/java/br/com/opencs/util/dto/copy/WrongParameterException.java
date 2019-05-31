package br.com.opencs.util.dto.copy;

public class WrongParameterException extends AutoCopyDeclarationException {

	private static final long serialVersionUID = 1L;

	public WrongParameterException() {
	}

	public WrongParameterException(String message) {
		super(message);
	}

	public WrongParameterException(Throwable message) {
		super(message);
	}

	public WrongParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrongParameterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
