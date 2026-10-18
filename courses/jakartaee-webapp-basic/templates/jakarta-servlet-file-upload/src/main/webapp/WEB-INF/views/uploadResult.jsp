<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>アップロード結果</title>
</head>
<body>
<h2>アップロード結果</h2>
<hr />
<dl>
  <dt>ID</dt><dd>${storedFile.id()}</dd>
  <dt>タイトル</dt><dd><c:out value="${storedFile.title()}" /></dd>
  <dt>ファイル名</dt><dd><c:out value="${storedFile.originalName()}" /></dd>
  <dt>サイズ（バイト）</dt><dd>${storedFile.size()}</dd>
</dl>
<a href="${pageContext.request.contextPath}/FileUploadPage.html">続けてアップロードする</a>
</body>
</html>
