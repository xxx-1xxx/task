package com.zjtec.admin.web;

import com.zjtec.admin.entity.Employee;
import com.zjtec.admin.mapper.EmployeeMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 员工列表 Servlet —— 实验四步骤 6 的产物：把 MyBatis 接进 Web 项目。
 *
 * <p><b>⚠️ 这是一个"刻意的反面教材"，是留给实验五的对照样本，请勿删改。</b>
 * 它能跑、能出数据，但写法有三个真问题：</p>
 *
 * <ol>
 *   <li><b>每个请求都新建 SqlSessionFactory</b>（见下面那行 <code>new SqlSessionFactoryBuilder()</code>）。
 *       工厂内部持有一个 POOLED 连接池，本该<b>整个应用只建一次</b>、活到应用关闭。
 *       每请求新建 = 每次都在建连接池，旧的那些没人关 → <b>连接泄漏</b>；
 *       几十人同时点，数据库就开始拒绝连接。</li>
 *
 *   <li><b>SqlSession 不是线程安全的</b>。本例每次请求都新建 session（还算安全），
 *       但一旦有人图省事把它提成成员变量共享，就会出现数据串行、
 *       session 被提前关闭等<b>极难复现</b>的偶发怪错。</li>
 *
 *   <li><b>一切都是 new 出来的，无法替换</b>。工厂在类里硬编码 new，
 *       单元测试没法换成假数据源，只能靠手工点页面验证。</li>
 * </ol>
 *
 * <p><b>实验五会怎么改：</b>把 MyBatis 的创建权交给 Spring ——
 * {@code SqlSessionFactoryBean} 让工厂变成单例、随应用启停；
 * {@code MapperScannerConfigurer} 自动为 Mapper 接口生成代理并注入 Service，
 * 业务代码里从此不再出现 SqlSession。本类届时会被 {@code EmployeeController} 取代。</p>
 *
 * <p><b>一句话记住：凡是 new 出来的地方，都是应该交给容器的地方。</b></p>
 */
@WebServlet("/employees")
public class EmployeeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(EmployeeServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        log.debug("收到 /employees 请求（实验四写法：每个请求新建一个 SqlSessionFactory）");

        // ⚠️ 问题就在这一行：工厂本该由容器建一次，这里却每个请求都建
        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {

            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);

            // try-with-resources 确保 session 一定被关闭 —— 这一层是写对的
            try (SqlSession session = factory.openSession()) {

                List<Employee> list = session.getMapper(EmployeeMapper.class).findAll();
                log.debug("查询到 {} 条员工记录", list.size());

                req.setAttribute("list", list);
                req.getRequestDispatcher("/WEB-INF/jsp/employee-list.jsp").forward(req, resp);
            }
        }
    }
}
