package ru.job4j.urlshortcut.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

public class Utils {
    public static boolean containsConstraintName(DataIntegrityViolationException ex, String constraintName) {
        Throwable cause = ex.getCause();

        while (cause != null) {
            if (cause instanceof ConstraintViolationException) {
                SQLException sqlEx = ((ConstraintViolationException) cause).getSQLException();
                if (sqlEx.getMessage().contains(constraintName)) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
}
