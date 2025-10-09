package by.yurhilevich;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebController {

    private final SearchService searchService;
    private final IndexerService indexerService;
    private final ConfigService configService;

    public WebController( ConfigService configService, SearchService searchService, IndexerService indexerService) {
        this.configService = configService;
        this.searchService = searchService;
        this.indexerService = indexerService;
    }

    @GetMapping("/config")
    public String configPage(Model model) {
        model.addAttribute("directories", String.join("\n", configService.getSearchDirectories()));
        model.addAttribute("interval", configService.getReindexIntervalMinutes());
        return "config"; // Имя нового HTML-файла
    }

    @PostMapping("/config")
    public String saveConfig(@RequestParam String directories,
                             @RequestParam int interval,
                             RedirectAttributes redirectAttributes) {
        configService.saveSettings(directories, interval);
        redirectAttributes.addFlashAttribute("message", "Настройки успешно сохранены!");

        new Thread(() -> indexerService.rebuildIndex()).start();

        return "redirect:/config";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/search")
    public String search(@RequestParam("query") String query, Model model) {
        if (query == null || query.isBlank()) {
            return "redirect:/";
        }
        try {
            List<SearchResult> results = searchService.search(query, 10);
            model.addAttribute("results", results);
            model.addAttribute("query", query);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Произошла ошибка во время поиска.");
        }
        return "results";
    }
}
