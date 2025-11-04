package by.yurhilevich;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DictionaryRepository extends JpaRepository<DictionaryEntry, Long> {

    // Поиск слова в нашем словаре
    Optional<DictionaryEntry> findByEnglishWord(String englishWord);
}