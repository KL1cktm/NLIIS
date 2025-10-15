package by.yurhilevich;


public interface LanguageService {
    Language recognize(String text);
    String getMethodName();
}