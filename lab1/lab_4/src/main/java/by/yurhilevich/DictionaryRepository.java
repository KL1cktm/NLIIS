package by.yurhilevich;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DictionaryRepository extends JpaRepository<DictionaryEntry, Long> {

    Optional<DictionaryEntry> findByEnglishWord(String englishWord);
    Optional<DictionaryEntry> findByRussianTranslation(String russianWord);
}