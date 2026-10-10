package com.zjtec.admin.service;

/**
 * 用户业务接口（P3 起）。
 *
 * <p><b>为什么用接口而不是直接写实现类：</b>Controller 只认这个接口，
 * 不知道背后是"模拟校验"还是"查 MySQL"。所以 P5 把实现从写死改成查库时，
 * <b>Controller 一行都不用改</b> —— 这就是面向接口编程的实际收益，
 * 也是实验二 IoC 那个"换实现只改配置"的性质在 Web 工程里的延续。</p>
 *
 * <p>对照 ASP.NET：相当于 {@code IUserService} 接口 + DI 注入，
 * Controller 依赖 {@code IUserService} 而不是 {@code UserService} 具体类。</p>
 */
public interface UserService {

    /**
     * 校验用户名与密码。
     *
     * <p><b>实现演化路线（同一个方法，三种实现）：</b></p>
     * <pre>
     *   P3：与常量比对（写死 admin/123）      ← 现在
     *   P5：查 user 表（SELECT ... WHERE username = ?）
     *   P6：不变（注册是新增的另一个方法）
     * </pre>
     *
     * @param username 用户名
     * @param password 密码
     * @return 校验通过返回 true
     */
    boolean login(String username, String password);

    /**
     * 判断用户名是否已被注册。
     *
     * <p>P3/P5 阶段不涉及（返回实现由后续实验补），P6 注册功能需要用它
     * 拒绝重复用户名。这里先声明，是因为"注册前必须判重"这条业务规则
     * 属于业务层的职责 —— UI 层不该自己拼这个判断。</p>
     *
     * @param username 待检查的用户名
     * @return 已存在返回 true
     */
    boolean exists(String username);
}
