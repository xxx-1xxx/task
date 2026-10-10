# zjtec-admin · 实验四学生版（P4）

> 《Java后台应用项目开发》2627.1　｜　实验四：MyBatis 框架的使用（8 学时）
> 这是**实验四的标准起点**：实验三的成果（双容器 + 登录）已经全部完成，
> 实验四要加的东西留了 **5 个 TODO** 等你填。

---

## 〇、先用哪一种起点？

| 你的情况 | 建议 |
|---|---|
| 实验三**已经跑通**（`/login` 能打开、能登录） | **两种都行**。继续用你自己的工程最省事；改用这一份也完全可以，它会省掉"实验三遗留的小毛病"带来的干扰 |
| 实验三**没跑通**，或换机器/重装了 IDEA | **用这一份**。先确认 `http://localhost:8080/zjtec-admin/login` 能打开，再开始实验四 |

> ⚠️ 不管你选哪一种，**从实验四开始，工程名、包名、数据库名都不许再变**：
> `zjtec-admin` / `com.zjtec.admin` / `db_ssm`，一路用到实验六。

---

## 一、先跑起来（约 8 分钟）

> ⚠️ **第一次跑，你会看到"跳过"或"报错"，这是设计好的。**
> TODO 还没填，程序当然跑不通。请不要一上来就怀疑环境。

1. **建库**（实验四步骤 1）：用 HeidiSQL 新建查询，执行工程根目录下 `db/schema.sql`
   （建 `db_ssm` 库、`dept` 与 `employee` 两张表，并造 3 个部门、5 名员工）。
2. **改密码**：打开 `src/main/resources/jdbc.properties`，把
   `jdbc.password=你的密码` 改成**你本机 MySQL 的真实密码**。
   > 这个文件里的 `&` 直接写，**不用**写成 `&amp;` —— 只有 XML 里才要转义。
3. **用 IDEA 打开本工程**：`File → Open` → 选中本文件夹（有 `pom.xml` 那一层）→ 等依赖下载完。
4. **先跑 `TestMyBatis`**（不用起 Tomcat）：在 IDEA 里右键 `src/main/java/com/zjtec/admin/TestMyBatis.java` → `Run 'TestMyBatis.main()'`。
   - 这是实验四**最重要的调试入口**：报错直白、能单步、能看 SQL 日志。
   - **在这一步把 MyBatis 跑通再往下走**，不要着急接 Web。
5. **跑单元测试**：右键 `src/test/java/com/zjtec/admin/mapper/EmployeeMapperTest.java` → `Run 'EmployeeMapperTest'`。
   - 一开始是**红的**（12 条里会挂一批）—— 那就是 TODO 还没填。
6. **最后才配 Tomcat**（照实验手册实验三 步骤 5）：
   - `Run → Edit Configurations → + → Tomcat Server → Local`
   - `Deployment` → `+` → `Artifact` → 选 `zjtec-admin:war exploded`
   - **Application context 填 `/zjtec-admin`**
7. 启动后访问 **`http://localhost:8080/zjtec-admin/employees`**，看到**带部门名**的员工列表即成功。

---

## 二、哪些是给你的，哪些要你写

### ✅ 已经给你的（不用改，改了会跑不起来）

