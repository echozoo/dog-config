# API 设计 — Business Config v0.1

> 对应领域规格：`openspec/specs/config-admin-api/spec.md`、`openspec/specs/config-read-api/spec.md`
> 本文件定义管理端 CRUD API 与业务读取 API（Design 层，How）。
> 双通道落地：sdk 提供 Java Bean 类型化 API，web 暴露 HTTP 接口。

## 1. 概览

- HTTP 统一前缀 `/api`
- 管理端：`/api/pages`、`/api/groups`、`/api/items`
- 业务读取端：`/api/configs`
- 统一响应包装：

```json
{ "code": 0, "message": "ok", "data": ... }
```

## 2. 错误码

| code | 含义 |
| --- | --- |
| 0 | 成功 |
| 40000 | 参数校验失败 |
| 40400 | 资源不存在 |
| 40900 | 唯一性冲突（code/key 重复，含与已软删除记录重复）/ 层级删除保护（含未删除子级） |
| 50000 | 服务器内部错误 |

## 3. Page 管理

| 方法 | 路径 | 说明 | 请求体 |
| --- | --- | --- | --- |
| POST | /api/pages | 创建 Page | PageRequest |
| GET | /api/pages | 分页查询 Page | ?name=&status=&page=&size= |
| GET | /api/pages/{id} | 查询单个 Page | - |
| PUT | /api/pages/{id} | 修改 Page | PageRequest |
| DELETE | /api/pages/{id} | 逻辑删除 Page | - |
| PATCH | /api/pages/{id}/status | 启用/禁用 Page | {status} |

**PageRequest**

```json
{
  "code": "ORDER",
  "name": "订单配置",
  "description": "订单相关配置",
  "status": "ACTIVE",
  "sort": 1
}
```

## 4. Group 管理

| 方法 | 路径 | 说明 | 请求体 |
| --- | --- | --- | --- |
| POST | /api/pages/{pageId}/groups | 创建 Group | GroupRequest |
| GET | /api/pages/{pageId}/groups | 按 Page 查 Group 列表 | ?status= |
| GET | /api/groups/{id} | 查询单个 Group | - |
| PUT | /api/groups/{id} | 修改 Group | GroupRequest |
| DELETE | /api/groups/{id} | 逻辑删除 Group | - |
| PATCH | /api/groups/{id}/status | 启用/禁用 Group | {status} |

**GroupRequest**

```json
{
  "code": "AUTO_CANCEL",
  "name": "自动取消",
  "description": "订单自动取消策略",
  "sort": 1
}
```

## 5. Item 管理

| 方法 | 路径 | 说明 | 请求体 |
| --- | --- | --- | --- |
| POST | /api/groups/{groupId}/items | 创建 Item | ItemRequest |
| GET | /api/groups/{groupId}/items | 按 Group 查 Item 列表 | ?status= |
| GET | /api/items/{id} | 查询单个 Item | - |
| PUT | /api/items/{id} | 修改 Item（含 value） | ItemRequest |
| DELETE | /api/items/{id} | 逻辑删除 Item | - |
| PATCH | /api/items/{id}/status | 启用/禁用 Item | {status} |
| PATCH | /api/items/{id}/value | 修改配置值 | {value} |
| GET | /api/items/{id}/versions | 列出配置项版本（按 version_no 升序） | - |
| GET | /api/items/{id}/versions/{versionNo} | 查询单个版本详情 | - |
| POST | /api/items/{id}/versions/{versionNo}/rollback | 回滚到指定历史版本 | - |
| POST | /api/items/{id}/restore | 恢复已软删除的配置项 | - |

**ItemRequest**

```json
{
  "key": "order.auto.cancel.minutes",
  "name": "自动取消时间",
  "value": "60",
  "defaultValue": "30",
  "valueType": "INTEGER",
  "componentType": "NUMBER",
  "options": "[{\"value\":\"SF\",\"label\":\"顺丰\"}]",
  "description": "订单创建后超过该分钟数自动取消",
  "required": false,
  "sort": 1
}
```

## 6. 业务读取 API（HTTP）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /api/configs/{key} | 读取单个配置（返回解析后的值） |
| GET | /api/configs?keys=a,b,c | 按 keys 批量读取（返回 Map） |
| GET | /api/configs?prefix=order. | 按前缀批量读取（返回 Map） |

响应示例：

```json
// GET /api/configs/order.auto.cancel.minutes
{ "code": 0, "message": "ok", "data": 30 }

// GET /api/configs?keys=order.timeout,order.allow.over.sell
{ "code": 0, "message": "ok", "data": {
    "order.timeout": 30,
    "order.allow.over.sell": false
} }
```

**读取规则**

```text
value 非空            → 使用 value
value 空，defaultValue 非空 → 使用 defaultValue（DB 集中兜底）
value 空，defaultValue 空  → 带参重载返回调用方传入参数；无参重载返回 null
```

- 单个 key 不存在时返回 40400
- 批量读取：不存在的 key 不在结果中出现

## 7. 业务读取 API（Java SDK Bean）

供业务系统直接注入使用（无需 HTTP）：

```java
String  getString(String key);
String  getString(String key, String defaultValue);
Integer getInt(String key);
Integer getInt(String key, Integer defaultValue);
Long    getLong(String key);
Long    getLong(String key, Long defaultValue);
BigDecimal getDecimal(String key);
BigDecimal getDecimal(String key, BigDecimal defaultValue);
Boolean getBoolean(String key);
Boolean getBoolean(String key, Boolean defaultValue);
<T> T   getJson(String key, Class<T> clazz);
Map<String, Object> getBatch(List<String> keys);
Map<String, Object> getByPrefix(String prefix);
```

- 读取优先级：`value` → `defaultValue`（DB 兜底）→ 调用方传入参数 / null
- 类型转换失败抛 `IllegalArgumentException`
- `getBatch` / `getByPrefix` 与 HTTP 批量读取规则一致

## 8. 关联

- 领域规格：`openspec/specs/config-admin-api/spec.md`、`openspec/specs/config-read-api/spec.md`
- 模块结构：`doc/design/project-structure.md`
