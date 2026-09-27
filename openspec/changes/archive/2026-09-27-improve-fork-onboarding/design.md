## Context

纯文档变更，动机见 proposal.md — Why。已在探索中确认：不引入 H2/Docker，只提供「换数据库」指引；可视化只用图表而非截图。本文件记录图表形式、指引落点与范围边界等取舍。

## Goals / Non-Goals

**Goals:**

- README 首屏 1 分钟看懂「是什么、怎么组织、怎么读」。
- 「换数据库」有可照做的 touchpoint 清单与排查方法，不必翻源代码。
- 「设计思想」可从 README 一键抵达现有决策文档。

**Non-Goals:**

- 不实现多数据库支持，不动代码。
- 不引入新依赖、构建或工具链。
- 不做截图/GIF、不做 CI。

## Decisions

**决策 1：可视化用 Mermaid（必要时附 ASCII），不用截图**
- 理由：GitHub 可渲染、纯文本可维护、随代码演进成本低；截图需素材且易过期。
- 备选：截图/GIF（需人工维护、易过期）；外部图片（增加外部依赖）。

**决策 2：换数据库指南「README 摘要 + design 存档」双落点**
- README 给 touchpoint 简表与步骤（抓人、可照做）；`doc/design/database-design.md` 追加「换数据库」小节存细节与完整清单。
- 备选：只放 README（长、挤占首屏）；只放 design（fork 者不易发现）。

**决策 3：不落地 H2 / 其他数据库支持，只写指引**
- 理由：需求方明确「支持其他数据库、魔改数据操作层就行」，落地多库会引入非目标依赖与维护面。
- 备选：加 H2 demo profile（被否，超出范围）；加多方言 DDL（后续按需）。

**决策 4：「设计思想」不新写长文，README 摘要 + 链接既有决策文档**
- 理由：`doc/design/design-decisions.md` 已含 D1–D10；避免两处维护。
- 备选：新写独立「设计思想」页（重复、易漂移）。

## Risks / Trade-offs

- [图表随代码演进过期] → 图保持高抽象层（架构/模型/语义），变动面小；README 状态由 archive 钩子维护。
- [换库指引遗漏后续新增的方言 SQL] → 指南中给出排查方法（全局搜反引号 / `LIMIT` / driver），而非只罗列当前位置。
- [Mermaid 在个别平台不渲染] → 关键图同时保留 ASCII 版本。

## Migration Plan

文档改动，无部署与回滚风险；回退即 `git revert`。

## Open Questions

- 换库指南是否补一个具体目标库（如 PostgreSQL）的 DDL 对照示例？本变更先给 touchpoint 与排查方法，示例按需后续补。
