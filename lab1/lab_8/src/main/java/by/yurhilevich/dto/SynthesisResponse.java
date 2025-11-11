package by.yurhilevich.dto;

public record SynthesisResponse(
        String audioContentBase64,
        String mimeType
) {}