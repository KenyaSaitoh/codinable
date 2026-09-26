package pro.kensait.spring.upload;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FileUploadController {
    private final FileStorageService fileService;

    public FileUploadController(FileStorageService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/")
    public String index() {
        return "FileUploadPage";
    }

    @PostMapping("/files")
    public String upload(
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes attributes) throws IOException {
        Long id = fileService.store(title, file);
        attributes.addAttribute("id", id);
        return "redirect:/files/{id}";
    }

    @GetMapping("/files/{id}")
    public String result(@PathVariable Long id, Model model) {
        model.addAttribute("storedFile", fileService.find(id));
        return "UploadResultPage";
    }
}
