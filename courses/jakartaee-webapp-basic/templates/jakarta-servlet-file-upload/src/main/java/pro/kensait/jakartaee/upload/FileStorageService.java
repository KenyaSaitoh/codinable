package pro.kensait.jakartaee.upload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.Part;

/**
 * ファイルの検証と保存を受け持つ
 * 本体は OS の一時ディレクトリの下の jakarta-file-upload に、アプリケーションが発行した保存先キーの名前で置き、
 * メタデータ（元のファイル名など）はメモリー上の Map に持つ
 */
@ApplicationScoped
public class FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "txt");

    private final AtomicLong sequence = new AtomicLong();
    private final Map<Long, StoredFile> files = new ConcurrentHashMap<>();
    private Path storageRoot;

    @PostConstruct
    public void init() {
        try {
            storageRoot = Path.of(System.getProperty("java.io.tmpdir"), "jakarta-file-upload");
            Files.createDirectories(storageRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("保存先を作れません", exception);
        }
    }

    public Long store(String title, Part file) throws IOException {
        String originalName = displayName(file.getSubmittedFileName());
        validate(file, originalName);

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
        return id == null ? null : files.get(id);
    }

    private String displayName(String submitted) {
        if (submitted == null || submitted.isBlank()) {
            throw new InvalidFileException("ファイルを選択してください");
        }
        int slash = Math.max(submitted.lastIndexOf('/'),
                submitted.lastIndexOf('\\'));
        String name = submitted.substring(slash + 1);
        if (name.isBlank() || 255 < name.length()
                || name.chars().anyMatch(Character::isISOControl)) {
            throw new InvalidFileException("ファイル名が正しくありません");
        }
        return name;
    }

    private void validate(Part file, String name) {
        if (file.getSize() == 0) {
            throw new InvalidFileException("空のファイルは保存できません");
        }
        int dot = name.lastIndexOf('.');
        String extension = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (dot < 0 || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException("この形式のファイルは保存できません");
        }
    }
}
