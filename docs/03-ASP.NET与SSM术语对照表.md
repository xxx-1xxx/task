# 03 · ASP.NET 与 SSM 术语对照表

> 面向：有 ASP.NET / ASP.NET Core 后端背景的教师与学生
> 用途：① 授课教师快速建立两者映射；② 课堂上用学生可能听过的概念做类比
> 对应教材：ch06–ch15（SpringMVC + MyBatis + SSM 整合）

---

## 开发前先记住一句总纲

| | ASP.NET Core | SSM（本课程） |
|---|---|---|
| 一句话 | "**约定优于配置**，一切在 `Program.cs` 里用代码装配" | "**配置显式可见**，装配写在 XML + 注解里" |
| 配置载体 | `appsettings.json` + C# 代码 | `jdbc.properties` + XML + 注解 |
| 依赖来源 | NuGet 包引用 | Maven 依赖（`pom.xml`） |
| 启动入口 | `Program.cs` / `Startup.cs` | `web.xml` + 容器（Tomcat） |

**核心认知差异**：ASP.NET Core 里"某个服务在哪注册"经常要翻 `Program.cs` 才知道；
SSM 里"某层被谁管"直接写在 XML 的 `component-scan` 上，**看得见**。
这也是本课程坚持用 XML 的原因之一 —— 对初学者，可见的配置比隐式的约定更好理解。

---

## 一、分层与命名

| 概念 | ASP.NET / ASP.NET Core | SSM（本课程） | 本工程位置 |
|---|---|---|---|
| 处理 HTTP 请求 | `Controller`（MVC） / Minimal API | `@Controller` 类 | `web/LoginController` |
| Action 方法 | `public IActionResult Login(...)` | `@RequestMapping` 标注的方法 | `login()` / `loginByPojo()` |
| 业务逻辑层 | `Service` 类 | `@Service` 类（接口 + impl） | `service/UserService` + `impl/UserServiceImpl` |
| 数据访问层 | `Repository` / `DbContext` | `@Mapper` 接口 + 映射 XML | `mapper/`（P4 启用） |
| 数据模型 | `Model` / `Entity` | 实体类 | `entity/User` |
| 传输对象 | `DTO` / `ViewModel` | `DTO` | `dto/`（P6 引入） |
| 依赖注册容器 | `IServiceCollection`（DI 容器） | Spring IoC 容器（`ApplicationContext`） | `applicationContext.xml` |

---

## 二、依赖注入（DI）

| 概念 | ASP.NET Core | Spring | 说明 |
|---|---|---|---|
| 注册服务 | `builder.Services.AddScoped<IUserService, UserService>()` | `@Service` 注解 + `component-scan` | Spring 靠扫描，不用逐个登记 |
| 构造注入 | `public Ctor(IUserService svc)` — **默认方式** | 单构造器自动注入（Spring 4.3+） | 本工程 `LoginController` 用的就是这个 |
| 字段/属性注入 | 需第三方或 `[FromServices]` | `@Autowired` 标在字段上 | **教材与实验手册用的是这种**；考试按教材答 |
| 按名注入 | 具名注册 + `GetService<T>("name")` | `@Resource(name="...")` / `@Qualifier` | `@Autowired` 认类型，`@Resource` 认名字 |
| 生命周期 | `AddSingleton` / `AddScoped` / `AddTransient` | `singleton`（默认） / `prototype` / `request` | Spring 默认单例，**这是必须讲的一条** |
| 循环依赖 | 构造函数循环 → 直接编译/启动报错 | 构造注入报错；Setter 注入被三级缓存悄悄解开 | 教材/课件有专门加餐 Demo |

> ⚠️ **最容易踩的一条**：Spring 默认所有 Bean 都是**单例**。
> 所以 Service 里不要存"每次请求独有的状态"（比如把当前登录用户塞成字段），
> 否则会串用户。ASP.NET 里 `AddScoped` 的直觉在这里不成立。

---

## 三、请求处理链路

