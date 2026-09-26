package pro.kensait.spring.upload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private final Path storageRoot = Path.of(
            System.getProperty("java.io.tmpdir"), "spring-file-upload");
    private final AtomicLong sequence = new AtomicLong();
    private final Map<Long, StoredFile> files = new ConcurrentHashMap<>();

    public FileStorageService() throws IOException {
        Files.createDirectories(storageRoot);
    }

    public Long store(String title, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("ファイルを選択してください");
        }

        String originalName = file.getOriginalFilename();
        String storageKey = UUID.randomUUID().toString();
        Path target = storageRoot.resolve(storageKey);

        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target);
        }

        long id = sequence.incrementAndGet();
        files.put(id, new StoredFile(id, title, originalName,
                storageKey, file.getSize()));
        return id;
    }

    public StoredFile find(Long id) {
        StoredFile storedFile = files.get(id);
        if (storedFile == null) {
            throw new IllegalArgumentException("アップロード結果が見つかりません");
        }
        return storedFile;
    }
}
