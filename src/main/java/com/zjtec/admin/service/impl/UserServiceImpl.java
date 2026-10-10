package com.zjtec.admin.service.impl;

import com.zjtec.admin.common.AppInfo;
import com.zjtec.admin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 用户业务实现（P3 版：模拟数据库校验）。
 *
 * <p><b>大纲原文要求</b>：实验三「编写方法模拟数据库验证用户名和密码，
 * 从而模拟开发登录功能」—— 这个类就是那个"模拟数据库"。</p>
 *
 * <p><b>@Service 是什么：</b>告诉 Spring「启动时把这个类实例化，放进容器」。
 * 配合 applicationContext.xml 里的 {@code <context:component-scan base-package="com.zjtec.admin.service"/>}
 * 生效。少了扫描配置，注解就是白加 —— 这是最常见的启动期报错之一
 * （{@code NoSuchBeanDefinitionException}），属于"响"错误，好排查。</p>
 *
 * <p><b>这个类在后续实验怎么变：</b></p>
 * <pre>
 *   P3（现在）：没有依赖，用常量比对
 *   P5        ：注入 UserMapper（@Autowired），改成查 user 表
 *               → 那时才需要关心"字段注入 vs 构造注入"
 * </pre>
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * P3 的"数据库"就是 AppInfo 里的那一对演示常量，
     * 所以此刻"已存在的用户"只有 admin 一个。
     */
    @Override
    public boolean login(String username, String password) {
        log.debug("登录校验（模拟库）：username={}", username);

        boolean matched = AppInfo.DEMO_USERNAME.equals(username)
                && AppInfo.DEMO_PASSWORD.equals(password);

        log.debug("校验结果：{}", matched ? "通过" : "用户名或密码不匹配");
        return matched;
    }

    /**
     * P3 阶段：只有演示账号算"已存在"。
     * P6 注册功能会把它换成 {@code userMapper.findByUsername(username) != null}。
     */
    @Override
    public boolean exists(String username) {
        return AppInfo.DEMO_USERNAME.equals(username);
    }
}
