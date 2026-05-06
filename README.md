# Thread Doctor

Spring Boot 后端与 Vite 前端的轻量化编译、部署、启动流程。

## 编译

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

输出产物：

- 后端 jar：`target\thread-doctor-0.1.0-SNAPSHOT.jar`
- 前端静态资源：`frontend\dist\`

后端 jar 是 Spring Boot 可执行 jar，可以直接运行：

```powershell
java -jar .\target\thread-doctor-0.1.0-SNAPSHOT.jar
```

## 部署已编译产物

编译完成后，不重新编译，直接部署：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild
```

也可以显式指定产物路径：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild -BackendJar .\target\thread-doctor-0.1.0-SNAPSHOT.jar -FrontendDist .\frontend\dist
```

默认部署目录：`deploy\`

## 启动与停止

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\start.ps1
powershell -ExecutionPolicy Bypass -File .\deploy\status.ps1
powershell -ExecutionPolicy Bypass -File .\deploy\stop.ps1
```

默认访问地址：`http://localhost:8080/`

## 大模型配置

启动后可在前端的 `LLM configuration` 面板修改 `baseUrl`、`API key`、`model`。

- 保存后对下一次诊断请求热生效，不需要重启后端。
- 未在前端配置的字段会继续使用后端 `application*.yml` 或环境变量配置。
- 前端读取配置状态时不会回显完整 API key，只显示脱敏值。
