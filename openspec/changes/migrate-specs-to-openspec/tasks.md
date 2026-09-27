## 1. 迁移设计文档（doc/）

- [ ] 1.1 新建 `doc/design/`，迁入 `specs/v0.1/database-design.md`
- [ ] 1.2 迁入 `api-design.md`，保留端点与字段设计表（行为已提炼进 `config-admin-api` / `config-read-api`）
- [ ] 1.3 迁入 `project-structure.md`
- [ ] 1.4 迁入 `design-decisions.md`
- [ ] 1.5 将 `specs/v0.1/requirements.md` 迁至 `doc/product/v0.1/`
- [ ] 1.6 校正迁入文档中的交叉引用路径（不再指向 `specs/v0.1/`）

## 2. 更新引用

- [ ] 2.1 更新 `openspec/config.yaml` context：「项目自身规格在 specs/v0.1/」改为 openspec，并同步 `doc/design/` 说明
- [ ] 2.2 全局检索仍指向 `specs/v0.1/` 的引用，逐一更新（`README.md`、`db/schema.sql`、`db/data.sql` 注释等）
- [ ] 2.3 更新 `README.md` 文档表：`specs/v0.1/` 行改为 `doc/design/` 与 openspec

## 3. 删除旧目录

- [ ] 3.1 删除 `specs/v0.1/`（含 `README.md`、`tasks.md`、`review.md`）

## 4. 验证

- [ ] 4.1 归档后确认 `openspec/specs/` 出现 `config-model` / `config-admin-api` / `config-read-api` 三个能力规格
- [ ] 4.2 全局检索确认无残留 `specs/v0.1` 引用
- [ ] 4.3 `openspec validate --strict` 通过
- [ ] 4.4 `mvn install` 通过（确认纯文档迁移无副作用）
