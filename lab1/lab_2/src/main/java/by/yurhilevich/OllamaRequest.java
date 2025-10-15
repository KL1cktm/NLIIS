package by.yurhilevich;

public class OllamaRequest {
    private String model;
    private String prompt;
    private boolean stream = false; // Мы хотим получить ответ целиком, а не по частям

    public OllamaRequest(String model, String prompt) {
        this.model = model;
        this.prompt = prompt;
    }

    // Геттеры и сеттеры
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }
}
