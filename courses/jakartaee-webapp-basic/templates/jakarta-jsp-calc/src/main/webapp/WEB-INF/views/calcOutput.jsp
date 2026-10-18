<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>CalcOutputPage</title>
</head>
<body>
  <h2>CalcOutputPage</h2>
  <hr />
  <div>計算結果 ---&gt; <c:out value="${result}" /></div>
  <a href="${pageContext.request.contextPath}/calc">入力画面へ戻る</a>
</body>
</html>
