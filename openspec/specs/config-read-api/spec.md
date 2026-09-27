# config-read-api Specification

## Purpose

定义业务系统读取配置的契约：统一的解析语义，以及 HTTP 读取接口与类型化 Java SDK 两条读取通道。

## Requirements

### Requirement: 读取解析语义

系统 SHALL 按 `valueType` 解析配置值，并遵循以下优先级：`value` 非空时使用 `value`；`value` 为空且 `defaultValue` 非空时使用 `defaultValue`；两者均为空时，带兜底参数的读取返回调用方传入的参数，无兜底参数的读取返回 null。

#### Scenario: 使用 value

- **WHEN** 配置项 `value` 非空
- **THEN** 读取返回按 `valueType` 解析后的 `value`

#### Scenario: 回落到 defaultValue

- **WHEN** 配置项 `value` 为空而 `defaultValue` 非空
- **THEN** 读取返回按 `valueType` 解析后的 `defaultValue`

#### Scenario: 无值且无兜底参数

- **WHEN** `value` 与 `defaultValue` 均为空，且读取未提供兜底参数
- **THEN** 读取返回 null

#### Scenario: 无值但有兜底参数

- **WHEN** `value` 与 `defaultValue` 均为空，且读取提供兜底参数
- **THEN** 读取返回调用方传入的兜底参数

### Requirement: HTTP 读取接口

系统 SHALL 提供 `GET /api/configs/{key}` 单条读取、`GET /api/configs?keys=a,b,c` 批量读取、`GET /api/configs?prefix=order.` 前缀读取。单条读取的 key 不存在时 SHALL 返回 NOT_FOUND（资源不存在）；批量与前缀读取的结果 SHALL 为 Map，且 SHALL NOT 包含不存在的 key。

#### Scenario: 单条读取

- **WHEN** 请求一个存在且为 ACTIVE 的配置 key
- **THEN** 返回解析后的值

#### Scenario: 单条读取不存在的 key

- **WHEN** 请求一个不存在的配置 key
- **THEN** 返回 NOT_FOUND（资源不存在）

#### Scenario: 批量读取

- **WHEN** 按多个 key 批量读取
- **THEN** 返回 Map，包含命中的已启用配置；不存在的 key 不出现在结果中

#### Scenario: 前缀读取

- **WHEN** 按前缀读取
- **THEN** 返回所有以该前缀开头且已启用的配置，组成 Map

### Requirement: 类型化 SDK 读取

系统 SHALL 提供独立 SDK，支持按字符串、整型、长整型、小数、布尔读取，以及将 JSON 反序列化到目标类型；并支持批量与前缀读取。每个类型化读取 SHALL 提供带调用方兜底参数的重载。当存储值无法按目标类型解析时，SDK SHALL 抛出类型转换异常。

#### Scenario: 类型化读取

- **WHEN** 通过 SDK 以 `getInt` / `getBoolean` 等读取配置
- **THEN** 返回对应 Java 类型解析后的值

#### Scenario: 带兜底参数的读取

- **WHEN** 配置无有效值且调用方传入兜底参数
- **THEN** 返回调用方传入的兜底参数

#### Scenario: 解析失败

- **WHEN** 存储值无法按目标类型解析
- **THEN** SDK 抛出类型转换异常