| 文件 | 作用 |
|---|---|
| `pom.xml` | 依赖清单（Spring 6.2 + SpringMVC + **MyBatis 3.5.14** + MySQL 驱动 + JSP/JSTL + Jackson + 日志） |
| `db/schema.sql` | 建库建表脚本（和实验手册一致） |
| `src/main/webapp/WEB-INF/web.xml` | 部署描述符，**配了两个容器**，重点读注释 |
| `src/main/resources/spring/*.xml` | 两个 Spring 容器配置（实验三已完成，**本实验不动**） |
| `src/main/resources/logback.xml` | 日志配置；SQL 会打到控制台与 `logs/` |
| `src/main/java/.../entity/Dept.java`、`Employee.java` | 实体类。**注意 `Employee` 同时有 `deptId`（外键列）和 `dept`（关联对象）**，这两个不是一回事，注释里写清了 |
| `src/main/java/.../mapper/EmployeeMapper.java` | Mapper **接口**（**不要改**，它是"契约"；改动它会导致 `.xml` 里的 `id` 对不上） |
| `src/main/java/.../TestMyBatis.java` | `main` 直连演示：关联查询 / 单表查询 / 动态 SQL / 主键回填，**跑完自动清理**，可反复运行 |
| `src/main/java/.../web/EmployeeServlet.java` | **反面教材**，每个请求都 `new` 一个工厂。三个毛病写在类注释里 —— **留着别删，实验五要用它对照** |
| `src/main/webapp/WEB-INF/jsp/employee-list.jsp` | 员工列表页（JSTL `<c:forEach>` + `${e.dept.name}`） |
| `src/main/java/.../config/StartupSelfCheckListener.java` | 启动自检；P4 起会**真连一次 MySQL** 并在日志里给结论 |
| `src/test/java/.../mapper/EmployeeMapperTest.java` | **12 条单元测试**，每条都对着一个容易写错的地方。**先跑一次红，填完变绿** |
| `com/zjtec/admin/mapper/EmployeeMapper.xml` 里的 `withDeptColumns`、`deleteById` | **两个样例**，照它们的样子写 |

### ★ 要你写的（5 个 TODO，全部在 2 个文件里）

| 编号 | 文件 | 做什么 | 难度 |
|---|---|---|---|
| **TODO-1** | `resources/mybatis-config.xml` | 填 `<dataSource>` 的四要素（用 `${jdbc.*}` 引用 `jdbc.properties`） | ★ |
| **TODO-2** | `EmployeeMapper.xml` → `findById` | 写**单表查询**（`resultType`） | ★ |
| **TODO-3** | `EmployeeMapper.xml` → `insert` + `update` | 写增、改两条语句；`insert` **再加两个属性实现主键回填** | ★★ |
| **TODO-4** | `EmployeeMapper.xml` → `employeeMap` 的 `<association>` **+** `findAll` | 写**关联查询**（`LEFT JOIN` + 把部门列装配成 `Dept` 对象）—— **本实验最重要的一条** | ★★★ |
| **TODO-5**<br>（选做） | `EmployeeMapper.xml` → `findByCondition` | 写**动态 SQL**（`<where>` + `<if>`） | ★★★ |

> 💡 **怎么找 TODO**：IDEA 里 `Ctrl + Shift + F` 搜 `TODO-`，全部列出来。
> 每个 TODO 上方都写清了"要做什么、怎么判对、验收标准"，**先读注释再动手**。

> ⚠️ 没填的 TODO 不是留空，而是填了一句**故意的错 SQL**（比如 `SELECT TODO-2_单表查询还没写`）。
> 这不是 bug —— 它会让数据库报错信息里**直接出现 TODO 编号**，告诉你卡在哪一项，
> 和实验三学生版里 `throw new UnsupportedOperationException("TODO-x")` 是同一个思路。

---

## 三、这个工程的知识主线

实验四要理解的核心是 **"SQL 查出来的宽表，怎么变成 Java 对象"**：

```
     MySQL 两张表                    SQL 查出来的"宽表"
  ┌─ employee ─┐                  id  name  gender  dept_id  dept_name
  │ id         │  LEFT JOIN       1   张三   男       1       研发部
  │ name       │  ─────────►      2   李四   女       2       人事部
  │ gender     │  dept_id = id
  │ dept_id ───┼──┐
  └────────────┘  │              resultMap 负责把"宽表"拆回去：
  ┌─ dept ─────┐  │
  │ id    ◄────┼──┘              员工那 4 列 ──► Employee 的属性
  │ name       │                 部门那 2 列 ──► new 一个 Dept，塞进 employee.dept
  └────────────┘                   └─ 这一步就是 <association> 干的
```

三条要能自己说清的话：

1. **`resultType` 只能"列名 → 属性名"一对一映射**。遇到"要把两列组装成一个对象"就无能为力 ——
   这就是关联查询**必须手写 `resultMap`** 的根本原因。
