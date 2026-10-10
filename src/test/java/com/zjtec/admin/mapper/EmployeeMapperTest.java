package com.zjtec.admin.mapper;

import com.zjtec.admin.entity.Employee;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 实验四的单元测试 —— 10 个用例，全部围绕"容易写错的地方"设计。
 *
 * <p><b>为什么这个测试类要写成"能自动跳过"：</b>
 * 它依赖真实数据库（{@code db_ssm}）。如果某台机器没建库，
 * 测试应当<b>跳过</b>而不是<b>失败</b> —— 否则"环境没配好"会被误读成"代码写错了"，
 * 把人引到错误的方向。这就是 JUnit 5 {@code Assumptions}（假设）的用途：
 * 假设不成立就跳过，不判定为失败。</p>
 *
 * <p>想在本机真正跑起来，两步：① 执行 {@code db/schema.sql} 建库建表；
 * ② 把 {@code jdbc.properties} 里的密码改成本机的。</p>
 *
 * <p>本类中带"反例"字样的断言，是在验证<b>错误的东西不会被悄悄放过</b> ——
 * 只测成功路径，是实验报告最常见的失分点。</p>
 */
@DisplayName("实验四 · EmployeeMapper（需先建 db_ssm 库）")
class EmployeeMapperTest {

    private static SqlSessionFactory factory;

    /** 非 null 表示环境不可用，所有用例跳过 */
    private static String skipReason;

    @BeforeAll
    static void initFactory() {
        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            factory = new SqlSessionFactoryBuilder().build(in);
        } catch (Throwable t) {
            skipReason = "跳过（mybatis-config.xml 读不进来或配置写错）："
                    + t.getClass().getSimpleName();
            dumpSkipReason(t);
            return;
        }

