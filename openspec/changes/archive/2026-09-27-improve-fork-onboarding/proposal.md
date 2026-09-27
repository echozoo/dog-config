## Why

项目定位是「让人 fork 魔改或直接套用设计思想的参考实现」。但当前 README 首屏以文字为主，读者要读完大段说明才能在脑中拼出系统长什么样；同时「换个数据库」这类最常见的魔改诉求没有任何指引，读者得自己翻代码才能找出与 MySQL 绑定的少数几处。降低「看懂」和「改得动」的门槛，直接决定别人是否愿意 fork。

## What Changes

- **README 首屏可视化**：用图表（Mermaid / ASCII，不用截图）呈现整体架构、Page → Group → Item 模型、读取语义流水线。
- **README 增「设计思想」摘要**：提炼关键取舍，链接 `doc/design/design-decisions.md`，便于直接套用。
- **新增「换数据库 / 魔改数据访问层」指南**：列出与 MySQL 绑定的全部 touchpoint（驱动依赖、连接配置、DDL 方言、种子脚本、自定义 SQL）与替换步骤，并给出排查方法。
- **明确完整 DDL 与执行步骤**：在 README 快速开始中说明 `db/schema.sql` / `db/data.sql` 的位置与执行方式。
- 纯文档变更：不改运行时行为、HTTP API 与数据库结构。

## Capabilities

### New Capabilities

<!-- 无：纯文档与指引，不引入新能力 -->

### Modified Capabilities

<!-- 无：不改变任何既有能力的行为契约 -->

本变更无规格变更（纯文档/指引），已在 `.openspec.yaml` 置 `skip_specs: true`。

## Impact

- 文档：`README.md`；换库细节拟追加到 `doc/design/database-design.md` 或 README 专节。
- 代码 / API / 数据库：无变更。
- 参考现状：`doc/design/database-design.md`（表结构）、`doc/design/design-decisions.md`（D1–D10 决策）。

## Non-goals

- 不真正支持 H2 / 其他数据库运行——只提供指引，由使用者自行魔改。
- 不引入 Docker / Compose。
- 不新增 CI、不改任何业务功能。
- 不使用截图 / GIF（只用图表）。
