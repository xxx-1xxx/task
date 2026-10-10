package com.zjtec.admin.web;

import com.zjtec.admin.common.Result;
import com.zjtec.admin.entity.User;
import com.zjtec.admin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 登录控制器 —— 实验三的核心交付物。
 *
 * <p><b>本类覆盖大纲实验三的全部要点</b>（第 (3)(4)(5) 条）：</p>
 * <table border="1">
 *   <caption>大纲要求 → 本类对应实现</caption>
 *   <tr><th>大纲要求</th><th>对应方法</th><th>用到的技术</th></tr>
 *   <tr><td>用 SpringMVC 实现登录功能</td><td>{@link #login}</td><td>@Controller + @PostMapping</td></tr>
 *   <tr><td>获取请求数据（RequestParam 方式）</td><td>{@link #login}</td><td>@RequestParam</td></tr>
 *   <tr><td>获取请求数据（POJO 方式）</td><td>{@link #loginByPojo}</td><td>POJO 自动绑定</td></tr>
 *   <tr><td>使用模型数据处理响应数据</td><td>{@link #login} / {@link #loginByPojo}</td><td>ModelMap / ModelAndView</td></tr>
 *   <tr><td>（选做）处理 JSON 数据</td><td>{@link #apiLogin}</td><td>@RequestBody + @ResponseBody</td></tr>
 * </table>
 *
 * <p><b>为什么 Controller 里没有一句 SQL、没有一句业务判断：</b>
 * Controller 的职责只有三件事 —— 接参数、调 Service、选视图。
 * 校验逻辑在 {@link UserService}，将来换成查库时本类一行都不用改。
 * 这条"分层边界"是实验三和实验五都会考的点。</p>
 */
@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    /** 依赖的 Service：由 Spring 注入，Controller 不自己 new */
    private final UserService userService;

    /**
     * 构造注入：Spring 4.3+ 单构造器可省略 {@code @Autowired}，会自动装配。
     *
     * <p>教材与实验手册用的是「字段注入」（{@code @Autowired} 标在字段上），
     * 两种都能跑通、考试答哪种都对。这里用构造注入是因为它有两个实际好处：
     * 依赖可以声明为 {@code final} 防止被改；依赖缺失时<b>启动就报错</b>，
     * 而不是运行到那一行才抛空指针。</p>
     */
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    // ==================================================================
    // ① 进入登录页
    // ==================================================================

    /**
     * GET /login → 显示登录表单。
     *
     * <p>返回的字符串 {@code "login"} 不是文件名，而是<b>逻辑视图名</b>：
     * 视图解析器用 prefix + 名字 + suffix 拼成 {@code /WEB-INF/jsp/login.jsp}。</p>
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // ==================================================================
    // ② 方式一：@RequestParam 逐参数接收
    // ==================================================================

    /**
     * POST /login → 校验登录，结果交给 result.jsp 渲染（整页刷新）。
     *
     * <p>{@code @RequestParam("username")} 的意思是：从请求参数里取名为
     * {@code username} 的那个值，赋给形参。形参名与参数名一致时可省略括号里的字符串。</p>
     *
     * <p>参数没传会怎样？默认 {@code required = true}，直接返回 400。
     * 想让参数可选，写 {@code @RequestParam(required = false)} 或给默认值。</p>
     */
    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        ModelMap model) {

        log.debug("收到登录请求 [@RequestParam 方式] username={}", username);

        boolean success = userService.login(username, password);

        // ModelMap = 给视图准备数据的"袋子"，视图里用 ${msg} 取出来
        model.addAttribute("msg", success ? "登录成功，欢迎回来！" : "用户名或密码错误");
        model.addAttribute("username", username);
        model.addAttribute("way", "@RequestParam 逐参数接收");

        return "result";
    }

    // ==================================================================
    // ③ 方式二：POJO 自动绑定（参数多了以后更省事）
    // ==================================================================

    /**
     * POST /loginByPojo → 同样的登录，改用 POJO 接收参数。
     *
     * <p><b>怎么绑上的：</b>表单里 {@code name="username"}、{@code name="password"}
     * 与 {@link User} 的属性名一致，Spring 就用无参构造创建 User，
     * 再逐个调 setter 把值塞进去。方法形参直接写 {@code User user} 即可。</p>
     *
     * <p><b>什么时候该用 POJO：</b>参数超过 3～4 个、或表单字段与某个实体高度重合时。
     * 参数两三个的时候，{@code @RequestParam} 反而更直白。</p>
     *
     * <p>本例同时演示第二种响应方式 {@link ModelAndView}：把"视图名"和"数据"
     * 装在同一个对象里返回，适合需要在方法内动态决定跳哪个页面的场景。</p>
     */
    @PostMapping("/loginByPojo")
    public ModelAndView loginByPojo(User user) {

        log.debug("收到登录请求 [POJO 绑定方式] {}", user);

        boolean success = userService.login(user.getUsername(), user.getPassword());

        ModelAndView mv = new ModelAndView("result");
        mv.addObject("msg", success ? "登录成功，欢迎回来！" : "用户名或密码错误");
        mv.addObject("username", user.getUsername());
        mv.addObject("way", "POJO 自动绑定（" + User.class.getSimpleName() + " 对象接收）");
        return mv;
    }

    // ==================================================================
    // ④ 选做：返回 JSON（实验六的 AJAX 会用到同一套写法）
    // ==================================================================

    /**
     * POST /api/login → 返回 JSON 而不是跳页面（大纲实验三「选做：处理JSON数据」）。
     *
     * <p><b>两个注解各管一半：</b></p>
     * <ul>
     *   <li>{@code @RequestBody}：把请求体里的 JSON 反序列化成 {@link User} 对象（进）</li>
     *   <li>{@code @ResponseBody}：把返回的对象序列化成 JSON 写回响应（出）</li>
     * </ul>
     * <p>有了这两个注解，Spring 会自动找 Jackson 来干活 —— 所以 pom.xml 里
     * 必须有 {@code jackson-databind}。少了它，会报
     * {@code HttpMediaTypeNotAcceptableException}。</p>
     *
     * <p><b>怎么测：</b>浏览器地址栏只能发 GET，测不了这个接口。
     * 用 Apifox / Postman，或浏览器按 F12 在 Console 里执行：</p>
     * <pre>
     * fetch('/zjtec-admin/api/login', {
     *   method: 'POST',
     *   headers: {'Content-Type': 'application/json'},
     *   body: JSON.stringify({username: 'admin', password: '123'})
     * }).then(r =&gt; r.json()).then(console.log)
     * </pre>
     *
     * <p><b>一个真实项目的细节：</b>返回前把密码置空。哪怕是登录成功的响应，
     * 也没有任何理由把密码回传给前端 —— 这是习惯，不是形式。</p>
     */
    @PostMapping("/api/login")
    @ResponseBody
    public Result<User> apiLogin(@RequestBody User user) {

        log.debug("收到登录请求 [JSON 方式] username={}", user.getUsername());

        if (!userService.login(user.getUsername(), user.getPassword())) {
            return Result.fail("用户名或密码错误");
        }

        user.setPassword(null);          // 响应中不返回密码
        return Result.ok("登录成功", user);
    }
}
