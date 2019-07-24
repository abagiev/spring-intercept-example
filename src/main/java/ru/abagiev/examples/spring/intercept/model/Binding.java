package ru.abagiev.examples.spring.intercept.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Binding {
    private long studentId;
    private long bookId;
}
