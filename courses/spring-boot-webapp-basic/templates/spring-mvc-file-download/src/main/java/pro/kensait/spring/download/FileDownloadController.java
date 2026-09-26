package pro.kensait.spring.download;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.ws.rs.core.HttpHeaders;

/*
 * ファイルダウンロードのコントローラーを担うクラス
 */
@Controller
public class FileDownloadController {

    private final Path fileStorageLocation =
            Paths.get("C:/tmp/output").toAbsolutePath().normalize();

    // downloadファイルの実行
    @GetMapping("/download/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
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
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File download error: " + ex.getMessage());
        }
    }

    // アクションメソッド：入力画面への遷移
    @GetMapping("/")
    public String index() {
        return "FileDownloadPage";
    }
}
