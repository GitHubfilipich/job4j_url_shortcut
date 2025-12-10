package ru.job4j.urlshortcut.exception;

public class LoginGenerationException extends RuntimeException {

    public LoginGenerationException() {
        super("Failed to generate unique login");
    }
}
