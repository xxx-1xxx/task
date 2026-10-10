<%--
  ============================================================
  登录结果页（视图）· /WEB-INF/jsp/result.jsp
  ============================================================
  Controller 返回的逻辑视图名 "result" 被视图解析器拼成
      prefix + "result" + suffix = /WEB-INF/jsp/result.jsp
  然后转发到这里渲染。

  ${msg} / ${username} / ${way} 都是 Controller 通过 ModelMap
  或 ModelAndView 放进"模型"的数据，这里用 EL 表达式取出来。
  EL 取不到值时不会报错，只会显示空白 —— 所以"页面一片空白"
  通常意味着 Controller 忘了 addAttribute。
  ============================================================
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>登录结果 · zjtec-admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css">
</head>
<body>
<div class="card">
    <h1>登录结果</h1>

    <p class="result">${msg}</p>

    <table class="kv">
        <tr>
            <th>用户名</th>
            <td>${username}</td>
        </tr>
        <tr>
            <th>接参方式</th>
            <td>${way}</td>
        </tr>
        <tr>
            <th>响应方式</th>
            <td>ModelMap / ModelAndView → 视图解析器 → 本页</td>
        </tr>
        <tr>
            <th>校验来源</th>
            <td>UserServiceImpl 模拟校验（P5 起改为查 MySQL 的 user 表）</td>
        </tr>
    </table>

    <a class="btn" href="${pageContext.request.contextPath}/login">← 返回登录页</a>

    <p class="tip">
        想验证"视图被外露"的问题？直接在浏览器访问
        <code>/zjtec-admin/WEB-INF/jsp/result.jsp</code> —— 会得到 404，
        因为 <code>/WEB-INF/</code> 下的资源不允许直接访问，只能经 Controller 转发。
    </p>
</div>
</body>
</html>
