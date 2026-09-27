# config-admin-api Specification

## Purpose

定义管理端通过 HTTP 对 Page / Group / Item 进行配置管理的行为契约，包括统一响应、错误码语义与各层级的管理能力。

## Requirements

### Requirement: 统一响应与错误码

所有管理端接口 SHALL 返回统一响应 `{code, message, data}`，HTTP 状态 SHALL 为 200，业务结果以 `code` 表达：成功为 0，参数错误为 BAD_REQUEST（参数校验失败），资源不存在为 NOT_FOUND（资源不存在），唯一性冲突为 CONFLICT（唯一性冲突）。

#### Scenario: 操作成功

- **WHEN** 任一管理操作合法完成
- **THEN** 返回 `code=0` 与操作结果

#### Scenario: 资源不存在

- **WHEN** 请求一个不存在的 Page / Group / Item
- **THEN** 返回 NOT_FOUND（资源不存在）

### Requirement: Page 管理能力

系统 SHALL 支持创建 Page、分页查询 Page（可按名称模糊与状态过滤）、查询单个 Page、修改 Page、删除 Page 与启停 Page。删除 Page 时若其下仍存在未删除的 Group，SHALL 返回 CONFLICT（40900）并拒绝删除。

#### Scenario: 创建并查询 Page

- **WHEN** 创建一个 Page 后按 id 查询
- **THEN** 返回其 code、name、description、status 与 sort

#### Scenario: 分页与过滤 Page

- **WHEN** 按名称关键字或状态查询 Page 列表
- **THEN** 返回匹配的分页结果

#### Scenario: 启停 Page

- **WHEN** 将 Page 状态改为 DISABLED 或 ACTIVE
- **THEN** 返回更新后的状态

#### Scenario: 删除含未删除 Group 的 Page 被拒

- **WHEN** 删除一个仍存在未删除 Group 的 Page
- **THEN** 返回 CONFLICT（40900），且该 Page 未被删除

### Requirement: Group 管理能力

系统 SHALL 支持在指定 Page 下创建 Group、按 Page 查询 Group 列表（可按状态过滤）、查询单个 Group、修改 Group、删除 Group 与启停 Group。删除 Group 时若其下仍存在未删除的 Item，SHALL 返回 CONFLICT（40900）并拒绝删除。

#### Scenario: 在 Page 下创建 Group

- **WHEN** 在一个存在的 Page 下创建 Group
- **THEN** 创建成功且归属于该 Page

#### Scenario: 按 Page 查询 Group

- **WHEN** 按 pageId 查询 Group 列表
- **THEN** 返回该 Page 下的 Group，按排序与 id 有序

#### Scenario: 启停 Group

- **WHEN** 将 Group 状态改为 DISABLED 或 ACTIVE
- **THEN** 返回更新后的状态

#### Scenario: 删除含未删除 Item 的 Group 被拒

- **WHEN** 删除一个仍存在未删除 Item 的 Group
- **THEN** 返回 CONFLICT（40900），且该 Group 未被删除

### Requirement: Item 管理能力

系统 SHALL 支持在指定 Group 下创建 Item、按 Group 查询 Item 列表（可按状态过滤）、查询单个 Item、修改 Item（含 value）、删除 Item、启停 Item，以及单独更新 Item 的 value。

#### Scenario: 在 Group 下创建 Item

- **WHEN** 在一个存在的 Group 下创建 Item
- **THEN** 创建成功且归属于该 Group

#### Scenario: 修改 Item 定义

- **WHEN** 更新 Item 的 name、value、valueType、componentType 等定义字段
- **THEN** 返回更新后的 Item

#### Scenario: 单独更新配置值

- **WHEN** 仅请求更新 Item 的 value
- **THEN** 返回值已更新的 Item

#### Scenario: 启停 Item

- **WHEN** 将 Item 状态改为 DISABLED 或 ACTIVE
- **THEN** 返回更新后的状态
