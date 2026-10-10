package com.zjtec.admin.common;

/**
 * 应用级常量：把容易写错的"魔法字符串"集中到一处。
 *
 * <p>对照 ASP.NET：相当于把 appsettings.json 里读出来的值封装成一个
 * {@code AppSettings} 静态类，业务代码里不出现裸字符串。</p>
 */
public final class AppInfo {

    /** 应用名（与 pom.xml 的 finalName 一致） */
    public static final String APP_NAME = "zjtec-admin";

    /** 当前里程碑：P4 = 装上 MyBatis 数据层（P3 Web 层 → P4 数据层 → P5 整合 → P6 注册登录） */
    public static final String APP_STAGE = "P4 · MyBatis 员工查询";

    /** 部署后的访问前缀（Tomcat 中 Deployment → Application context 填的值） */
    public static final String CONTEXT_PATH = "/zjtec-admin";

    /** 登录演示账号（P3 用模拟校验；P5 起改为查 user 表） */
    public static final String DEMO_USERNAME = "admin";
    public static final String DEMO_PASSWORD = "123";

    private AppInfo() {
        // 常量类，禁止实例化
    }
}
