package br.com.bea.androidtools.api.storage;

public class WrongQueryImplementatioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WrongQueryImplementatioException(final String message) {
        super(message);
    }

    public WrongQueryImplementatioException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public WrongQueryImplementatioException(final Throwable cause) {
        super(cause);
    }

}
