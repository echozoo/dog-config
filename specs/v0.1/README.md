# Business Config v0.1 — Spec 流程索引

> 本目录按 Spec-Driven Development 分层组织产物。

## 流程分层

```text
Requirements（需求）
    ↓
Content Model（内容模型 / Spec）
    ↓
Design（设计）
    ↓
Tasks（任务分解）
    ↓
Implementation（实现）→ 见仓库代码
    ↓
Verify（验证）→ specs/v0.1/review.md
    ↓
Review（评审）→ specs/v0.1/review.md
    ↓
Archive（归档）→ olinguito/cases/dog-config/
```

## 产物清单

| 层 | 文件 | 内容 |
| --- | --- | --- |
| Requirement | `requirements.md` | 需求确认记录、澄清、边界 |
| Spec（Content Model） | `domain-model.md` | 领域对象、枚举、值解析规则（What） |
| Design | `database-design.md` | MySQL 表结构、索引、访问模式（How） |
| Design | `api-design.md` | 管理端 + 读取端 API 定义（How） |
| Design | `project-structure.md` | 多模块结构与依赖（How） |
| Design | `design-decisions.md` | 关键设计决策记录与取舍（Why） |
| Tasks | `tasks.md` | 任务分解表（可追溯设计依据） |
| Review | `review.md` | 验收记录与评审结论 |

## 层间追溯

- Spec 定义「是什么」（What），Design 定义「怎么做」（How），Tasks 定义「做什么」（执行计划）
- 每份 Design 文档标注其对应的 Spec；每个 Task 标注设计依据
- 需求变更时：先改 Requirement → 影响 Spec → 再改 Design / Tasks，禁止跳过

## 对应需求

- `doc/product/v0.1/业务通用配置管理系统需求规格说明书_v1.0.md`
