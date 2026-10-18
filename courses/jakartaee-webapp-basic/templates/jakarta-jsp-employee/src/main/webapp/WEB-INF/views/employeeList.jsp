<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
<title>EmployeeListPage</title>
</head>
<body>
<h2>社員一覧</h2>
<hr />
<table border="1">
  <tr><th>ID</th><th>氏名</th><th>部署名</th><th>月給</th><th>操作</th></tr>
  <c:forEach var="employee" items="${employeeList}">
    <tr>
      <td>${employee.employeeId}</td>
      <td><c:out value="${employee.employeeName}" /></td>
      <td><c:out value="${employee.departmentName}" default="（所属なし）" /></td>
      <td><fmt:formatNumber value="${employee.salary}" pattern="#,##0" /></td>
      <td>
        <form method="post"
              action="${pageContext.request.contextPath}/employees/delete">
          <input type="hidden" name="employeeId" value="${employee.employeeId}" />
          <button type="submit">削除</button>
        </form>
      </td>
    </tr>
  </c:forEach>
</table>
<c:if test="${empty employeeList}">
  <p>社員が登録されていません。</p>
</c:if>
</body>
</html>
