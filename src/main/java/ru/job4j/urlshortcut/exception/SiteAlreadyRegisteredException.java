package ru.job4j.urlshortcut.exception;

public class SiteAlreadyRegisteredException extends RuntimeException {

    public SiteAlreadyRegisteredException() {
        super("Site is already registered");
    }
}
