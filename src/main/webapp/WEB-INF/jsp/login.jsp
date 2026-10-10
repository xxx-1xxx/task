<%--
  ============================================================
  登录页（视图）· /WEB-INF/jsp/login.jsp
  ============================================================
  这个页面同时演示两种"Controller 怎么接表单参数"，方便对比：

    表单① → POST ${contextPath}/login        → LoginController.login()
             方法形参是 @RequestParam String username / password

    表单② → POST ${contextPath}/loginByPojo  → LoginController.loginByPojo()
             方法形参是 User user（POJO 自动绑定）

  两个表单字段的 name 完全一样（username / password），
  差别只在 Controller 那一侧怎么写 —— 这就是本实验要对比的认知点。

  注意表单 action 用的是 ${pageContext.request.contextPath}，不是写死的 /zjtec-admin：
  这样部署时改了 Application context，页面不用跟着改。
  ============================================================
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>用户登录 · zjtec-admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css">
</head>
<body>
<div class="card">
    <h1>用户登录</h1>
    <p class="sub">演示账号：<code>admin</code> / <code>123</code>（P3 为模拟校验，尚未连数据库）</p>

    <!-- ============ 方式一：@RequestParam 逐参数接收 ============ -->
    <form action="${pageContext.request.contextPath}/login" method="post" class="form">
        <label>用户名
            <input type="text" name="username" value="admin" required>
        </label>
        <label>密　码
            <input type="password" name="password" value="123" required>
        </label>
        <button type="submit">登录 · @RequestParam 方式</button>
    </form>

    <div class="divider"><span>两种接参方式对比</span></div>

    <!-- ============ 方式二：POJO 自动绑定 ============ -->
    <form action="${pageContext.request.contextPath}/loginByPojo" method="post" class="form">
        <label>用户名
            <input type="text" name="username" value="admin" required>
        </label>
        <label>密　码
            <input type="password" name="password" value="123" required>
        </label>
        <button type="submit" class="btn-secondary">登录 · POJO 绑定方式</button>
    </form>

    <p class="tip">
        故意输错密码试试：把 <code>123</code> 改成别的，两种方式都会走到「失败」分支。
        两个表单调用的是<b>同一个 Service</b> —— 换接参方式不影响业务逻辑。
    </p>
</div>
</body>
</html>
