package by.yurhilevich;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaResponse {
    private String response;

    // Геттеры и сеттеры
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
}