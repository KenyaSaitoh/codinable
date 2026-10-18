package pro.kensait.jakartaee.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/download/*")
public class FileDownloadServlet extends HttpServlet {
    // ダウンロードさせるファイルの置き場所（curl/01_prepare_sample.sh で用意する）
    private static final Path STORAGE =
            Path.of("C:/tmp/output").toAbsolutePath().normalize();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String fileName = pathInfo == null ? "" : pathInfo.substring(1);
        Path file = STORAGE.resolve(fileName).normalize();
        if (!file.startsWith(STORAGE) || !Files.isRegularFile(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String contentType = Files.probeContentType(file);
        response.setContentType(contentType != null
                ? contentType : "application/octet-stream");
        response.setContentLengthLong(Files.size(file));
        response.setHeader("Content-Disposition",
                contentDisposition(file.getFileName().toString()));
        try (InputStream input = Files.newInputStream(file);
                OutputStream output = response.getOutputStream()) {
            input.transferTo(output);
        }
    }

    private static String contentDisposition(String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        String fallback = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        return "attachment; filename=\"" + fallback + "\""
                + "; filename*=UTF-8''" + encoded;
    }
}
