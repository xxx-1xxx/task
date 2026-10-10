/**
 * ============================================================
 * zjtec-admin · 员工信息管理后台（实验三~六载体工程）
 * ============================================================
 *
 * <h3>这个工程是什么</h3>
 * 一个工程，从实验三（SpringMVC）一直长到实验六（注册登录 + AJAX），
 * 而不是每个实验新建一个项目。每做完一个实验，工程就多一层能力：
 *
 * <pre>
 *   P3 ─ SpringMVC  →  web / service                （登录：模拟校验）
 *   P4 ─ MyBatis    →  + mapper / entity + 映射 XML  （员工·部门 增删改查）
 *   P5 ─ SSM 整合   →  + 数据源 &amp; SqlSessionFactory 交给 Spring
 *   P6 ─ 注册登录   →  + 统一响应体 + AJAX 异步注册
 * </pre>
 *
 * <h3>分包约定（每层只认下一层的接口）</h3>
 * <pre>
 *   web      Controller：接请求、调 Service、选视图。不写业务，不碰 SQL
 *   service  业务逻辑；接口 + impl 分离，便于替换实现与单测
 *   mapper   持久层接口（P4 起）；一个方法对应映射 XML 里的一条 SQL
 *   entity   数据库实体，字段与表结构对应
 *   common   通用件（统一响应体 Result、应用常量）
 *   config   配置与启动自检
 * </pre>
 *
 * <h3>调用链路（实验五考核必答）</h3>
 * <pre>
 *   浏览器 → DispatcherServlet → Controller → Service → Mapper → MySQL
 *                                   ↑ 返回视图名       ↑ 返回实体/集合
 * </pre>
 *
 * <p><b>技术栈</b>：Spring 6.2 + SpringMVC + MyBatis + JSP + 外部 Tomcat 10 + Maven<br>
 * <b>课程</b>：《Java后台应用项目开发》2627.1 ｜ 湛江科技学院计算机工程学院</p>
 */
package com.zjtec.admin;
