package by.yurhilevich;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlphabeticalService implements LanguageService {

    private Map<Character, Double> russianProfile;
    private Map<Character, Double> italianProfile;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() throws Exception {
        russianProfile = objectMapper.readValue(
                new ClassPathResource("profiles/ru_alphabet_profile.json").getInputStream(),
                new TypeReference<>() {}
        );
        italianProfile = objectMapper.readValue(
                new ClassPathResource("profiles/it_alphabet_profile.json").getInputStream(),
                new TypeReference<>() {}
        );
    }

    @Override
    public Language recognize(String text) {
        Map<Character, Double> textProfile = calculateProfile(text);

        double russianDistance = calculateDistance(textProfile, russianProfile);
        double italianDistance = calculateDistance(textProfile, italianProfile);

        if (russianDistance < italianDistance) return Language.RUSSIAN;
        if (italianDistance < russianDistance) return Language.ITALIAN;

        return Language.UNKNOWN;
    }

    private Map<Character, Double> calculateProfile(String text) {
        Map<Character, Integer> counts = new HashMap<>();
        int totalLetters = 0;

        for (char ch : text.toCharArray()) {
            if (Character.isLetter(ch)) {
                counts.put(ch, counts.getOrDefault(ch, 0) + 1);
                totalLetters++;
            }
        }

        Map<Character, Double> profile = new HashMap<>();
        for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
            profile.put(entry.getKey(), (double) entry.getValue() / totalLetters);
        }
        return profile;
    }

    private double calculateDistance(Map<Character, Double> profile1, Map<Character, Double> profile2) {
        double distance = 0.0;
        var allChars = new java.util.HashSet<Character>(profile1.keySet());
        allChars.addAll(profile2.keySet());

        for (Character ch : allChars) {
            double freq1 = profile1.getOrDefault(ch, 0.0);
            double freq2 = profile2.getOrDefault(ch, 0.0);
            distance += Math.abs(freq1 - freq2);
        }
        return distance;
    }

    @Override
    public String getMethodName() {
        return "Алфавитный метод";
    }
}