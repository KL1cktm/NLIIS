package by.yurhilevich;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileProcessingService {
    public String cleanTextFromFile(MultipartFile file) throws IOException {
        String htmlContent = new String(file.getBytes());
        String text = Jsoup.parse(htmlContent).text();
        return text.toLowerCase().replaceAll("[^а-яa-zàèéìòù\\s]", "");
    }
}