# Thread Doctor

面向 Java 线上故障的轻量级诊断工具，包含 Spring Boot 后端和 Vite 前端。

## 构建

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

构建产物：

- 后端 Jar：`target\thread-doctor-0.1.0-SNAPSHOT.jar`
- 前端静态资源：`frontend\dist\`

后端 Jar 是 Spring Boot 可执行包，可以直接运行：

```powershell
java -jar .\target\thread-doctor-0.1.0-SNAPSHOT.jar
```

## 部署

构建完成后，可以直接部署已有产物：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild
```

也可以显式指定后端 Jar 和前端资源目录：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild -BackendJar .\target\thread-doctor-0.1.0-SNAPSHOT.jar -FrontendDist .\frontend\dist
```

默认部署目录：`deploy\`

## 启停

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\start.ps1
powershell -ExecutionPolicy Bypass -File .\deploy\status.ps1
powershell -ExecutionPolicy Bypass -File .\deploy\stop.ps1
```

默认访问地址：`http://localhost:8080/`

## 大模型配置

启动后，可以在前端“大模型配置”面板修改 `baseUrl` 和 `model`。

- 前端保存的配置会在后续诊断请求中生效，不需要重启后端。
- 前端未配置的字段继续使用后端默认配置。
- API Key 不允许从前端或 YAML 文件配置，运行环境必须通过 `LLM_API_KEY` 提供。

## 无法定位时的交接

如果诊断无法明确定位根因，报告会展示：

- 未解决原因
- 需要用户继续补充的关键信息
- 可复制到 Codex/OpenCode 的代码库排查 Prompt

用户可以补充证据后重新诊断，也可以复制 Prompt 到具备 codebase 能力的工具继续排查。Thread Doctor 不会自动执行该 Prompt。

## 安全

- 生产环境必须通过 `LLM_API_KEY` 配置 API Key，禁止写入 `application*.yml` 或前端运行时配置。
- 请求大小、上传、检索、证据、指标和生成产物限制见 [docs/security-hardening.md](docs/security-hardening.md)。
- 生产环境应保持敏感数据掩码能力开启。

英文版文档见 [README_en.md](README_en.md)。
