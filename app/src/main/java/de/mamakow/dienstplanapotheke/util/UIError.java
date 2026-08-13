package de.mamakow.dienstplanapotheke.util;

public class UIError {
    private final String message;
    private final Type type;
    private final Runnable retryAction;

    public UIError(String message, Type type) {
        this(message, type, null);
    }

    public UIError(String message, Type type, Runnable retryAction) {
        this.message = message;
        this.type = type;
        this.retryAction = retryAction;
    }

    public String getMessage() {
        return message;
    }

    public Type getType() {
        return type;
    }

    public Runnable getRetryAction() {
        return retryAction;
    }

    public enum Type {
        TOAST,
        SNACKBAR,
        SNACKBAR_WITH_RETRY
    }
}
