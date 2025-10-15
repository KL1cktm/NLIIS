package by.yurhilevich;

public class AnalysisResult {
    private final String methodName;
    private final Language detectedLanguage;
    private final long durationMs;

    public AnalysisResult(String methodName, Language detectedLanguage, long durationMs) {
        this.methodName = methodName;
        this.detectedLanguage = detectedLanguage;
        this.durationMs = durationMs;
    }

    public String getMethodName() {
        return methodName;
    }

    public Language getDetectedLanguage() {
        return detectedLanguage;
    }

    public long getDurationMs() {
        return durationMs;
    }
}