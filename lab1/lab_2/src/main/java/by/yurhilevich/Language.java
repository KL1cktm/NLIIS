package by.yurhilevich;

public enum Language {
    RUSSIAN("Русский"),
    ITALIAN("Итальянский"),
    UNKNOWN("Неизвестно");

    private final String name;

    Language(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}