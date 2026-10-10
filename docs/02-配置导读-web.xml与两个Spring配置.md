# 02 · 配置导读 · web.xml 与两个 Spring 配置

> 用途：**本实验的核心考核资料**。学生必须能讲清每个配置在干什么、并能自己改。
> 用法：上课逐段过一遍 → 课后对着"少一行会怎样"表自查
> 对应教材：ch06 §6.2.2（web.xml 部署 DispatcherServlet）、ch15 §15.3.4（配置文件）

---

## 一、先记住分工：四个文件，各管一摊

| 文件 | 谁读它 | 管什么 | 什么时候加载 |
|---|---|---|---|
| `webapp/WEB-INF/web.xml` | **Tomcat** | 装配 Filter、Listener、Servlet | Tomcat 启动，部署应用时 |
| `resources/spring/applicationContext.xml` | Spring **Root 容器** | Service、DAO、数据源 | `ContextLoaderListener` 触发，**先** |
| `resources/spring/springmvc-servlet.xml` | Spring **MVC 容器** | Controller、视图解析、静态资源 | `DispatcherServlet` 初始化，**后** |
| `resources/jdbc.properties` | 上面两个（P4/P5 起） | 数据库连接信息 | 被 `property-placeholder` 读取 |

一句话记忆：

> **`web.xml` 是"给 Tomcat 看的"，两个 Spring 配置是"给 Spring 看的"；
> 一个管 Service，一个管 Controller。**

---

## 二、web.xml 逐段读

```xml
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee" ... version="5.0">
```

**① 命名空间**：`jakarta.ee` 而不是老教程里的 `java.sun.com`。
这是 Tomcat 10 与 Tomcat 9 的分水岭 —— 教材 ch06 用的就是 Jakarta 版。

> 🔴 **考过的高频坑**：抄了老教程的 `java.sun.com/xml/ns/javaee` + `version="2.5"`，
> Tomcat 10 会直接拒绝部署（报 `IllegalArgumentException` 或直接 404）。

```xml
<filter>
  <filter-name>encodingFilter</filter-name>
  <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
  <init-param><param-name>encoding</param-name><param-value>UTF-8</param-value></init-param>
  <init-param><param-name>forceEncoding</param-name><param-value>true</param-value></init-param>
</filter>
<filter-mapping>...<url-pattern>/*</url-pattern></filter-mapping>
```

**② 编码过滤器**：解决 POST 提交的中文乱码。
`forceEncoding=true` 表示**响应也强制用 UTF-8**（不写它，页面可能乱码）。
`url-pattern` 是 `/*`（所有请求），不是 `/`。

> **动手验证**：把这段注释掉，用中文用户名登录 —— 页面上的中文会变成问号或乱码。

```xml
<listener>
  <listener-class>com.zjtec.admin.config.StartupSelfCheckListener</listener-class>
</listener>
```

**③ 启动自检**（本工程加的，拓展）：启动时打一份"体检报告"。见 README 第二节。

```xml
<context-param>
  <param-name>contextConfigLocation</param-name>
  <param-value>classpath:spring/applicationContext.xml</param-value>
</context-param>
<listener>
  <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
```

**④ Root 容器**：Tomcat 启动时创建**父容器**。
`ContextLoaderListener` 会去找 `classpath:spring/applicationContext.xml` 并加载它。
**这两段是配套的** —— 少了 `<context-param>`，它就会去找默认位置（`/WEB-INF/applicationContext.xml`）而找不到。

```xml
<servlet>
  <servlet-name>dispatcherServlet</servlet-name>
  <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
  <init-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:spring/springmvc-servlet.xml</param-value>
  </init-param>
  <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
  <servlet-name>dispatcherServlet</servlet-name>
  <url-pattern>/</url-pattern>
</servlet-mapping>
```

**⑤ DispatcherServlet（MVC 容器）**：所有请求的入口。

- `contextConfigLocation` 告诉它配置文件在哪（不写的话默认去 `WEB-INF/<servlet-name>-servlet.xml`）；
- `load-on-startup=1` = **随容器启动就初始化**。
  去掉它 → 第一次访问才初始化，第一次页面明显变慢（可以现场演示）；
- `url-pattern` 写 `/`（拦截所有请求，但不含 JSP），**不能写 `/*`**（那会把 JSP 也拦下，页面渲染会出问题）。

```xml
<welcome-file-list>
  <welcome-file>index.html</welcome-file>
</welcome-file-list>
```

**⑥ 欢迎页**：访问 `/zjtec-admin/` 时给哪个文件。

