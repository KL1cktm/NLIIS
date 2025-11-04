package by.yurhilevich;

import lombok.Data;

@Data
public class TranslationRequest {
    private String text; // Должен совпадать с 'name' в <textarea>
}