<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
<title>EmployeeListPage</title>
<style>
  /* コースのテーマカラー（#345065 / #486A83 / #7299B5）で社員管理の画面を整える */
  body { font-family: "BIZ UDPGothic", "Yu Gothic UI", "Meiryo", sans-serif; color: #1F2D3A; background: #F6F8FA; margin: 0; padding: 20px 28px; }
  h1, h2 { color: #345065; font-size: 22px; margin: 0 0 12px; padding-bottom: 8px; border-bottom: 3px solid #486A83; }
  hr { display: none; }
  form { margin: 8px 0; }
  label { color: #486A83; font-size: 13px; margin: 0 4px 0 10px; }
  input[type=text], input[type=number], input[type=date], select { border: 1px solid #CBD5DD; border-radius: 4px; padding: 4px 6px; font-size: 14px; background: #fff; }
  button, input[type=submit] { background: #486A83; color: #fff; border: 0; border-radius: 4px; padding: 5px 14px; font-size: 14px; cursor: pointer; margin: 0 2px; }
  button:hover, input[type=submit]:hover { background: #345065; }
  button:disabled { background: #B8C4CE; cursor: default; }
  button.delete, input[value="削除"] { background: #9A5B5B; }
  a { color: #345065; }
  table { border-collapse: collapse; margin: 12px 0; background: #fff; font-size: 14px; }
  th, td { border: 1px solid #CBD5DD; padding: 6px 12px; }
  th { background: #486A83; color: #fff; font-weight: 600; text-align: left; }
  tr:nth-child(even) td { background: #EEF2F5; }
  td button, td input[type=submit] { padding: 3px 10px; font-size: 13px; }
  .error, [style*="color: red"], [style*="color:red"] { color: #B4423F; }
</style>
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
