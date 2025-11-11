package by.yurhilevich.dto;

public record SynthesisRequest(
        String text,
        double speakingRate,
        double stability,
        double similarityBoost
) {}
