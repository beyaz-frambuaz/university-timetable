package com.shablii.timetable.exceptions;

public class MethodNotImplementedException extends RuntimeException {

    public MethodNotImplementedException(String entity) {

        super("Method DELETE for " + entity + " not yet implemented");
    }

}
