package pro.kensait.jakartaee.upload;

import java.io.IOException;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

@Named
@RequestScoped
public class FileUploadBean {
    @Inject
    private FileStorageService fileService;
    @Inject
    private FacesContext facesContext;

    private String title;
    private Part file;

    public String upload() throws IOException {
        try {
            Long id = fileService.store(title, file);
            return "UploadResultPage?faces-redirect=true&id=" + id;
        } catch (InvalidFileException exception) {
            facesContext.addMessage("uploadForm:file",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            exception.getMessage(), null));
            return null;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }
}
