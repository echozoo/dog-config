## 1. 值编解码器（core）

- [x] 1.1 新增 core 值编解码器：`validate(valueType, raw)` 与 `parse(valueType, raw)`，覆盖 STRING/INTEGER/LONG/DECIMAL/BOOLEAN/JSON
- [x] 1.2 让 `ConfigServiceImpl.parseValue` 改用该编解码器，读路径行为不变
- [x] 1.3 补 core 单测：每种类型的合法值解析、非法值抛异常

## 2. 写路径值校验（web）

- [x] 2.1 `createItem` / `updateItem` 落库前调用编解码器校验 `value` 与 `defaultValue`
- [x] 2.2 `updateItemValue` 更新 value 前做同样校验
- [x] 2.3 校验 `componentType` 与 `valueType` 白名单（SWITCH→BOOLEAN、NUMBER→数值、INPUT→STRING、TEXTAREA→STRING/JSON、SELECT/RADIO→STRING）
- [x] 2.4 `SELECT` / `RADIO` 校验 `options` 为合法 JSON 数组
- [x] 2.5 校验 `required=true` 时 `value` 或 `defaultValue` 至少一个非空

## 3. 软删感知的唯一性（core + web）

- [x] 3.1 三个 Mapper 各新增「包含已删记录」的查询方法（显式 SQL，绕过逻辑删除过滤）
- [x] 3.2 `createItem` 按 key 查重时覆盖已删记录，命中返回 `CONFLICT` 并区分「已删除占用」
- [x] 3.3 `createPage` / `createGroup` 同样覆盖已删 `code` / `(page_id, code)`

## 4. 父级删除保护（web）

- [x] 4.1 `deletePage` 先统计未删子 Group，存在则 `CONFLICT`
- [x] 4.2 `deleteGroup` 先统计未删子 Item，存在则 `CONFLICT`

## 5. 测试与验证

- [x] 5.1 core 单测覆盖编解码器全部场景
- [x] 5.2 web 集成测试覆盖：非法值 400、组件不兼容 400、缺 options 400、必填 400、删后同名 409、含子级删除 409
- [x] 5.3 `mvn install` 全模块通过，确认读路径回归测试无变化
