package by.yurhilevich;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {

    private final FileProcessingService fileProcessingService;
    private final List<LanguageService> recognitionServices;

    public MainController(FileProcessingService fileProcessingService, List<LanguageService> services) {
        this.fileProcessingService = fileProcessingService;
        this.recognitionServices = services;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/recognize")
    public String recognize(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Пожалуйста, выберите файл для загрузки.");
            return "index";
        }

        try {
            String cleanText = fileProcessingService.cleanTextFromFile(file);
            List<AnalysisResult> results = new ArrayList<>();

            for(LanguageService service : recognitionServices) {
                long startTime = System.currentTimeMillis();
                Language detected = service.recognize(cleanText);
                long endTime = System.currentTimeMillis();
                results.add(new AnalysisResult(service.getMethodName(), detected, endTime - startTime));
            }

            model.addAttribute("fileName", file.getOriginalFilename());
            model.addAttribute("results", results);

            return "result";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при обработке файла: " + e.getMessage());
            return "index";
        }
    }
}