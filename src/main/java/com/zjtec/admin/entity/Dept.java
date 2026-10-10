package com.zjtec.admin.entity;

/**
 * 部门实体 —— 对应 {@code db_ssm.dept} 表。
 *
 * <p><b>它在实验四里的角色：</b>被 {@link Employee} 通过映射文件的
 * {@code <association>} 关联进来。员工列表页上那一列"研发部 / 人事部"，
 * 就是它的 {@code name}。</p>
 *
 * <p><b>为什么属性用 {@code Integer} 而不是 {@code int}：</b>
 * 表的 id 列虽然不会为 NULL，但换成"关联列可能查不到"的场景，
 * {@code int} 表达不了"没有值"——它会静默变成 0，
 * 于是出现"id = 0 的部门"这种不报错、但对不上的脏数据。
 * 包装类型里 null 就是 null，一眼能看出来。这是本项目统一用包装类型的原因。</p>
 */
public class Dept {

    private Integer id;
    private String name;

    /** MyBatis 反射建对象要用无参构造 —— 少了它，查询直接抛 InstantiationException */
    public Dept() {
    }

    public Dept(Integer id, String name) {
        this.id = id;
        this.name = name;
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

    @Override
    public String toString() {
        return "Dept{id=" + id + ", name='" + name + "'}";
    }
}
