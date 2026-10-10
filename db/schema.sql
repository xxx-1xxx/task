-- ============================================================
-- zjtec-admin · 实验四 建库建表脚本
-- 与《实验指导书》实验四步骤 1 完全一致，可直接在 HeidiSQL 里整段执行
--
-- 库名 db_ssm：实验四 / 实验五 / 实验六 共用同一个库
-- ============================================================

CREATE DATABASE IF NOT EXISTS db_ssm DEFAULT CHARACTER SET utf8mb4;
USE db_ssm;

-- ---------- 部门表 ----------
CREATE TABLE IF NOT EXISTS dept (
  id   INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '部门表';

-- ---------- 员工表（dept_id 外键关联部门） ----------
CREATE TABLE IF NOT EXISTS employee (
  id      INT PRIMARY KEY AUTO_INCREMENT,
  name    VARCHAR(50) NOT NULL,
  gender  VARCHAR(10),
  dept_id INT,
  CONSTRAINT fk_dept FOREIGN KEY (dept_id) REFERENCES dept(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '员工表';

-- ---------- 测试数据 ----------
-- 注意：重复执行本脚本会重复插入。要重来一遍就先执行下面两行：
--   DELETE FROM employee;
--   DELETE FROM dept;

INSERT INTO dept(name) VALUES ('研发部'), ('人事部'), ('测试部');

INSERT INTO employee(name, gender, dept_id) VALUES
  ('张三', '男', 1),
  ('李四', '女', 2),
  ('王五', '男', 1),
  ('赵六', '女', 2),
  ('孙七', '男', 3);

-- ---------- 验证：应查出 5 行，且部门名不为空 ----------
SELECT e.id, e.name, e.gender, d.name AS dept_name
FROM employee e
LEFT JOIN dept d ON e.dept_id = d.id
ORDER BY e.id;
