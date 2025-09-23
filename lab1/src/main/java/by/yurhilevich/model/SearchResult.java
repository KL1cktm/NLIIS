package by.yurhilevich.model;

/**
 * Класс, представляющий один результат поиска.
 * Соответствует диаграмме из лабораторной работы.
 */
public class SearchResult {
    private int documentId;
    private String title;
    private String snippet;
    private double rank;
    private String date;

    public SearchResult(int documentId, String title, String snippet, double rank, String date) {
        this.documentId = documentId;
        this.title = title;
        this.snippet = snippet;
        this.rank = rank;
        this.date = date;
    }

    // Геттеры
    public int getDocumentId() { return documentId; }
    public String getTitle() { return title; }
    public String getSnippet() { return snippet; }
    public double getRank() { return rank; }
    public String getDate() { return date; }

    @Override
    public String toString() {
        return "SearchResult{" +
                "documentId=" + documentId +
                ", title='" + title + '\'' +
                ", snippet='" + snippet + '\'' +
                ", rank=" + rank +
                ", date='" + date + '\'' +
                '}';
    }
}
