package ru.abagiev.examples.spring.intercept.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Student {
    private long id;
    private String lastName;
    private String firstName;
}
