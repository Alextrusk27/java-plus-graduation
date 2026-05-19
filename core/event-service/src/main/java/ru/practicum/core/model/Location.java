package ru.practicum.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Setter
@Getter
@ToString
public class Location {
    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lon;
}
