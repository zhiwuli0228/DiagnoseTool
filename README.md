# Thread Doctor

Thread Doctor 是面向 Java 生产问题的轻量诊断工具，包含 Spring Boot 后端、Vite 前端和本机 Sidecar 日志解析服务。

## 构建

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

产物：

- 后端 Jar：`target\thread-doctor-0.1.0-SNAPSHOT.jar`
- 前端静态资源：`frontend\dist\`

Jar 可以直接运行：

```powershell
java -jar .\target\thread-doctor-0.1.0-SNAPSHOT.jar
```

## 部署

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild
```

也可以指定已有产物：

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

默认地址：

- 前端和后端：`http://localhost:8080/`
- Sidecar 健康检查：`http://127.0.0.1:18765/api/sidecar/health`

如需调整端口，复制 `deploy\app.env.example` 为 `deploy\app.env`，修改：

```properties
APP_PORT=8080
SIDECAR_PORT=18765
```

## 大日志流程

大 ZIP 或大目录默认使用 Sidecar 模式：

1. 启动 `deploy\start.ps1`，脚本会同时启动后端和 Sidecar。
2. 在前端日志区域输入本机 ZIP 路径或目录路径。
3. 点击 Sidecar 本地解析。
4. 在本机完成解压、解析、脱敏、聚类、时间线和证据生成。
5. 前端只展示解析结果；用户确认后，只提交脱敏后的结构化结果或选中的关键片段到后端。

原始日志不会默认上传后端。文件上传按钮仅用于小文件兼容模式，不建议用于生产大日志。

## LLM 配置

前端 `LLM configuration` 面板可以调整 `baseUrl` 和 `model`，新配置会在下一次诊断请求生效。

API Key 只从环境变量 `LLM_API_KEY` 读取，不在前端或 YAML 中明文保存。

## 无法定位时的交接

如果诊断无法明确定位根因，系统会返回未定位原因、需要补充的证据，以及可复制到 Codex/OpenCode 的 codebase 调查 Prompt。Thread Doctor 不会自动执行该 Prompt。

英文说明见 [README_en.md](README_en.md)。
