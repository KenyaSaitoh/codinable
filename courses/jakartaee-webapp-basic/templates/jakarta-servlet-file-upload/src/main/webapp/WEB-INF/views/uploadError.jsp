<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>アップロードできません</title>
</head>
<body>
<h2>アップロードできません</h2>
<hr />
<%-- FileStorageService が送出した InvalidFileException のメッセージ（web.xml の error-page） --%>
<p><c:out value="${requestScope['jakarta.servlet.error.message']}" /></p>
<a href="${pageContext.request.contextPath}/FileUploadPage.html">アップロード画面へ戻る</a>
</body>
</html>
