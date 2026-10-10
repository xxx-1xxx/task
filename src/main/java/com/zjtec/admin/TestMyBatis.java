package com.zjtec.admin;

import com.zjtec.admin.entity.Employee;
import com.zjtec.admin.mapper.EmployeeMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;

/**
 * 实验四步骤 5 的入口：用 {@code main} 方法直连数据库，跑一遍 MyBatis。
 *
 * <p><b>为什么先在 main 里跑，而不是直接进 Web？</b>
 * 因为这一层还没有 Spring 参与，报错直白、能单步调试。
 * 先在最小环境里把 MyBatis 本身跑通，再把偶发问题留给 Web 环境 ——
 * 这是排查问题的基本次序：<b>一次只引入一个不确定因素</b>。</p>
 *
 * <p>在 IDEA 里直接右键 → Run 'TestMyBatis.main()' 即可（不用起 Tomcat）。</p>
 *
 * <p>本类会依次演示：关联查询、单表查询的差别、动态 SQL、主键回填。
 * 为不污染数据库，最后会把演示新增的那条数据删掉，<b>可以反复运行</b>。</p>
 */
public class TestMyBatis {

    public static void main(String[] args) throws Exception {

        // ① 读全局配置 → 建工厂
        //    try-with-resources：in 用完自动关，避免文件句柄泄漏
        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);

            // ② openSession(true) = 自动提交。
            //    不传 true 时是手动事务，增删改必须自己 session.commit()，
            //    否则数据"插了却查不到"——这是新手最常见的困惑之一。
            try (SqlSession session = factory.openSession(true)) {

                EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);

                System.out.println("=== ① 关联查询 findAll()：员工带部门名 ===");
                List<Employee> all = mapper.findAll();
                for (Employee e : all) {
                    System.out.println("    " + e.getName() + " → " + e.getDept().getName());
                }

                System.out.println();
                System.out.println("=== ② 单表查询 findById(1)：注意 dept 是 null ===");
                Employee one = mapper.findById(1);
                System.out.println("    " + one);
                System.out.println("    ↳ deptId=" + one.getDeptId() + " 有值（外键列），"
                        + "但 dept=" + one.getDept() + "（没写关联查询，装配不出来）");

                System.out.println();
                System.out.println("=== ③ 动态 SQL findByCondition(1, null)：只按部门过滤 ===");
                for (Employee e : mapper.findByCondition(1, null)) {
                    System.out.println("    " + e.getName() + " · " + e.getGender()
                            + " · " + e.getDept().getName());
                }
                System.out.println("    ↳ 第二个参数传 null，SQL 里那条 <if> 就不会拼进 WHERE");

                System.out.println();
                System.out.println("=== ④ 新增 insert()：验证主键回填 ===");
                Employee neo = new Employee();
                neo.setName("测试员");
                neo.setGender("女");
                neo.setDeptId(3);

                int rows = mapper.insert(neo);
                System.out.println("    受影响行数 = " + rows);
                System.out.println("    回填的主键 = " + neo.getId()
                        + "   ← insert 之前它是 null，keyProperty 把数据库生成的值写回来了");

                System.out.println();
                System.out.println("=== ⑤ 删除清理：把刚才那条演示数据删掉 ===");
                System.out.println("    删除行数 = " + mapper.deleteById(neo.getId()));
                System.out.println("    当前员工总数 = " + mapper.findAll().size());

                System.out.println();
                System.out.println("全部跑通 ✅  —— 现在可以照实验四步骤 6 把它接进 Web 项目了。");
            }
        }
    }
}
