# AGENTS.md

dog-config 是业务通用配置管理系统（Business Config）实现仓库。

## 仓库定位

统一管理零散业务配置（订单/商品/物流等），三级模型 **Page → Group → Item**，Key 是业务访问的稳定契约。

## 构建与测试

- 构建：`mvn install`（先装 sdk/core 到本地仓库，多模块）
- 运行：`mvn -pl dog-config-web spring-boot:run`（端口 8080）
- 测试：core 为单测（Mockito），web 为集成测试，连本地 MySQL `dog_config_test` 库（自动建库）
- 生产库：本地 MySQL `dog_config`（凭据 `admin/123456`，见 web 模块 `application.yml`）

## 规范来源

- 需求：`doc/product/v0.1/`（需求规格说明书 v1.0）
- 流程索引：`specs/v0.1/README.md`（Requirement → Spec → Design → Tasks → Review）
- 规格：`specs/v0.1/`（requirements / domain-model / database-design / api-design / project-structure / design-decisions / tasks / review）

## 开发规则

- 所有新增/变更遵循 Spec-Driven：先确认规格（`specs/v0.1/`）再实现
- 接口契约只定义在 `dog-config-sdk`（`ConfigService`），实现放 `dog-config-core`，web 只做 HTTP 暴露
- 实体 / 枚举 / Mapper 在 core，Controller / 异常 / DTO 在 web
- 读取语义：value → defaultValue → 调用方参数 / null（对齐 `specs/v0.1/domain-model.md` §4）
- 软删除用 MyBatis-Plus `@TableLogic`（deleted 列），status 列保留 ACTIVE/DISABLED 业务态
- 数据库结构变更需同步更新 `specs/v0.1/database-design.md` 与 `db/schema.sql`
