package br.com.opencs.util.dto.copy;

public class NoReturnException extends AutoCopyDeclarationException {

	private static final long serialVersionUID = 1L;

	public NoReturnException() {
	}

	public NoReturnException(String message) {
		super(message);
	}

	public NoReturnException(Throwable message) {
		super(message);
	}

	public NoReturnException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoReturnException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
