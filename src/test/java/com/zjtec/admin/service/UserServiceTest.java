package com.zjtec.admin.service;

import com.zjtec.admin.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Service 层单元测试（P3）。
 *
 * <p><b>为什么业务层最值得写单测：</b>Service 里是"规则"，
 * 规则会变、也最容易写错。这个测试类不启动 Spring、不连数据库，
 * 直接 new 出实现类就能跑 —— 毫秒级，改一次代码点一下就知道有没有坏。</p>
 *
 * <p>对照 ASP.NET：相当于对业务服务的 xUnit 测试，
 * 用直接实例化替代 DI 容器，测的是逻辑而不是装配。</p>
 *
 * <p><b>注意"负向断言"：</b>下面一大半用例测的是"应该失败"的情况。
 * 只测正确路径的测试，价值有限 —— 真实 bug 几乎都藏在错误分支里。</p>
 */
class UserServiceTest {

    private final UserService userService = new UserServiceImpl();

    @Test
    @DisplayName("账号密码都正确 → 校验通过")
    void loginWithCorrectCredentials() {
        assertTrue(userService.login("admin", "123"));
    }

    @Test
    @DisplayName("密码错误 → 校验不通过")
    void loginWithWrongPassword() {
        assertFalse(userService.login("admin", "wrong-password"));
    }

    @Test
    @DisplayName("用户名不存在 → 校验不通过")
    void loginWithUnknownUser() {
        assertFalse(userService.login("zhangsan", "123"));
    }

    @Test
    @DisplayName("用户名为 null → 不抛异常，直接返回 false")
    void loginWithNullUsername() {
        // 实现里用的是 AppInfo.DEMO_USERNAME.equals(username)：
        // 常量在前，null 也安全。反过来写成 username.equals(...) 这里就会 NPE。
        assertFalse(userService.login(null, "123"));
    }

    @Test
    @DisplayName("密码为 null → 不抛异常，直接返回 false")
    void loginWithNullPassword() {
        assertFalse(userService.login("admin", null));
    }

    @Test
    @DisplayName("用户名大小写敏感 → Admin 不等于 admin")
    void usernameIsCaseSensitive() {
        assertFalse(userService.login("Admin", "123"));
    }

    @Test
    @DisplayName("exists：演示账号判定为已存在")
    void existsForDemoUser() {
        assertTrue(userService.exists("admin"));
    }

    @Test
    @DisplayName("exists：其他用户名判定为不存在")
    void existsForOtherUser() {
        assertFalse(userService.exists("zhangsan"));
    }
}
