package com.zjtec.admin.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Controller 层测试（P3）—— 用 MockMvc 在<b>不启动 Tomcat</b> 的前提下验证 Web 层。
 *
 * <p><b>MockMvc 解决什么问题：</b>Web 层的错误（映射写错、参数没绑上、视图名拼错、
 * 忘加 @ResponseBody）传统上只能靠"启动 Tomcat → 打开浏览器 → 点点点"来发现，
 * 一次几十秒，还很容易漏掉失败分支。MockMvc 把"发请求、拿响应"变成一次方法调用，
 * 毫秒级完成，且可以断言到视图名和模型数据这一层。</p>
 *
 * <p><b>它不能替代什么：</b>JSP 的真实渲染、Tomcat 的部署、静态资源能否访问，
 * 这些仍要在容器里验证。所以本类通过 ≠ 工程一定能跑起来，
 * 但本类不通过，工程<b>一定</b>跑不起来。</p>
 *
 * <p><b>两个配置都加载：</b>{@code applicationContext.xml} 提供 Service，
 * {@code springmvc-servlet.xml} 提供 Controller —— 真实运行时它们是父子两个容器，
 * 测试时放进同一个上下文，效果等价（扫描范围互补，不冲突）。</p>
 */
@SpringJUnitWebConfig(locations = {
        "classpath:spring/applicationContext.xml",
        "classpath:spring/springmvc-servlet.xml"
})
class LoginControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // ==================================================================
    // 页面跳转
    // ==================================================================

    @Test
    @DisplayName("GET /login → 视图名 login（即 /WEB-INF/jsp/login.jsp）")
    void loginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    // ==================================================================
    // 方式一：@RequestParam
    // ==================================================================

    @Test
    @DisplayName("POST /login 账号密码正确 → 视图 result，模型带成功提示")
    void loginSuccess() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attribute("username", "admin"))
                .andExpect(model().attribute("way", "@RequestParam 逐参数接收"))
                .andExpect(model().attribute("msg", "登录成功，欢迎回来！"));
    }

    @Test
    @DisplayName("POST /login 密码错误 → 模型带失败提示（错误分支也要测）")
    void loginFailure() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attribute("msg", "用户名或密码错误"));
    }

    @Test
    @DisplayName("POST /login 缺少参数 → 400（@RequestParam 默认 required=true）")
    void loginMissingParam() throws Exception {
        mockMvc.perform(post("/login").param("username", "admin"))
                .andExpect(status().isBadRequest());
    }

    // ==================================================================
    // 方式二：POJO 自动绑定
    // ==================================================================

    @Test
    @DisplayName("POST /loginByPojo → 参数被绑进 User 对象，视图 result")
    void loginByPojoBinding() throws Exception {
        mockMvc.perform(post("/loginByPojo")
                        .param("username", "admin")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attribute("username", "admin"))
                .andExpect(model().attribute("msg", "登录成功，欢迎回来！"));
    }

    // ==================================================================
    // 选做：JSON（@RequestBody + @ResponseBody）
    // ==================================================================

    @Test
    @DisplayName("POST /api/login → 返回 JSON，且不回传明文密码")
    void apiLoginReturnsJson() throws Exception {
        String body = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(body.contains("\"code\":200"), "响应应包含 code:200，实际 = " + body);
        assertTrue(body.contains("\"username\":\"admin\""), "响应应包含用户名，实际 = " + body);
        assertFalse(body.contains("\"password\":\"123\""), "响应不应回传明文密码，实际 = " + body);
    }

    @Test
    @DisplayName("POST /api/login 密码错误 → 返回 code 500 的失败响应")
    void apiLoginFailure() throws Exception {
        String body = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"bad\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(body.contains("\"code\":500"), "响应应包含 code:500，实际 = " + body);
        assertTrue(body.contains("用户名或密码错误"), "响应应包含失败提示，实际 = " + body);
    }
}
