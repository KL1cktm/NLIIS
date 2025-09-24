package by.yurhilevich;

import java.util.List;

public class SearchResult {
    private String title;
    private String snippet;
    private double rank;
    private List<String> presentTerms;
    private String filePath; // <-- НОВОЕ ПОЛЕ

    public SearchResult(String title, String snippet, double rank, List<String> presentTerms, String filePath) {
        this.title = title;
        this.snippet = snippet;
        this.rank = rank;
        this.presentTerms = presentTerms;
        this.filePath = filePath; // <-- ОБНОВЛЕННЫЙ КОНСТРУКТОР
    }

    // Геттеры и сеттеры
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }
    public double getRank() { return rank; }
    public void setRank(double rank) { this.rank = rank; }
    public List<String> getPresentTerms() { return presentTerms; }
    public void setPresentTerms(List<String> presentTerms) { this.presentTerms = presentTerms; }
    public String getFilePath() { return filePath; } // <-- ГЕТТЕР ДЛЯ НОВОГО ПОЛЯ
    public void setFilePath(String filePath) { this.filePath = filePath; }
}