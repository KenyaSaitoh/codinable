package pro.kensait.spring.download;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/*
 * ファイルダウンロードのコントローラーを担うクラス
 */
@Controller
public class FileDownloadController {

    // 保存場所（プロジェクトの直下の download フォルダ）
    private final Path fileStorageLocation =
            Paths.get("download").toAbsolutePath().normalize();

    // downloadファイルの実行
    @GetMapping("/download/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            // 保存場所の外を指す名前は、対象なしと同じ扱いにする
            if (!filePath.startsWith(fileStorageLocation)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                                + resource.getFilename() + "\"")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    // アクションメソッド：入力画面への遷移
    @GetMapping("/")
    public String index() {
        return "FileDownloadPage";
    }

    // アクションメソッド：入力されたファイル名のダウンロードリンクを表示
    @GetMapping("/download")
    public String showLink(@RequestParam String fileName, Model model) {
        model.addAttribute("fileName", fileName);
        return "FileDownloadPage";
    }
}
