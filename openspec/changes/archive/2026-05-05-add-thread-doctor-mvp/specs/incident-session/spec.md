## ADDED Requirements

### Requirement: 缓存型故障会话
系统 SHALL 允许用户创建和查询故障会话，并 SHALL 将会话数据保存在应用缓存中。系统 MUST NOT 依赖数据库保存或读取故障会话。

#### Scenario: 创建故障会话
- **WHEN** 用户提交故障标题、描述和严重级别
- **THEN** 系统 MUST 创建一个带唯一会话 ID 的缓存会话并返回当前状态

#### Scenario: 查询缓存会话
- **WHEN** 用户根据会话 ID 查询仍在缓存中的故障会话
- **THEN** 系统 MUST 返回会话详情和当前状态

#### Scenario: 会话缓存未命中
- **WHEN** 用户查询不存在或已过期的会话 ID
- **THEN** 系统 MUST 返回可诊断的未找到错误

### Requirement: 会话缓存边界
系统 SHALL 通过配置控制故障会话缓存的容量和过期时间。

#### Scenario: 缓存配置生效
- **WHEN** 应用启动并读取缓存配置
- **THEN** 系统 MUST 使用配置的最大容量和 TTL 管理故障会话缓存