```
ASP.NET Core :
  HTTP → Kestrel → Middleware 管道 → Routing → Controller.Action → IActionResult

SSM（本课程）:
  HTTP → Tomcat → Filter(CharacterEncodingFilter) → DispatcherServlet
       → HandlerMapping → Controller 方法 → 视图名 → ViewResolver → JSP
```

| 环节 | ASP.NET Core | SSM | 本工程位置 |
|---|---|---|---|
| 前端控制器 | `EndpointRoutingMiddleware` | `DispatcherServlet` | `web.xml` |
| 路由 | `[Route("login")]` / 约定路由 | `@RequestMapping("/login")` | `LoginController` |
| HTTP 方法限定 | `[HttpGet]` / `[HttpPost]` | `@GetMapping` / `@PostMapping` | 同左 |
| 过滤器 | Middleware | `Filter`（Servlet 规范）或 `HandlerInterceptor` | `web.xml` 的 `encodingFilter` |
| 视图渲染 | Razor `.cshtml` | JSP `.jsp` + JSTL | `/WEB-INF/jsp/*.jsp` |

---

## 四、参数绑定（P3 重点）

| 场景 | ASP.NET Core | SSM | 本工程示例 |
|---|---|---|---|
| 查字符串参数 | `[FromQuery] string name` | `@RequestParam("name") String name` | `login()` |
| 表单字段 | `[FromForm]` | 无需注解，直接形参即可 | `login()` |
| 整体绑定到对象 | `[FromForm] LoginForm f` | 形参直接写 `User user`（**自动逐字段 set**） | `loginByPojo()` |
| 请求体 JSON | `[FromBody] LoginDto dto` | `@RequestBody` | `apiLogin()` |
| 路径变量 | `[FromRoute] int id`（`{id}`） | `@PathVariable("id")` | P4 会用到 |
| 参数必填 | 模型验证 `[Required]` → `ModelState` | `@RequestParam(required=false)` / 教材 ch10 的 JSR 380 | — |

> **课堂类比**：`@RequestBody` 之于 SSM，相当于 `[FromBody]`；
> 而"形参直接写 `User user`"这种自动绑定，ASP.NET 里对应的是 `[FromForm]` 绑定复杂对象 ——
> 区别是 Spring MVC 连注解都可以省。

---

## 五、返回值与响应（P3 重点）

| 想做的事 | ASP.NET Core | SSM | 本工程示例 |
|---|---|---|---|
| 返回一个页面 | `return View("Result");` | `return "result";`（逻辑视图名） | `login()` |
| 页面 + 数据 | `View(model)` | `ModelMap` 加属性 / `ModelAndView` | 两种各用一次 |
| 返回 JSON | `return Ok(obj);` / `Json(obj)` | `@ResponseBody` + 返回对象 | `apiLogin()` |
| 重定向 | `RedirectToAction("Login")` | `return "redirect:/login";` | — |
| 统一响应包装 | `ApiResponse<T>` / `Ok(new {...})` | `Result<T>`（**拓展**） | `common/Result.java` |

> ⚠️ **最容易混淆的一点**：`@Controller` 的方法返回一个字符串，
> 它不是"响应内容"，而是**逻辑视图名**；视图解析器会拼成
> `prefix + "result" + suffix` = `/WEB-INF/jsp/result.jsp`。
> 在 ASP.NET 里 `return View("Result")` 也是找视图，这一点直觉是相通的；
> 但 `return Content("ok")` 的直觉在这里会让人误以为字符串直接输出 —— 那是 `@ResponseBody` 的行为。

---

## 六、配置与启动

| 做什么 | ASP.NET Core | SSM | 本工程位置 |
|---|---|---|---|
| 应用入口/装配 | `Program.cs` | `web.xml` | `webapp/WEB-INF/web.xml` |
| 连接字符串 | `appsettings.json` 的 `ConnectionStrings` | `jdbc.properties` | `resources/jdbc.properties` |
| 读配置 | `IConfiguration["Key"]` | `<context:property-placeholder>` + `${key}` | P5 启用 |
| 业务容器注册 | `builder.Services.*` | `applicationContext.xml` | Root 容器 |
| Web 层注册 | `builder.Services.AddControllers()` | `springmvc-servlet.xml` + `mvc:annotation-driven` | MVC 容器 |
| 环境区分（dev/prod） | `appsettings.Development.json` | 多个 properties + Spring Profile（**本课程不讲**） | — |
| 静态文件 | `app.UseStaticFiles()` | `mvc:default-servlet-handler` | `springmvc-servlet.xml` |