        // ⚠️ 这里**只验证"能不能连上库"**，故意不调用任何 Mapper 方法。
        //
        // 为什么？"环境没配好"和"SQL 写错了"是两回事，必须分开报：
        //   · 连不上库      → 跳过（不是你的代码问题，别把人引偏）
        //   · SQL 写错了    → 失败（就是你写错了，必须看见红）
        // 早先的写法在这里调了一次 findAll() 探活，结果实验四的 TODO 还没做时，
        // SQL 报错会被当成"连不上库"而把 12 个用例全部跳过 ——
        // 明明是红色的错误被显示成灰色的"跳过"，反馈就废了。
        try (SqlSession s = factory.openSession()) {
            try (Connection c = s.getConnection()) {
                c.getMetaData().getDatabaseProductVersion();   // 真拉一次元数据
            }
        } catch (Throwable t) {
            skipReason = "跳过（连不上数据库或 db_ssm 未建库）：" + t.getClass().getSimpleName()
                    + " —— 请先执行 db/schema.sql，并在 jdbc.properties 里填本机密码。";
            dumpSkipReason(t);
        }
    }

    /** 报告里必须能看见"为什么跳过"。只留一句 skipReason 不够 —— 真正能定位的是这个堆栈。 */
    private static void dumpSkipReason(Throwable t) {
        System.out.println();
        System.out.println("########## " + skipReason);
        t.printStackTrace(System.out);
        System.out.println();
    }

    @BeforeEach
    void requireDatabase() {
        Assumptions.assumeTrue(skipReason == null, skipReason);
    }

    // ================================================================
    //  1. 关联查询（实验四的核心考核点）
    // ================================================================

    @Test
    @DisplayName("findAll：每行都能带出部门名 —— association 是否生效的判定")
    void findAll_carriesDeptName() {
        try (SqlSession s = factory.openSession()) {

            List<Employee> list = s.getMapper(EmployeeMapper.class).findAll();

            assertFalse(list.isEmpty(), "表里应有测试数据，请先执行 db/schema.sql");

            long withDept = list.stream()
                    .filter(e -> e.getDept() != null && e.getDept().getName() != null)
                    .count();

            assertEquals(list.size(), withDept,
                    "应每一行都装配出部门对象；有行为空说明 resultMap 的 <association> 没生效");

            // 反例：全空是最典型的"关联查询写成了单表查询"
            assertTrue(withDept > 0, "部门名全为空 → findAll 退化成单表查询了");
        }
    }

    @Test
    @DisplayName("findAll：外键列 deptId 与关联对象 dept.id 应当一致")
    void findAll_deptIdConsistentWithAssociation() {
        try (SqlSession s = factory.openSession()) {

            for (Employee e : s.getMapper(EmployeeMapper.class).findAll()) {
                if (e.getDept() != null) {
                    // 反例：resultMap 里若把 association 的 column 写错，
                    //       会装出一个"id 对不上"的部门对象，页面显示看似正常、其实串了
                    assertEquals(e.getDeptId(), e.getDept().getId(),
                            "外键列与关联对象主键不一致 → 检查 resultMap 里 dept_id / dept_name 的别名");
                }
            }
        }
    }

    @Test
    @DisplayName("findAll：结果按 id 升序（ORDER BY 生效）")
    void findAll_orderedById() {
        try (SqlSession s = factory.openSession()) {

            List<Employee> list = s.getMapper(EmployeeMapper.class).findAll();

            for (int i = 1; i < list.size(); i++) {
                assertTrue(list.get(i - 1).getId() < list.get(i).getId(),
                        "第 " + i + " 条 id 没比前一条大 → ORDER BY 没生效或结果被重排");
            }
        }
    }

    // ================================================================
    //  2. 单表查询 —— 用"断言 dept 为 null"把知识点钉死
    // ================================================================

    @Test
    @DisplayName("findById：单表查询 → dept 必须为 null（这是知识点，不是 bug）")
    void findById_hasNoAssociation() {
        try (SqlSession s = factory.openSession()) {

            Employee e = s.getMapper(EmployeeMapper.class).findById(1);

            assertNotNull(e, "id=1 应存在，请先执行 db/schema.sql");
            assertEquals(1, e.getId().intValue());
            assertNotNull(e.getDeptId(), "外键列 dept_id 应当有值");

            // 反例断言：这条最容易被人当成 bug"顺手修掉"
            assertNull(e.getDept(),
                    "findById 是单表查询，dept 不该有值。若不为 null，说明 SQL 被改成了关联查询 —— "
                            + "那就失去了与 findAll 对照的意义");
        }
    }

    @Test
    @DisplayName("findById：查不到的 id 返回 null（而不是抛异常）")
    void findById_notFoundReturnsNull() {
        try (SqlSession s = factory.openSession()) {

            // 反例：很多初学者以为"查不到 = 出错"，于是把 null 当异常抛。
            //       MyBatis 的语义是返回 null，调用方负责判空。
            assertNull(s.getMapper(EmployeeMapper.class).findById(999999),
                    "查不到应返回 null；若抛异常说明 SQL 里用了 getOne 之类的非空语义");
        }
    }

    // ================================================================
    //  3. 动态 SQL（拓展内容，不计入考核）
    // ================================================================

    @Test
    @DisplayName("findByCondition(部门, null)：只拼第一个 if，结果全属该部门")
    void findByCondition_deptIdOnly() {
        try (SqlSession s = factory.openSession()) {

            List<Employee> list = s.getMapper(EmployeeMapper.class).findByCondition(1, null);

            assertFalse(list.isEmpty(), "研发部（id=1）应有员工");
            assertTrue(list.stream().allMatch(e -> Integer.valueOf(1).equals(e.getDeptId())),
                    "结果里混进了别的部门 → <if> 的判断写错了");
            assertTrue(list.stream().allMatch(e -> e.getDept() != null),
                    "带条件查询也应同时走关联映射");
        }
    }

    @Test
    @DisplayName("findByCondition(null, 性别)：只拼第二个 if")
    void findByCondition_genderOnly() {
        try (SqlSession s = factory.openSession()) {

            List<Employee> list = s.getMapper(EmployeeMapper.class).findByCondition(null, "女");

            assertFalse(list.isEmpty(), "应有女性员工");
            assertTrue(list.stream().allMatch(e -> "女".equals(e.getGender())),
                    "结果里混进了别的性别 → 第二个 <if> 的判断写错了");
        }
    }

    @Test
    @DisplayName("findByCondition(null, null)：WHERE 整段不出现，等同于 findAll")
    void findByCondition_noCondition() {
        try (SqlSession s = factory.openSession()) {

            EmployeeMapper m = s.getMapper(EmployeeMapper.class);

            assertEquals(m.findAll().size(), m.findByCondition(null, null).size(),
                    "两个条件都不传时应返回全部 —— <where> 会自动省掉整个 WHERE");
        }
    }

    @Test
    @DisplayName("findByCondition(null, 空串)：空串同样不拼进 SQL")
    void findByCondition_blankGenderIgnored() {
        try (SqlSession s = factory.openSession()) {

            EmployeeMapper m = s.getMapper(EmployeeMapper.class);

            // 反例：若少了 gender != '' 这半句，会拼出 "AND gender = ''"，
            //       结果是 0 条 —— 页面看起来"没数据"，实际是条件写错了
            assertEquals(m.findAll().size(), m.findByCondition(null, "").size(),
                    "性别传空串应视作不传；若为 0 条，说明 <if> 里少了 gender != '' 这一半");
        }
    }

    // ================================================================
    //  4. 增删改 —— 插进去的数据都在 finally 里清掉，可反复运行
    // ================================================================

    @Test
    @DisplayName("insert：影响 1 行，且自增主键回填到入参对象")
    void insert_backfillsGeneratedKey() {
        try (SqlSession s = factory.openSession(true)) {

            EmployeeMapper m = s.getMapper(EmployeeMapper.class);

            Employee neo = new Employee();
            neo.setName("单元测试员");
            neo.setGender("女");
            neo.setDeptId(3);

            assertNull(neo.getId(), "插入前主键应为 null —— 这正是「回填」要证明的前提");

            int rows = m.insert(neo);

            try {
                assertEquals(1, rows, "应影响 1 行");
                assertNotNull(neo.getId(),
                        "主键没回填 → 检查 insert 上的 useGeneratedKeys / keyProperty");

                // 反例：回填的 id 必须真能在库里查到，否则只是"假回填"
                Employee back = m.findById(neo.getId());
                assertNotNull(back, "回填的主键在库里查不到 → keyProperty 指错了属性名");
                assertEquals("单元测试员", back.getName());
            } finally {
                if (neo.getId() != null) {
                    m.deleteById(neo.getId());
                }
            }
        }
    }

    @Test
    @DisplayName("update：改完之后查得到新值")
    void update_changesValue() {
        try (SqlSession s = factory.openSession(true)) {

            EmployeeMapper m = s.getMapper(EmployeeMapper.class);

            Employee neo = new Employee();
            neo.setName("改名前");
            neo.setGender("男");
            neo.setDeptId(1);
            m.insert(neo);

            try {
                neo.setName("改名后");
                neo.setDeptId(2);

                assertEquals(1, m.update(neo), "应影响 1 行");

                Employee back = m.findById(neo.getId());
                assertEquals("改名后", back.getName(), "姓名没更新");
                assertEquals(2, back.getDeptId().intValue(), "部门没更新");
            } finally {
                if (neo.getId() != null) {
                    m.deleteById(neo.getId());
                }
            }
        }
    }

    @Test
    @DisplayName("deleteById：删完查不到；重复删除返回 0 行而不报错")
    void deleteById_removesRow() {
        try (SqlSession s = factory.openSession(true)) {

            EmployeeMapper m = s.getMapper(EmployeeMapper.class);

            Employee neo = new Employee();
            neo.setName("待删除");
            neo.setGender("男");
            neo.setDeptId(1);
            m.insert(neo);

            assertEquals(1, m.deleteById(neo.getId()), "首次删除应影响 1 行");
            assertNull(m.findById(neo.getId()), "删除后不该再查到");

            // 反例：重复删除是"幂等"的 —— 返回 0 而不是抛异常。
            //       业务代码要能处理这个 0（比如提示"记录已不存在"）。
            assertEquals(0, m.deleteById(neo.getId()), "重复删除应影响 0 行");
        }
    }
}
