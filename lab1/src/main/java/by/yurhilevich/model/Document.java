package by.yurhilevich.model;

import by.yurhilevich.db.DBConnection;

import java.sql.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Класс, представляющий документ и методы для работы с ним.
 * Соответствует диаграмме из лабораторной работы.
 */
public class Document {
    private String title;
    private String text;
    private Date date;
    private Time time;
    private int documentID;

    public Document(String title, String text, Date date, Time time) {
        this.title = title;
        this.text = text;
        this.date = date;
        this.time = time;
    }

    // Геттеры
    public int getDocumentID() { return documentID; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public Date getDate() { return date; }
    public Time getTime() { return time; }

    /**
     * Добавляет документ в базу данных и получает сгенерированный ID.
     * @return true, если документ успешно добавлен, иначе false.
     */
    public boolean AddDocumentToBase() {
        String sql = "INSERT INTO Documents(title, text, date, time) VALUES(?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, this.title);
            pstmt.setString(2, this.text);
            pstmt.setDate(3, new java.sql.Date(this.date.getTime()));
            pstmt.setTime(4, this.time);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        this.documentID = rs.getInt(1);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении документа в БД: " + e.getMessage());
        }
        return false;
    }

    /**
     * Статический метод для удаления документа из базы данных.
     * @param documentID Идентификатор документа.
     * @return true, если документ успешно удален, иначе false.
     */
    public static boolean DeleteDocumentFromBase(Integer documentID) {
        String sql = "DELETE FROM Documents WHERE documentId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, documentID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении документа из БД: " + e.getMessage());
        }
        return false;
    }
}
