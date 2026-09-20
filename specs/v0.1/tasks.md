# 任务分解 — Business Config v0.1

> Tasks 层：将 Design 决策逐条推演为可执行、可验证的任务。
> 对应设计：`specs/v0.1/design-decisions.md`、`specs/v0.1/api-design.md`、`specs/v0.1/project-structure.md`

## 推演原则

每个任务必须能追溯到设计决策（D#）或 Spec 条款。任务 = 设计决策的落地执行，不是临时拆分。

## 任务清单

| # | 任务 | 设计依据 | 执行内容 | 验证方式 | 状态 |
| --- | --- | --- | --- | --- | --- |
| T1 | 父 POM 与三模块骨架 | D5/D6 | 聚合 pom + sdk/core/web 子模块 | `mvn install` 成功 | ✅ |
| T2 | sdk 接口契约 | D4/D6、api-design §7 | `ConfigService` 接口（get/getString/getInt/getLong/getDecimal/getBoolean/getJson/getBatch/getByPrefix） | 接口编译通过，无实现 | ✅ |
| T3 | core 领域实体与枚举 | domain-model §2/§3 | ConfigPage/Group/Item + ConfigStatus/ValueType/ComponentType | 实体与表结构映射正确 | ✅ |
| T4 | core Mapper | D1 | 3 个 BaseMapper | 编译 + 集成测试 | ✅ |
| T5 | core ConfigService 实现 | domain-model §4、D10 | 值解析、默认值兜底、批量/前缀 | 单测 9 用例通过 | ✅ |
| T6 | web 统一响应/异常 | api-design §2 | ApiResponse/ErrorCode/ApiException/GlobalExceptionHandler | 错误码返回正确 | ✅ |
| T7 | web Page 管理 API | api-design §3 | CRUD + 状态 | 集成测试 createPageAndConflict | ✅ |
| T8 | web Group 管理 API | api-design §4 | CRUD + 状态 | 集成测试覆盖 | ✅ |
| T9 | web Item 管理 API | api-design §5 | CRUD + 状态 + value 更新 | 集成测试 updateItemValueReflectedInRead | ✅ |
| T10 | web 读取 HTTP API | api-design §6 | 单 key / keys 批量 / 前缀批量 | 集成测试 3 用例 | ✅ |
| T11 | 静态管理后台页面 | D3 | index.html 原生 JS 渲染 Page→Group→Item | 浏览器访问 + 修改 value 生效 | ✅ |
| T12 | MySQL schema/data 脚本 | database-design §2/§5 | 建表 SQL + 种子数据（幂等） | 建库成功，9 条种子 item | ✅ |
| T13 | 集成测试环境 | T7-T10 验证 | MockMvc 连本地 MySQL dog_config_test（自动建库、事务回滚） | 6 用例通过 | ✅ |

## 任务推演追溯表

| Task | 设计依据（文档 + 条款） | 状态 |
| --- | --- | --- |
| T1 | project-structure §2 + design-decisions D5/D6 | ✅ |
| T2 | api-design §7 + D4/D6 | ✅ |
| T3 | domain-model §2/§3 | ✅ |
| T4 | design-decisions D1 | ✅ |
| T5 | domain-model §4 + D10 | ✅ |
| T6 | api-design §2 | ✅ |
| T7 | api-design §3 | ✅ |
| T8 | api-design §4 | ✅ |
| T9 | api-design §5 | ✅ |
| T10 | api-design §6 + D4 | ✅ |
| T11 | design-decisions D3 | ✅ |
| T12 | database-design §2/§5 | ✅ |
| T13 | review.md 验证策略 | ✅ |

## 验证结论

- 全部 13 个任务完成
- core 单测 9 通过、web 集成测试 6 通过
- 运行验证：管理后台可访问、读取 API 全部正常
