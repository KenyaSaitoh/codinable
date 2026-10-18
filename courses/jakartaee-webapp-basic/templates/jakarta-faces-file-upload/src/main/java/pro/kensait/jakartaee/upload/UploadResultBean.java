package pro.kensait.jakartaee.upload;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** アップロード結果の画面（UploadResultPage.xhtml）。ビューパラメータの ID で保存済みの情報を取り出す */
@Named
@RequestScoped
public class UploadResultBean {
    @Inject
    private FileStorageService fileService;

    private Long id;
    private StoredFile storedFile;

    public void load() {
        storedFile = fileService.find(id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StoredFile getStoredFile() {
        return storedFile;
    }
}
