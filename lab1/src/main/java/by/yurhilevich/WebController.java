package by.yurhilevich;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebController {

    private final SearchService searchService;

    public WebController(SearchService searchService) {
        this.searchService = searchService;
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
            e.printStackTrace(); // В реальном приложении здесь должно быть логирование
            model.addAttribute("error", "Произошла ошибка во время поиска.");
        }
        return "results";
    }
}
