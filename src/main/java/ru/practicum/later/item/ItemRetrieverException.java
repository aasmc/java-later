package ru.practicum.later.item;

import ru.practicum.later.common.LaterApplicationException;

public class ItemRetrieverException extends LaterApplicationException {
    public ItemRetrieverException(String message) {
        super(message);
    }

    public ItemRetrieverException(String message, Throwable cause) {
        super(message, cause);
    }
}