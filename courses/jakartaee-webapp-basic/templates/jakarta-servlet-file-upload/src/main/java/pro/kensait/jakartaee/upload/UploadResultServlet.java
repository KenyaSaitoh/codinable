package pro.kensait.jakartaee.upload;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** アップロード結果の表示（PRG の GET）。ID で保存済みのメタデータを取得して JSP へ渡す */
@WebServlet("/files/result")
public class UploadResultServlet extends HttpServlet {
    @Inject
    private FileStorageService fileService;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        StoredFile storedFile;
        try {
            storedFile = fileService.find(Long.valueOf(request.getParameter("id")));
        } catch (NumberFormatException exception) {
            storedFile = null;
        }
        if (storedFile == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("storedFile", storedFile);
        request.getRequestDispatcher("/WEB-INF/views/uploadResult.jsp")
                .forward(request, response);
    }
}
