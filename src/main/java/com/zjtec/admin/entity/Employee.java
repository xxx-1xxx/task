package com.zjtec.admin.entity;

/**
 * 员工实体 —— 对应 {@code db_ssm.employee} 表。
 *
 * <p><b>⚠️ 它有两个"部门"属性，这是实验四最容易混的地方：</b></p>
 * <ul>
 *   <li>{@link #deptId}（{@code Integer}）—— 对应表里的<b>外键列</b> {@code dept_id}。
 *       任何查询都会有值（只要 SQL 选了这一列）。</li>
 *   <li>{@link #dept}（{@link Dept}）—— <b>关联对象</b>，由映射文件里
 *       {@code resultMap} 的 {@code <association>} 从关联列填进来。
 *       <b>只有写了关联查询才会有值</b>。</li>
 * </ul>
 *
 * <p>于是同一个类在两种查询下表现不同：</p>
 * <pre>
 *   findAll()  → 关联查询，deptId 和 dept 都有值   → 页面能显示"研发部"
 *   findById() → 单表查询，dept 是 null            → 页面显示部门那格是空的
 * </pre>
 * <p>这个差别是考核点：<b>想在页面上显示部门名，就必须写关联查询</b>，
 * 而不是简单地 {@code SELECT * FROM employee}。</p>
 */
public class Employee {

    private Integer id;
    private String name;
    private String gender;

    /** 外键列 dept_id */
    private Integer deptId;

    /** 关联对象：只有关联查询才会填，单表查询时是 null */
    private Dept dept;

    /** MyBatis 反射建对象要用无参构造 */
    public Employee() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public Dept getDept() {
        return dept;
    }

    public void setDept(Dept dept) {
        this.dept = dept;
    }

    /** 打印时把关联部门名一起带上，调试时一眼能看出关联查询生效没有 */
    @Override
    public String toString() {
        return "Employee{id=" + id + ", name='" + name + "', gender='" + gender
                + "', deptId=" + deptId
                + ", dept=" + (dept == null ? "null（未做关联查询）" : dept.getName())
                + "}";
    }
}