---

## 七、横切关注点

| 关注点 | ASP.NET Core | SSM | 课程定位 |
|---|---|---|---|
| 日志 | `ILogger<T>`（内置） | SLF4J + Logback | 本工程已配（拓展） |
| 统一异常处理 | `app.UseExceptionHandler(...)` | `@ControllerAdvice` + `@ExceptionHandler` | **教材 ch12，大纲外** |
| 模型验证 | `[Required]` + `ModelState.IsValid` | JSR 380（`@NotNull` 等）+ `BindingResult` | **教材 ch10，大纲外** |
| 认证/授权 | `[Authorize]` + 中间件 | `HandlerInterceptor` + session | **教材 ch09，大纲外** |
| 国际化 | `IStringLocalizer` / `.resx` | `MessageSource` + `.properties` | **教材 ch11，大纲外** |
| 事务 | `TransactionScope` / `IDbContextTransaction` | `@Transactional`（教材 ch05） | **大纲外（治理决定：可讲可不讲）** |

> 这一栏对照的价值在于：**学生将来无论走 .NET 还是 Java 路线，
> 这套"横切关注点"清单是通用的** —— 只是名字不同、写法不同，职责完全对应。
> 但要守住红线：ch09–ch13 属大纲外，本课程主线不展开。

---

## 八、数据访问（P4/P5 用）

| 概念 | ASP.NET Core | MyBatis（本课程） |
|---|---|---|
| 访问入口 | `DbContext` / `Dapper` 的 `Query<T>` | `SqlSession` / Mapper 接口 |
| 查询定义 | LINQ 表达式 | XML 里的 `<select>` + SQL |
| 参数占位 | `@p0` / 命名参数 | `#{name}`（预编译，防注入） |
| 结果映射 | 属性名约定 / `[Column]` | `resultType`（约定）或 `resultMap`（手动，**关联查询必须用**） |
| 关联查询 | `.Include(x => x.Dept)` | `<association>` / `<collection>` |
| 事务提交 | `SaveChanges()` | `SqlSession.commit()` / Spring 管理后自动提交 |
| 连接池 | 内置 | DBCP2（本工程 P5 用 `BasicDataSource`） |

---

## 九、一张"三行记住"的速查卡（可做成 PPT 一页）

| 你熟悉的 | 这里的对应物 |
|---|---|
| `[HttpGet("login")] + IActionResult` | `@GetMapping("/login")` + 返回视图名字符串 |
| `[FromBody] dto` / `[FromQuery] x` | `@RequestBody` / `@RequestParam` |
| `appsettings.json` 连接串 | `jdbc.properties` |
| `AddScoped<IX, X>()` | `@Service` + `component-scan`（**注意默认单例**） |
| `UseStaticFiles()` | `mvc:default-servlet-handler` |
| `ILogger<T>` | `LoggerFactory.getLogger(X.class)` |
| `UseExceptionHandler` | `@ControllerAdvice`（教材 ch12，大纲外） |

---

> **教学提示**：这份表**不必逐条讲**。建议只在三处用得上：
> ① P3 讲 `@RequestParam` / `@RequestBody` 时提一次"对应你将来可能见到的 `[FromQuery]`/`[FromBody]`"；
> ② P3 讲视图名时类比 `return View("Result")`；
> ③ P5 讲容器与单例时，强调"这里没有 `AddScoped` 的概念，默认就是单例，所以 Service 不能存请求级状态"。
> 其余留给学生自己看 —— 有兴趣的人会来问，没兴趣的人讲了也记不住。
