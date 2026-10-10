package com.zjtec.admin.mapper;

import com.zjtec.admin.entity.Employee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工数据访问接口 —— 实验四的核心交付物。
 *
 * <p><b>接口里没有一行实现，它是怎么跑起来的？</b>
 * 这是 MyBatis 最值得讲清的一点：</p>
 * <ol>
 *   <li>本接口的全限定名 {@code com.zjtec.admin.mapper.EmployeeMapper}，
 *       <b>必须与 {@code EmployeeMapper.xml} 里 {@code <mapper namespace="...">} 的值完全一致</b>；</li>
 *   <li>接口里每个方法名，对应 XML 里一个 {@code <select>} / {@code <insert>} /
 *       {@code <update>} / {@code <delete>} 的 {@code id}；</li>
 *   <li>调用 {@code session.getMapper(EmployeeMapper.class)} 时，
 *       MyBatis 用 JDK 动态代理生成一个实现类，方法一调就去找同名的 SQL 执行。</li>
 * </ol>
 * <p>所以 namespace 写错、或者方法名与 {@code id} 对不上，报的错是
 * {@code Invalid bound statement (not found): com.zjtec.admin.mapper.EmployeeMapper.xxx}
 * —— 看到这个错误，先去核对这两处，不用怀疑 SQL 本身。</p>
 *
 * <p><b>本项目的方法与实验指导书完全一致</b>（findAll / findById / insert /
 * update / deleteById），末尾多一个 {@link #findByCondition}，用于演示
 * 大纲第 (4) 条"参数的传递方式"与动态 SQL。它标为拓展，不计入考核。</p>
 */
public interface EmployeeMapper {

    /** 查全部员工，<b>并级联查出部门名</b>（走 resultMap 的 association） */
    List<Employee> findAll();

    /** 按主键查单个员工。这是<b>单表查询</b>——返回的 Employee 里 dept 是 null */
    Employee findById(int id);

    /** 新增，返回受影响行数 */
    int insert(Employee e);

    /** 修改，返回受影响行数 */
    int update(Employee e);

    /** 按主键删除，返回受影响行数 */
    int deleteById(int id);

    /**
     * 【拓展 · 不计入考核】按条件查询，两个参数都可为 null。
     *
     * <p>演示两件事：</p>
     * <ul>
     *   <li><b>多个参数的传递方式</b>——方法参数超过一个时，MyBatis 无法靠参数名取到值，
     *       必须用 {@code @Param("名字")} 指定，SQL 里才能写 {@code #{名字}}。</li>
     *   <li><b>动态 SQL</b>——映射文件里用 {@code <where>} + {@code <if>}，
     *       条件传了才拼进 WHERE，没传就不拼。这是"一个方法应对多种查询"的标准做法。</li>
     * </ul>
     *
     * @param deptId 部门 id，可为 null（不限部门）
     * @param gender 性别，可为 null（不限性别）
     */
    List<Employee> findByCondition(@Param("deptId") Integer deptId,
                                   @Param("gender") String gender);
}
