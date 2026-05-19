package ru.practicum.core.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntityName {
    USER("User"),
    CATEGORY("Category"),
    EVENT("Event"),
    REQUEST("Request"),
    COMPILATION("Compilation"),
    COMMENT("Comment");

    private final String value;
}