2. `mapUnderscoreToCamelCase` 能替你处理 `dept_id → deptId`，但**它只管列名与属性名**，
   管不了"装配出 `dept` 这个对象"。
3. **`dept_id` 在两处出现是正常的**：一处是员工表的外键列（`Employee.deptId`），
   一处是关联对象的身份（`Dept.id`）。`findById` 只填前者、`findAll` 两者都填 —— 这正是两者的区别。

> 这条主线是实验五的地基：实验五要做的事，就是把这些 SQL 的**创建权**从 MyBatis 手里收归 Spring。

---

## 四、出问题先查这里

| 现象 | 八成是这个原因 |
|---|---|
| 测试全部**跳过**（Skipped，不是 Failed） | 数据库没连上：密码还是占位符、MySQL 没启动、或 `db_ssm` 库没建。**看控制台里 `##########` 那行的堆栈，它会说清是哪一种** |
| `Invalid bound statement (not found): ...EmployeeMapper.xxx` | XML 的 `namespace` 与接口全限定名不一致，或方法名与 `<select id="...">` 对不上，或 `mybatis-config.xml` 的 `<mappers>` 没注册 |
| 报错里出现 `Unknown column 'TODO-2_...'` 之类 | **正常**：这就是 TODO 没填。按它给的行号去填 |
| 员工列表"所属部门"列全是空的 | `<association>` 没写，或 `column` 写成了 `name`（员工表也有 `name`，会串成员工姓名） |
| 列表少了某几个人（没报错） | `LEFT JOIN` 写成了 `INNER JOIN` —— 没部门的员工被过滤掉了 |
| `getId()` 是 `null` | `insert` 少了 `useGeneratedKeys` / `keyProperty`，**两个要成对** |
| 改完数据"查不到" | `openSession()` 没传 `true`，手动事务忘了 `commit()` |
| `deptId` 是 `null`，其他字段正常 | `mapUnderscoreToCamelCase` 被关了，或列名不是 `dept_id` |
| JSP 报 `无法获取包含TLD:[...]的JAR资源[/WEB-INF/jsp/jakarta.tags.core]` | JSTL 版本不对。**本工程 pom 已给 3.0**：`jakarta.tags.core` 这个 URI 只在 JSTL 3.0 的 TLD 里登记过，2.0.x 虽然包名带 jakarta，TLD 里仍是旧的 `java.sun.com/jsp/jstl/core`。**别把 pom 里的 JSTL 降版本** |
| `mvn` 在 Git Bash 里报 `ClassNotFoundException: ...Launcher` | 用 IDEA 内置 Maven，或 CMD / PowerShell，**不要用 Git Bash** |

---

## 五、不要做的事

1. **不要改 `EmployeeMapper.java` 接口** —— 它是契约；改了接口就要同步改 XML 的 `id`，两处缺一个就 `Invalid bound statement`。
2. **不要删或"修好" `EmployeeServlet.java`** —— 它是**故意写错的**，实验五要靠它逐行对照。
3. **不要把 `findById` 改成关联查询** —— 单表查询的 `dept` 为 `null` 是**知识点**，不是 bug；有一条测试专门守着它。
4. **不要把 `LEFT JOIN` 改成 `INNER JOIN`** —— 没有部门的员工会凭空消失，而且不报错。
5. **不要给 Spring 的 XSD 加版本号**（如 `spring-beans-6.2.xsd`）—— 机房离线会启动失败。
6. **不要提交带真实密码的 `jdbc.properties`**，改回占位符再交。

---

## 六、提交什么

1. 本工程（可打包为 zip，**不要带 `target/` 目录**）。
2. 实验报告：按 `Java后台应用项目开发实验报告模板-P4`，每个 TODO 都要有**代码 + 运行截图**。
3. **一棵带 ★ 标记的工程目录树**（说明本次给工程新增了哪些文件）—— 见实验手册附录 C。
4. 至少一张截图能证明：`/employees` 页面显示出了**部门名**（这是关联查询生效的唯一肉眼证据）。

> 详细的任务分解、逐项验收标准和自测用例，见同目录下发的《实验.P4学生任务单》。
