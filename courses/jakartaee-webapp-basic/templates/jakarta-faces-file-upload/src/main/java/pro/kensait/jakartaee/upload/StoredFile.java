package pro.kensait.jakartaee.upload;

/** 保存したファイルのメタデータ（公開用の ID・タイトル・元のファイル名・保存先キー・容量） */
public record StoredFile(Long id, String title, String originalName,
        String storageKey, long size) {
}