> ⚠️ **元素顺序不能随便调**。`web-app` 的子元素有严格顺序：
> `display-name → filter → filter-mapping → listener → servlet → servlet-mapping → welcome-file-list`。
> 写乱了 IDEA 会标红、Tomcat 可能直接拒绝部署。

---

## 三、applicationContext.xml（Root 容器）

```xml
<context:component-scan base-package="com.zjtec.admin.service"/>
```

**唯一一行，但很关键**：让 Spring 去 `com.zjtec.admin.service` 及其子包下找
`@Service` / `@Component` / `@Repository` 标注的类，实例化后放进容器。

有了它，`UserServiceImpl` 才成为 Bean；Controller 的 `@Autowired UserService` 才装配得上。

**没有它会怎样？**

```
NoSuchBeanDefinitionException:
  No qualifying bean of type 'com.zjtec.admin.service.UserService' available
```

**这个报错长什么样、怎么定位**，建议现场制造一次（把 `base-package` 改成 `com.zjtec.admin.xx`）。

### P5 会在这里加什么

整合 MyBatis 时，往下补三段（工程里已写好注释）：

1. `property-placeholder` 读 `jdbc.properties`
2. `dataSource`（DBCP2 连接池）
3. `sqlSessionFactory` + `MapperScannerConfigurer`

**同时要删掉 `mybatis-config.xml` 里的 `<environments>`**：
数据源的"产权"从 MyBatis 移交给 Spring，只能有一个主人。
两个都留着 → 数据源被重复定义，MyBatis 会用自己的连接池而绕过 Spring 的事务管理。

---

## 四、springmvc-servlet.xml（MVC 容器）

```xml
<context:component-scan base-package="com.zjtec.admin.web"/>
```

只扫 **Web 层**。与 Root 容器的扫描范围**刻意不重叠**（这是本工程最重要的设计）。

```xml
<mvc:annotation-driven/>
```

**注解驱动**。少了它：

- `@RequestMapping` 可能不生效 → 404
- `@ResponseBody` 不生效 → 返回 JSON 的接口会输出视图名而不是 JSON
- 各种参数绑定的默认行为会退化

> **动手验证**：注释掉这一行，重启，访问 `/login` 看 404。

```xml
<mvc:default-servlet-handler/>
```

把静态资源（css/js/图片）**交还给 Tomcat 的默认 Servlet**。

为什么需要它？因为 `<url-pattern>/</url-pattern>` 让 DispatcherServlet 接管了所有请求，
它找不到对应的 Controller 就 404 —— 连 `/static/css/app.css` 也一起 404。

> **动手验证**：注释掉这一行，重启，页面样式全丢。

```xml
<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
  <property name="prefix" value="/WEB-INF/jsp/"/>
  <property name="suffix" value=".jsp"/>
</bean>
```

**视图解析器**：把 Controller 返回的字符串拼成真实路径。

```
返回 "result"  →  /WEB-INF/  +  jsp/  +  result  +  .jsp
                = /WEB-INF/jsp/result.jsp
```

**前缀为什么是 `/WEB-INF/`？** 因为 `/WEB-INF/` 下的资源**浏览器不能直接访问**（访问会 404），
只能由服务端转发。这样视图就不会被绕过 Controller 直接打开。
可以现场演示：直接访问 `/zjtec-admin/WEB-INF/jsp/result.jsp` → 404。

---

## 五、为什么要有两个 Spring 容器（P3 最有价值的一问）

```
                    Tomcat
                      │
        ┌─────────────┴─────────────┐
        │  Root 容器（父）           │  ← ContextLoaderListener 创建
        │  applicationContext.xml   │
        │  管：Service / DAO / 数据源 │
        └─────────────┬─────────────┘
                      │ 子容器可以拿到父容器的 Bean
        ┌─────────────┴─────────────┐
        │  MVC 容器（子）            │  ← DispatcherServlet 创建
        │  springmvc-servlet.xml    │
        │  管：Controller / 视图     │
        └───────────────────────────┘
```

**规则一句话：子容器能看见父容器的 Bean，父容器看不见子容器的。**

- Controller 在子容器 → 能拿到父容器的 Service ✅（所以登录能用）
- Service 在父容器 → 拿不到子容器的 Controller ✅（本来也不该拿）

**为什么不合并成一个容器？**

1. **职责隔离**：Web 层配置（视图、静态资源、注解驱动）与业务层配置（数据源、事务）混在一个文件里，会越写越乱；
2. **P5 的落点**：SSM 整合时，数据源和 MyBatis 是往**父容器**加的，MVC 配置一行不用动；
3. **教材 ch15 就是这么分的**（`applicationContext.xml` + `springmvc.xml`），考试对照得上。

