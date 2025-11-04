package by.yurhilevich;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dictionary")
@Data
@NoArgsConstructor
public class DictionaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String englishWord;

    private String russianTranslation;

    private String posTag; // Тег (напр. "NN") [cite: 26]
    private String posTagDescription; // Расшифровка [cite: 26]

    public DictionaryEntry(String englishWord, String russianTranslation, String posTag, String posTagDescription) {
        this.englishWord = englishWord;
        this.russianTranslation = russianTranslation;
        this.posTag = posTag;
        this.posTagDescription = posTagDescription;
    }
}