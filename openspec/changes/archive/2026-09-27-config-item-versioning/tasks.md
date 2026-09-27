## 1. 版本数据模型（core + db）

- [x] 1.1 新增 `config_item_version` 建表语句，同步 `db/schema.sql`（`CREATE TABLE IF NOT EXISTS`）
- [x] 1.2 新增版本实体与 Mapper（core），含按 item_id 列表、按 item_id + version_no 查询
- [x] 1.3 同步 `doc/design/database-design.md` 表结构与索引说明

## 2. 版本写入（web）

- [x] 2.1 在 `ConfigAdminService` 抽出「追加版本」方法，写入 `value/default_value/value_type/change_type/version_no`
- [x] 2.2 `createItem` 追加 CREATE 初始版本（值可为空）
- [x] 2.3 `updateItem` 仅当 `value` / `defaultValue` 变化时追加 UPDATE 版本
- [x] 2.4 `updateItemValue` 追加 UPDATE 版本
- [x] 2.5 确认 `updateItemStatus` / `deleteItem` 不产生版本
- [x] 2.6 core 补「解除软删」的更新方法（显式 SQL 绕过逻辑删除），供恢复使用

## 3. 查询与操作端点（web）

- [x] 3.1 `GET /api/items/{id}/versions` 返回按 version_no 有序的版本列表
- [x] 3.2 `GET /api/items/{id}/versions/{versionNo}` 返回版本详情
- [x] 3.3 `POST /api/items/{id}/versions/{versionNo}/rollback` 回滚：写回旧值 + 追加 ROLLBACK 版本
- [x] 3.4 `POST /api/items/{id}/restore` 恢复软删项 + 追加 RESTORE 版本
- [x] 3.5 item / 版本不存在时返回 `NOT_FOUND`；恢复非软删或不存在项返回 `NOT_FOUND`

## 4. 测试与验证

- [x] 4.1 web 集成测试：创建产生 v1、值变更产生 v2、仅非值字段变更不产生版本
- [x] 4.2 web 集成测试：版本列表/详情、回滚后当前值与历史正确、不存在版本 404
- [x] 4.3 web 集成测试：软删后恢复可读且追加 RESTORE 版本、恢复未删项 404
- [x] 4.4 回归确认：既有读取测试与 SDK 契约无变化
- [x] 4.5 `mvn install` 全模块通过

## 5. 存量数据（可选）

- [x] 5.1 提供幂等回填脚本，为现有 `config_item` 各插入一条基于当前值的 CREATE 版本
