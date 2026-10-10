package com.zjtec.admin.entity;

import java.io.Serializable;

/**
 * 用户实体：对应数据库的 {@code user} 表（P5 起）。
 *
 * <p><b>P3 阶段的用途：</b>演示 SpringMVC 的「POJO 接收请求参数」——
 * 表单里 {@code name="username"} 与这里的属性名一致，
 * Controller 方法形参直接写 {@code User user}，Spring 就会自动把
 * 请求参数一个个 set 进去，不用写一堆 {@code @RequestParam}。</p>
 *
 * <p><b>表结构（P5 用 HeidiSQL 建）：</b></p>
 * <pre>
 * CREATE TABLE user (
 *   id       INT PRIMARY KEY AUTO_INCREMENT,
 *   username VARCHAR(50)  NOT NULL UNIQUE,
 *   password VARCHAR(100) NOT NULL,
 *   email    VARCHAR(100)
 * );
 * </pre>
 *
 * <p><b>为什么必须有无参构造 + 全部 getter/setter：</b>
 * MyBatis 用反射创建对象再 set 值，Jackson 反序列化 JSON 也走 setter。
 * 少一个 setter，那个字段就永远是 null —— 而且不报错，属于"哑"错误。</p>
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String username;
    private String password;
    private String email;

    /** 无参构造：MyBatis / Jackson 反射创建对象时要用 */
    public User() {
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 注意：{@code toString()} 里**不输出密码**。
     * 日志里一旦打了明文密码，就等于把密码写进了日志文件，这是真实项目中的事故。
     */
    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}
