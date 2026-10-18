package pro.kensait.jakartaee.upload;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/files")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024)
public class FileUploadServlet extends HttpServlet {
    @Inject
    private FileStorageService fileService;

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Part file;
        try {
            file = request.getPart("file");
        } catch (IllegalStateException exception) {
            response.sendError(413);
            return;
        }
        String title = request.getParameter("title");
        Long id = fileService.store(title, file);
        response.sendRedirect(request.getContextPath()
                + "/files/result?id=" + id);
    }
}
