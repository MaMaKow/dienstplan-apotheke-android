package de.mamakow.dienstplanapotheke.util;

/**
 * Ein Wrapper für Daten, die als Event über LiveData exponiert werden (z.B. SnackBar-Meldungen).
 * Garantiert, dass der Inhalt nur einmal verarbeitet wird.
 */
public class Event<T> {
    private final T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    public T peekContent() {
        return content;
    }
}
