package com.zjtec.admin.config;

import com.zjtec.admin.common.AppInfo;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 启动自检：应用启动时在日志里打一份"体检报告"。
 *
 * <p><b>为什么要有这个类：</b>实验课上最耽误时间的不是写代码，
 * 而是"跑不起来又不知道哪一步错了"。有了启动自检，只要看日志前几行，
 * 就能立刻判断出：环境对不对、配置有没有被加载、数据库通不通。</p>
 *
 * <p><b>P4 起多了一件事：真连一次数据库。</b>
 * 页面报 500 和"数据库连不上"之间隔着好几层堆栈，学生要翻很久才定位到根因；
 * 启动时直接连一次并给出明确结论，能把排查时间从十分钟压到十秒。</p>
 *
 * <p><b>对照 ASP.NET：</b>相当于 ASP.NET Core 启动时的
 * {@code ILogger} 启动日志 + {@code HealthChecks} 的"启动期"部分。</p>
 *
 * <p>注册方式：web.xml 里的 {@code <listener>}（没有用 {@code @WebListener} 注解，
 * 是为了让学生能在 web.xml 里直接看到它 —— 配置可见优先于少写两行）。</p>
 */
public class StartupSelfCheckListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(StartupSelfCheckListener.class);

    /** 数据库连接超时（秒）。避免 MySQL 没启动时启动过程长时间卡住 */
    private static final int DB_LOGIN_TIMEOUT_SECONDS = 3;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        log.info("");
        log.info("==================== {} 启动自检 ====================", AppInfo.APP_NAME);
        log.info("  当前里程碑    : {}", AppInfo.APP_STAGE);
        log.info("  Java 版本     : {} ({})", System.getProperty("java.version"), System.getProperty("java.home"));
        log.info("  操作系统      : {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        log.info("  Servlet 规范  : {}.{}", ctx.getMajorVersion(), ctx.getMinorVersion());

        String contextPath = ctx.getContextPath();
        log.info("  上下文路径    : {}", contextPath.isEmpty() ? "(ROOT)" : contextPath);
        if (!AppInfo.CONTEXT_PATH.equals(contextPath)) {
            log.warn("  ^ 提示：与文档约定的 {} 不一致，检查 IDEA 里 Deployment 的 Application context",
                    AppInfo.CONTEXT_PATH);
        }

        Properties jdbc = loadJdbcProperties();
        if (jdbc != null) {
            checkDatabase(jdbc);
        }

        log.info("  访问入口      : 登录页 http://localhost:8080{}/login", contextPath);
        log.info("                  员工页 http://localhost:8080{}/employees  （P4 新增）", contextPath);
        log.info("  自检完成：看到这一行，说明 web.xml 与两个 Spring 配置都已被容器加载。");
        log.info("========================================================");
        log.info("");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("{} 已停止。", AppInfo.APP_NAME);
    }

    /**
     * 读取 jdbc.properties。
     *
     * <p>P3 阶段它"存在但无人使用"，所以只做 INFO 提示；
     * P4 起 MyBatis 真正读它，这里顺便把目标库名显示出来，
     * 方便确认"配置读到了、读的是哪个库"。</p>
     */
    private Properties loadJdbcProperties() {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("jdbc.properties")) {

            if (in == null) {
                log.warn("  jdbc.properties: 未找到（P4 起需要，请确认它在 src/main/resources 下）");
                return null;
            }

            Properties props = new Properties();
            props.load(in);
            log.info("  jdbc.properties: 已加载（目标库 {}）",
                    parseDatabaseName(props.getProperty("jdbc.url", "")));
            return props;

        } catch (Exception e) {
            log.warn("  jdbc.properties: 读取失败 - {}", e.getMessage());
            return null;
        }
    }

    /**
     * 真连一次数据库，把结论直接写进启动日志。
     *
     * <p><b>注意：连不上只告警，绝不让应用启动失败。</b>
     * 因为 P3 的功能（SpringMVC 登录）本来就不依赖数据库 ——
     * 数据库没配好，不该连登录页都进不去。
     * 这也是一条通用原则：<b>可选依赖的健康检查，不应该阻断主体功能。</b></p>
     */
    private void checkDatabase(Properties props) {
        String driver = props.getProperty("jdbc.driver", "").trim();
        String url = props.getProperty("jdbc.url", "").trim();
        String username = props.getProperty("jdbc.username", "").trim();
        String password = props.getProperty("jdbc.password", "");

        // 最常见的第一种情况：密码还是模板里的占位符
        if (password == null || password.isBlank() || password.contains("你的密码")) {
            log.warn("  数据库连通    : 未检测 —— jdbc.properties 的密码还是占位符「你的密码」");
            log.warn("                  ↳ 改成你本机 MySQL 的密码后重启即可");
            return;
        }

        try {
            // 显式加载驱动类：Web 容器里类加载器是隔离的，
            // 不能完全指望 JDBC 4 的 SPI 自动注册，显式加载最稳
            Class.forName(driver);
            DriverManager.setLoginTimeout(DB_LOGIN_TIMEOUT_SECONDS);

            try (Connection conn = DriverManager.getConnection(url, username, password)) {

                log.info("  数据库连通    : OK（MySQL {} / 库 {}）",
                        conn.getMetaData().getDatabaseProductVersion(),
                        conn.getCatalog());

                // 顺带确认建表脚本跑过没有 —— "库连上了但表不存在"是第二种常见情况
                if (!tableExists(conn, "employee")) {
                    log.warn("                  ↳ 但表 employee 不存在，请先执行 db/schema.sql");
                }
            }

        } catch (ClassNotFoundException e) {
            log.warn("  数据库连通    : 失败 —— 找不到驱动类 {}", driver);
            log.warn("                  ↳ 检查 pom.xml 里 mysql-connector-j 依赖是否已引入");

        } catch (SQLException e) {
            log.warn("  数据库连通    : 失败 —— {}", e.getMessage());
            log.warn("                  ↳ 排查顺序：① MySQL 服务是否启动 ② 密码是否正确 "
                    + "③ db_ssm 库是否已建（执行 db/schema.sql）");
        }
    }

    /** 表存在性检查。查不动就算了（返回 true），不再往上叠一层噪音日志 */
    private boolean tableExists(Connection conn, String table) {
        try (ResultSet rs = conn.getMetaData()
                .getTables(conn.getCatalog(), null, table, null)) {
            return rs.next();
        } catch (SQLException e) {
            return true;
        }
    }

    /**
     * 从 JDBC URL 中取出库名，只打印库名、绝不打印密码。
     *
     * <p><b>这里踩过一个坑，值得记下来：</b>最初写的是
     * {@code url.substring(url.lastIndexOf('/') + 1, url.indexOf('?'))}，
     * 想当然认为"最后一个斜杠就是库名前面那个"。但连接串里带了
     * {@code serverTimezone=Asia/Shanghai} —— <b>时区参数里也有斜杠</b>，
     * 于是 {@code lastIndexOf('/')} 定位到了 Asia/Shanghai，
     * 算出的起点跑到终点后面，启动时抛
     * {@code Range [94, 34) out of bounds for length 144}。</p>
     *
     * <p>正确做法是：先跳到 {@code //} 之后（越过协议部分），
     * 再找第一个 {@code /} —— 那才是主机与库名的分界。
     * 这也解释了为什么"解析 URL"这类活儿，尽量不要靠"最后一个/第一个"
     * 这种位置假设，而要按结构定位。</p>
     */
    private String parseDatabaseName(String url) {
        if (url == null || url.isEmpty()) {
            return "(未配置)";
        }
        int afterScheme = url.indexOf("//");
        int dbSlash = afterScheme < 0 ? -1 : url.indexOf('/', afterScheme + 2);
        int query = url.indexOf('?');
        if (dbSlash < 0 || query < 0 || query <= dbSlash + 1) {
            return "(未能解析)";
        }
        return url.substring(dbSlash + 1, query);
    }
}
