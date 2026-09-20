# 领域模型 — Business Config v0.1

> 规格来源：`doc/product/v0.1/业务通用配置管理系统需求规格说明书_v1.0.md`
> 本文件定义系统核心领域对象、枚举与值解析规则（Spec 层，What）。

## 1. 总览

系统采用三级模型：

```text
ConfigPage
    │ 1:N
    ▼
ConfigGroup
    │ 1:N
    ▼
ConfigItem
```

设计原则（对齐需求书 §27）：

- **Page / Group 用于组织与展示**，不参与业务访问契约
- **Item 的 key 是业务访问的稳定契约**
- **配置类型明确**，不把一切当字符串
- **优先保证简单**，避免过度设计

## 2. 数据模型

### 2.1 ConfigPage — 业务配置域

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | Long | PK | 主键 |
| code | String | unique, not null | 唯一编码（业务标识） |
| name | String | not null | 配置名称 |
| description | String | nullable | 配置描述 |
| status | ConfigStatus | not null | 状态 |
| sort | Integer | default 0 | 排序 |
| createdAt | LocalDateTime | not null | 创建时间 |
| updatedAt | LocalDateTime | not null | 更新时间 |

### 2.2 ConfigGroup — 配置分组

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | Long | PK | 主键 |
| pageId | Long | FK → config_page.id, not null | 所属 Page |
| code | String | unique (page_id+code), not null | 分组编码 |
| name | String | not null | 分组名称 |
| description | String | nullable | 分组描述 |
| status | ConfigStatus | not null | 状态 |
| sort | Integer | default 0 | 排序 |
| createdAt | LocalDateTime | not null | 创建时间 |
| updatedAt | LocalDateTime | not null | 更新时间 |

### 2.3 ConfigItem — 配置项（核心对象）

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | Long | PK | 主键 |
| groupId | Long | FK → config_group.id, not null | 所属 Group |
| key | String | unique, not null | 配置 Key（业务访问契约） |
| name | String | not null | 配置名称 |
| value | String | nullable | 当前配置值（原始字符串存储） |
| defaultValue | String | nullable | 默认值（原始字符串存储） |
| valueType | ValueType | not null | 数据类型 |
| componentType | ComponentType | not null | 管理组件类型 |
| options | String | nullable | 可选项（JSON 数组字符串，仅 SELECT/RADIO 使用） |
| description | String | nullable | 配置说明 |
| required | boolean | default false | 是否必填 |
| status | ConfigStatus | not null | 状态 |
| sort | Integer | default 0 | 排序 |
| createdAt | LocalDateTime | not null | 创建时间 |
| updatedAt | LocalDateTime | not null | 更新时间 |

## 3. 枚举

### 3.1 ConfigStatus

```text
ACTIVE   # 启用，业务可读取
DISABLED # 禁用，业务不可读取
DELETED  # 逻辑删除（MyBatis-Plus @TableLogic 维护，不显式写入）
```

> 说明：DELETED 由 `@TableLogic` 在删除操作时自动置为逻辑删除标记，业务查询默认过滤；ACTIVE/DISABLED 为业务态，由管理端显式设置。

### 3.2 ValueType

```text
STRING   # 字符串
INTEGER  # 整型
LONG     # 长整型
DECIMAL  # 小数
BOOLEAN  # 布尔
JSON     # JSON
```

### 3.3 ComponentType

```text
INPUT    # 单行输入
NUMBER   # 数字输入
SWITCH   # 开关
SELECT   # 下拉选择
RADIO    # 单选
TEXTAREA # 多行文本
```

## 4. 值解析规则

- `value` / `defaultValue` 均以字符串原始存储，读取时按 `valueType` 解析
- 读取优先级：

```text
value 非空            → 使用 value
value 空，defaultValue 非空 → 使用 defaultValue（DB 集中兜底）
value 空，defaultValue 空  → 带参重载返回调用方传入参数；无参重载返回 null
```

- 解析失败：抛出类型转换异常（`IllegalArgumentException`）

## 5. 完整性约束

- config_page.code 唯一
- config_group (page_id, code) 唯一
- config_item.key 唯一
- 多租户扩展（tenant_id + key）V1 不引入

## 6. 关联

- 数据库设计：`specs/v0.1/database-design.md`
- API 设计：`specs/v0.1/api-design.md`
- 模块结构：`specs/v0.1/project-structure.md`
