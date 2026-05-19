package ru.practicum.core.model;

public enum State {
    PUBLISHED,
    PENDING,
    CANCELED;

    public static final String DEFAULT_STATE = "PENDING";
}
