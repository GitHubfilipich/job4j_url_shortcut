package ru.job4j.urlshortcut.exception;

public class UrlAlreadyRegisteredException extends RuntimeException {
    public UrlAlreadyRegisteredException() {
        super("URL is already registered");
    }
}