**扫描范围重叠会怎样？**

如果两个都写 `com.zjtec.admin`，Service 会被两个容器**各建一份**。
多数情况下不报错，但你会拿到两个不同的实例 —— 这在加了事务、加缓存之后
会变成"明明配了却不生效"的诡异 bug。**范围不重叠是纪律。**

---

## 六、jdbc.properties 与 logback.xml

### jdbc.properties（P4/P5 启用）

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/db_ssm?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=你的密码
```

四个参数各治一个坑（课堂上逐条问"去掉会怎样"）：

| 参数 | 去掉后的问题 |
|---|---|
| `characterEncoding=utf8` | 中文入库变乱码 |
| `serverTimezone=Asia/Shanghai` | 报时区错误 `The server time zone value ... is unrecognized` |
| `useSSL=false` | 实验室环境握手告警/失败 |
| `allowPublicKeyRetrieval=true` | MySQL 8.4 默认认证方式下报 `Public Key Retrieval is not allowed` |

两个易错点：

1. **properties 文件里的 `&` 直接写**，不要写成 `&amp;` —— 需要转义的是 XML，不是 properties；
2. **`Properties.load()` 用 ISO-8859-1 读取**，properties 文件里的中文严格来说需要转义
   （`\uXXXX`）。本工程只在注释和示例密码里出现中文，不影响解析；
   真实项目里建议 properties 内**不写中文**。

### logback.xml

关键就一行观念：**按包调级别**。

```xml
<logger name="com.zjtec.admin" level="DEBUG"/>      <!-- 自己的代码：看细节 -->
<logger name="org.springframework" level="INFO"/>   <!-- 框架：别刷屏 -->
```

排查"404 是谁吃了请求"时，临时把 `org.springframework.web` 调成 `DEBUG`，
就能看到 Spring 匹配了哪些 HandlerMapping —— 这比猜快得多。

---

## 七、自查表：少一行会怎样（建议当堂抽问）

| 删掉/改掉这个 | 会发生什么 | 属于哪类错误 |
|---|---|---|
| `web.xml` 的 `ContextLoaderListener` | Service 装配失败，启动报 `NoSuchBeanDefinitionException` | 响（启动即报） |
| `web.xml` 的 `CharacterEncodingFilter` | POST 中文乱码 | 哑（运行才现） |
| `web.xml` 的 `load-on-startup` | 首次访问明显变慢 | 不报错 |
| `web.xml` 的 `<welcome-file-list>` | 访问根路径 404 | 哑 |
| `dispatcherServlet` 的 `url-pattern` 改成 `/*` | JSP 也走 DispatcherServlet，页面渲染异常 | 响 |
| `applicationContext.xml` 的 `component-scan` | Service 找不到 | 响 |
| 扫描包写错成 `com.zjtec.admin.xx` | 同上 | 响 |
| `springmvc-servlet.xml` 的 `component-scan` | Controller 找不到 → 404 | 哑 |
| `<mvc:annotation-driven/>` | 映射失效 / `@ResponseBody` 不生效 | 哑偏响 |
| `<mvc:default-servlet-handler/>` | 静态资源 404，页面样式全丢 | 哑 |
| 视图解析器的 `prefix` 写错 | `FileNotFoundException`，找不到 JSP | 响 |
| `jdbc.properties` 的 `serverTimezone` | 连库报时区错误 | 响 |
| 把 css 放进 `WEB-INF/` | 浏览器取不到，样式丢 | 哑 |

> **"响"错误**（启动就炸）好排查，**"哑"错误**（跑到那一步才现）最耗时间。
> 这张表的用途不是背，而是教会学生一个判断：**出问题时先问"这是响的还是哑的"**，
> 响的去看启动日志，哑的去复现那一步的路径。

---

## 八、动手练习（课后自查）

1. 把 `LoginController` 的 `@PostMapping("/login")` 改成 `@GetMapping("/login")`，
   用表单提交会发生什么？为什么？
2. 把 `applicationContext.xml` 的扫描范围改成 `com.zjtec.admin.web`，重启观察报错全文，
   并把前 5 行抄进实验报告。
3. 视图解析器的 `prefix` 改成 `/jsp/`（去掉 WEB-INF），把 `result.jsp` 挪过去，
   重启后直接访问 `/zjtec-admin/jsp/result.jsp` —— 能访问吗？这说明什么问题？
4. 在 `springmvc-servlet.xml` 里加一行把 `org.springframework.web` 日志调成 DEBUG，
   重启后访问一次 `/login`，观察日志里 Spring 匹配了哪些 HandlerMapping。
