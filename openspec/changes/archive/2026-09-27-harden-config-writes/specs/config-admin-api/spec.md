## MODIFIED Requirements

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
