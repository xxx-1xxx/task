/**
 * 持久层（Mapper）—— 实验四启用。
 *
 * <h3>现在这里有什么</h3>
 * <pre>
 *   mapper/
 *   └─ EmployeeMapper.java        员工增删改查 + 关联查询（P4 核心）
 * </pre>
 * 配套的映射文件在
 * {@code src/main/resources/com/zjtec/admin/mapper/EmployeeMapper.xml}
 * —— 注意<b>接口与 XML 同包</b>（只是分别位于 {@code java/} 和 {@code resources/}
 * 两个源根下），这是 MyBatis 的惯例，找起来不费劲。
 *
 * <h3>Mapper 接口的写法约定</h3>
 * <pre>
 * public interface EmployeeMapper {
 *     List&lt;Employee&gt; findAll();          // 方法名 = 映射 XML 里的 &lt;select id="findAll"&gt;
 *     Employee findById(int id);
 *     int insert(Employee e);
 *     int update(Employee e);
 *     int deleteById(int id);
 * }
 * </pre>
 * 接口里<b>不写任何实现</b>，SQL 全在映射 XML 里；MyBatis 在运行时
 * 用动态代理生成实现类。接口全限定名必须与 XML 的 {@code namespace} 一字不差，
 * 否则报 {@code Invalid bound statement (not found)}。
 *
 * <h3>P4 阶段谁在调用它</h3>
 * <pre>
 *   TestMyBatis（main 方法）      手动 new SqlSessionFactory，用来调试 SQL
 *   EmployeeServlet（/employees） 每个请求 new 工厂 —— 能跑，但有三个真问题
 * </pre>
 * 上面两者的共同点是：<b>全在 new</b>。到实验五，Mapper 的代理对象会由 Spring
 * 自动创建并注入 Service，业务代码里只剩一句 {@code @Autowired}，
 * 再也看不到 SqlSession。
 *
 * @see com.zjtec.admin.mapper.EmployeeMapper P4 的核心接口
 * @see com.zjtec.admin.web.EmployeeServlet P4 留下的对照样本（反面教材）
 */
package com.zjtec.admin.mapper;
