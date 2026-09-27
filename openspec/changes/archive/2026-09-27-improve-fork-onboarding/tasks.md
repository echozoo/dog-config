## 1. README 首屏可视化（B / 决策 1）

- [x] 1.1 首屏加一句价值主张：这是什么、给谁用、一句话能 get 什么
- [x] 1.2 加整体架构图（Mermaid：业务系统 → web → core → MySQL；业务系统 → SDK）
- [x] 1.3 加模型图 Page → Group → Item（Mermaid ER 或保留 ASCII）
- [x] 1.4 加读取语义流水线图 `value → defaultValue → 调用方参数 / null`

## 2. 「设计思想」摘要（B / 决策 4）

- [x] 2.1 README 增「设计思想」节：三级模型、Key 即业务契约、字符串存储+类型解析、sdk/core/web 分层、软删除与业务态分离
- [x] 2.2 链接到 `doc/design/design-decisions.md`，避免重复维护

## 3. 换数据库指南（C / 决策 2、3）

- [x] 3.1 README 增「换数据库 / 魔改数据访问层」节：列出全部 touchpoint——驱动依赖（`web/pom.xml`）、连接配置（`application.yml`）、DDL 方言（`db/schema.sql`：`AUTO_INCREMENT` / `TINYINT(1)` / 反引号 / `TEXT` / `DATETIME`）、种子脚本（`db/data.sql`：`INSERT IGNORE` / `NOW()`）、自定义 SQL（`*Mapper.java`：反引号 + `LIMIT 1`）
- [x] 3.2 给出替换步骤与排查方法（全局搜反引号 / `LIMIT` / `driver`）
- [x] 3.3 在 README 快速开始中明确完整 DDL 的位置与执行步骤（`db/schema.sql`、`db/data.sql`）
- [x] 3.4 在 `doc/design/database-design.md` 追加「换数据库」小节，存放完整清单与说明

## 4. 验证

- [x] 4.1 Mermaid 图在本仓库渲染正常（本地/预览），必要时补 ASCII 备份
- [x] 4.2 README 内链接可达（`doc/design/design-decisions.md`、`doc/design/database-design.md`）
- [x] 4.3 全局检索确认无失效引用（如 `specs/v0.1`）
- [x] 4.4 `mvn install` 通过（确认纯文档变更无副作用）
