# 模块结构 — Business Config v0.1

> 对应领域规格：`openspec/specs/config-model/spec.md`
> 本文件定义 Maven 多模块结构、模块职责与依赖方向（Design 层，How）。

## 1. 总览

```text
dog-config/                       # 父 POM（聚合，无业务代码）
├── dog-config-core/              # 领域模型 + Mapper + ConfigService Bean
├── dog-config-sdk/               # 业务系统可引入的类型化读取 API
└── dog-config-web/               # Spring Boot 启动器 + 管理端 CRUD + HTTP 接口 + 静态后台
```

## 2. 模块职责

### 2.1 dog-config-core

- 领域实体：ConfigPage / ConfigGroup / ConfigItem + 枚举
- MyBatis-Plus Mapper（基础 CRUD + 查询方法）
- `ConfigService` 实现（值解析、默认值兜底、类型化转换、批量/前缀读取）
- 数据源 / MyBatis-Plus 配置类（供 web 装配）

### 2.2 dog-config-sdk

- 面向业务系统的类型化读取接口 `ConfigService`（纯接口契约）
- **独立模块**：只定义接口与返回模型，不依赖 core、不含任何实现
- 目的：业务系统 Maven 依赖 sdk 即可获得类型化读取契约，与 HTTP 通道并存

### 2.3 dog-config-web

- `@SpringBootApplication` 启动器（最终可运行 Jar）
- 管理端 Controller：/api/pages、/api/groups、/api/items
- 业务读取 Controller：/api/configs（HTTP 通道，复用 core 的 ConfigService）
- 统一响应 / 全局异常处理
- 静态管理后台页面（原生 HTML/JS/CSS，Spring 静态资源）
- 数据库 DDL / 种子数据脚本

## 3. 依赖方向

```text
dog-config-core  ──▶ （无外部依赖，自含领域 + 数据访问 + 实现）
dog-config-sdk   ──▶ （独立，不依赖 core）
dog-config-web   ──▶ dog-config-core
                   └── dog-config-sdk（提供 ConfigService 契约）
```

- core 不依赖 web / sdk
- sdk 完全独立，只声明接口契约，供业务系统引用
- web 依赖 core（装配 ConfigService 实现）与 sdk（对外暴露契约）

## 4. 包名规划

统一根包：`com.echozoo.config`

```text
com.echozoo.config
├── domain/        # core：实体与枚举
├── mapper/        # core：MyBatis-Plus Mapper
├── service/       # core：ConfigService 实现
├── sdk/           # sdk：对外接口契约（ConfigService 接口）
├── web/           # web：Controller、异常、静态页面
└── common/        # web：统一响应 / 错误码 / 异常
```

## 5. 运行方式

- 本地 MySQL（用户本地实例）
- 配置文件：`application.yml`（数据源、端口、MyBatis-Plus 配置）
- 数据库初始化：首次启动执行 `schema.sql` / `data.sql`（或启动时校验）
- 管理后台访问：`http://localhost:8080/`（静态页面）

## 6. 关联

- 领域规格：`openspec/specs/config-model/spec.md`
- API 设计：`doc/design/api-design.md`
