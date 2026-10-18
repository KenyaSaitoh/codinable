<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>CalcInputPage</title>
</head>
<body>
<h2>CalcInputPage</h2>
<hr />
<form method="post" action="${pageContext.request.contextPath}/add">
  <div><span>パラメータ1</span><input type="text" name="param1" /></div>
  <div><span>パラメータ2</span><input type="text" name="param2" /></div>
  <button type="submit">足し算</button>
</form>
<c:if test="${not empty sessionScope.lastResult}">
  <p>前回の計算結果 ---&gt; <c:out value="${sessionScope.lastResult}" /></p>
</c:if>
</body>
</html>
