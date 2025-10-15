package by.yurhilevich;

import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.springframework.stereotype.Service;

@Service
public class NeuralNetworkService implements LanguageService {

    private final LanguageDetector detector;

    public NeuralNetworkService(){
        this.detector = new OptimaizeLangDetector().loadModels();
    }

    @Override
    public Language recognize(String text) {
        if (text == null || text.isBlank()) {
            return Language.UNKNOWN;
        }

        LanguageResult result = detector.detect(text);
        String langCode = result.getLanguage();

        switch (langCode) {
            case "ru":
                return Language.RUSSIAN;
            case "it":
                return Language.ITALIAN;
            default:
                return Language.UNKNOWN;
        }
    }

    @Override
    public String getMethodName() {
        return "Готовая модель (Apache Tika)";
    }
}