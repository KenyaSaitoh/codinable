package pro.kensait.spring.upload;

public record StoredFile(Long id, String title, String originalName,
        String storageKey, long size) {
}
