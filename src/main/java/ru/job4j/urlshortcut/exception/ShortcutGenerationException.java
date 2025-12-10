package ru.job4j.urlshortcut.exception;

public class ShortcutGenerationException extends RuntimeException {
    public ShortcutGenerationException() {
        super("Failed to generate unique shortcut");
    }
}
