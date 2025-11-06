package by.yurhilevich;

import lombok.Data;

@Data
public class TranslationRequest {
    private String text;
    private String direction;
}