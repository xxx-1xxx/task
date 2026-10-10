<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  实验四 · 员工列表页
  ==================================================================
  这个页面证明了两件事：
    1. MyBatis 的关联查询结果，能一路带到 JSP 上显示；
    2. JSTL 用 Jakarta 命名空间（jakarta.tags.core），不是教材 ch15 里的
       http://java.sun.com/jsp/jstl/core —— 后者在 Tomcat 10 下直接失效。

  页面放在 /WEB-INF/jsp/ 下，浏览器不能直接访问，只能由 Servlet/Controller
  转发进来。这是刻意的：不让用户绕过控制层直访视图。
--%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>员工列表 · 实验四</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css">
</head>
<body>

<div class="page">

    <h1>员工列表</h1>

    <p class="hint">
        共 <strong>${list.size()}</strong> 条记录 ｜
        <span class="tag">实验四 · MyBatis 关联查询</span>
    </p>

    <table class="data-table">
        <thead>
        <tr>
            <th>编号</th>
            <th>姓名</th>
            <th>性别</th>
            <th>所属部门</th>
        </tr>
        </thead>
        <tbody>
        <%--
          ${e.dept.name} 是这一页最该看的一行：
          dept 不是 employee 表的列，而是关联查询组装出来的对象。
          如果把 findAll 改成单表查询，这里会整列变空 —— 这就是
          "关联查询有没有生效"的肉眼判定方法。
        --%>
        <c:forEach items="${list}" var="e">
            <tr>
                <td>${e.id}</td>
                <td>${e.name}</td>
                <td>${e.gender}</td>
                <td>${e.dept.name}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <p class="note">
        ℹ️ 本页由 <code>EmployeeServlet</code>（实验四写法）转发而来。
        实验五会把它换成 <code>EmployeeController</code>，
        数据来源改为 Spring 管理的 Mapper —— 但页面这一份不用改。
    </p>

</div>

</body>
</html>
