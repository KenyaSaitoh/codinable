package pro.kensait.leafbooks.api;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * 画像API コントローラー
 * classpath内の画像リソースを配信する
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private static final String IMAGES_PATH = "static/images/covers/";
    private static final String NO_IMAGE_FILE = "no-image.jpg";
    // 書籍coverimageの取得
    @GetMapping("/covers/{bookId}")
    public ResponseEntity<byte[]> getBookCoverImage(@PathVariable Integer bookId) {
        logger.info("[ ImageController#getBookCoverImage ] bookId: {}", bookId);

        // 画像ファイルパス: static/images/covers/{bookId}.jpg
        String fileName = bookId + ".jpg";
        logger.debug("[ ImageController ] Looking for image file: {}", fileName);
        
        Resource resource = new ClassPathResource(IMAGES_PATH + fileName);

        // 画像が存在しない場合はno-imageを返す
        if (!resource.exists()) {
            logger.warn("[ ImageController ] Image not found for bookId: {}, using no-image", bookId);
            return getNoImage();
        }

        try (InputStream inputStream = resource.getInputStream()) {
            byte[] imageBytes = inputStream.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            logger.error("[ ImageController ] Error reading image file for bookId {}: {}", bookId, e.getMessage());
            return getNoImage();
        }
    }
    // noimageの取得
    private ResponseEntity<byte[]> getNoImage() {
        try {
            Resource noImageResource = new ClassPathResource(IMAGES_PATH + NO_IMAGE_FILE);
            InputStream inputStream = noImageResource.getInputStream();
            byte[] imageBytes = inputStream.readAllBytes();
            inputStream.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("[ ImageController ] Error reading no-image file: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
