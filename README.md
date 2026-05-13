# AI Agent 项目

一个基于 Spring AI 和 Spring AI Alibaba 的智能代理系统，提供理财咨询、工具调用、RAG 知识库和 MCP 服务集成等功能。

## 📋 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [核心功能](#核心功能)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [API 接口](#api-接口)
- [开发指南](#开发指南)
- [测试](#测试)
- [常见问题](#常见问题)

## 🎯 项目简介

本项目是一个综合性的 AI 智能代理系统，集成了多种 AI 能力和工具，包括：

- **智能对话系统**：支持多轮对话、上下文记忆、流式输出
- **工具调用框架**：ReAct 模式的智能代理，能够自主规划和执行任务
- **RAG 知识库**：基于向量数据库的检索增强生成，支持语雀、飞书文档导入
- **MCP 服务集成**：Model Context Protocol 支持，可扩展外部工具服务
- **敏感词过滤**：DFA 算法实现的敏感词检测和拦截
- **文件处理能力**：PDF 生成、文件操作、资源下载等

## 🛠️ 技术栈

### 后端技术

- **Java 21**
- **Spring Boot 3.4.4**
- **Spring AI 1.1.2**
- **Spring AI Alibaba 1.1.2.2**
- **DashScope (通义千问)**：qwen-plus 模型
- **Lombok**：简化 Java 代码
- **Hutool**：Java 工具类库
- **Knife4j 4.5.0**：API 文档
- **Kryo 5.6.2**：高性能序列化
- **Jsoup 1.19.1**：网页解析
- **iText PDF 9.1.0**：PDF 生成
- **阿里云 OSS**：对象存储

### 前端技术

- **Vue 3**
- **Vite 5.2.0**
- **Vue Router 4.3.0**
- **Pinia 2.1.7**
- **Axios 1.6.8**

### 构建工具

- **Maven**：后端依赖管理
- **npm**：前端依赖管理

## 📁 项目结构

```
ai-agent/
├── src/main/java/com/zjw/
│   ├── agent/                  # 智能代理核心
│   │   ├── BaseAgent.java     # 基础代理抽象类
│   │   ├── ReActAgent.java    # ReAct 模式代理
│   │   ├── ToolCallAgent.java # 工具调用代理
│   │   ├── LiCaiManus.java    # 理财智能体实现
│   │   ├── LoopDetector.java  # 循环检测器
│   │   └── model/             # 代理状态模型
│   ├── app/                    # 应用层
│   │   └── FinancialApp.java  # 理财咨询应用
│   ├── controller/             # REST API 控制器
│   │   ├── AiController.java
│   │   ├── FinancialController.java
│   │   └── HealthController.java
│   ├── tools/                  # 工具集
│   │   ├── FileOperationTool.java      # 文件操作
│   │   ├── PDFGenerationTool.java      # PDF 生成
│   │   ├── ResourceDownloadTool.java   # 资源下载
│   │   ├── TerminalOperationTool.java  # 终端操作
│   │   ├── WebScrapingTool.java        # 网页抓取
│   │   ├── WebSearchTool.java          # 网络搜索
│   │   └── TerminateTool.java          # 终止工具
│   ├── advisor/                # Advisor 切面
│   │   ├── MyLoggerAdvisor.java         # 日志记录
│   │   ├── SensitiveWordAdvisor.java    # 敏感词过滤
│   │   ├── ReReadingAdvisor.java        # 重读增强
│   │   └── ToolExecutionLoggerAdvisor.java
│   ├── rag/                    # RAG 相关
│   │   ├── FinancialAppRagAdvisorFactory.java
│   │   ├── FinancialAppVectorStoreConfig.java
│   │   ├── MyQueryRewriter.java         # 查询重写
│   │   ├── ApiTranslationQueryTransformer.java # 翻译转换
│   │   └── ...
│   ├── service/                # 服务层
│   │   ├── ManualToolExecutionService.java # 手动工具执行
│   │   ├── TranslationService.java       # 翻译服务
│   │   └── ...
│   ├── config/                 # 配置类
│   │   ├── toolRegistration/   # 工具注册
│   │   ├── sensitiveWord/      # 敏感词配置
│   │   ├── oss/                # OSS 配置
│   │   └── cors/               # CORS 配置
│   ├── chatMemory/             # 对话记忆
│   │   └── FileBasedChatMemory.java # 文件持久化
│   └── common/                 # 通用类
│       ├── BaseResponse.java
│       ├── ResultUtils.java
│       └── exception/          # 异常处理
├── image-search-mcp-server/    # 图片搜索 MCP 服务
│   ├── src/main/java/
│   │   └── ImageSearchTool.java # 图片搜索工具
│   └── pom.xml
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── views/
│   │   │   ├── HomeView.vue
│   │   │   ├── FinancialChatView.vue
│   │   │   └── ManusChatView.vue
│   │   ├── router/
│   │   ├── utils/
│   │   └── components/
│   └── package.json
├── src/main/resources/
│   ├── application.yml         # 主配置文件
│   ├── mcp-servers.json        # MCP 服务器配置
│   └── document/               # 文档资源
└── pom.xml
```

## ✨ 核心功能

### 1. 智能代理架构

项目实现了基于 ReAct (Reasoning and Acting) 模式的智能代理系统：

- **BaseAgent**：基础代理类，管理状态、步骤执行、循环检测
- **ReActAgent**：实现思考-行动循环
- **ToolCallAgent**：处理工具调用的具体实现
- **LiCaiManus**：理财领域的专用智能体

### 2. 工具系统

内置多种实用工具：

- **文件操作工具**：读写文件、目录管理
- **PDF 生成工具**：生成结构化 PDF 报告
- **网络搜索工具**：集成 SearchAPI
- **网页抓取工具**：提取网页内容
- **资源下载工具**：下载网络资源
- **终端操作工具**：执行系统命令

### 3. RAG 知识库

支持检索增强生成：

- **向量数据库**：Spring AI Vector Store
- **文档加载器**：支持 Markdown、语雀、飞书文档
- **查询重写**：优化用户查询表达
- **翻译增强**：多语言查询翻译提升检索效果
- **阿里云百炼**：云端知识库集成

### 4. MCP 服务集成

通过 Model Context Protocol 扩展能力：

- **高德地图服务**：地理位置查询
- **图片搜索服务**：Pexels API 集成
- **自定义 MCP 服务器**：可扩展的工具服务

### 5. 对话管理

- **文件持久化**：基于 Kryo 的对话历史保存
- **多轮对话**：上下文感知
- **流式输出**：SSE 实时推送
- **敏感词过滤**：DFA 算法检测

## 🚀 快速开始

### 前置要求

- JDK 21+
- Maven 3.6+
- Node.js 18+
- npm 或 yarn

### 后端启动

1. **克隆项目**
```bash
git clone <repository-url>
cd ai-agent
```

2. **配置环境变量**

编辑 `src/main/resources/application.yml`，配置以下关键信息：

```yaml
spring:
  ai:
    dashScope:
      api-key: YOUR_DASHSCOPE_API_KEY  # 通义千问 API Key
```

其他可选配置（生产环境建议使用环境变量）：
- 语雀 Token
- 飞书 App ID 和 Secret
- 百度/有道/DeepL 翻译 API
- SearchAPI Key
- 阿里云 OSS 配置

3. **编译打包 MCP 服务器**

```bash
cd image-search-mcp-server
mvn clean package
cd ..
```

4. **运行后端**

```bash
mvn spring-boot:run
```

服务将在 `http://localhost:8123/api` 启动

### 前端启动

1. **安装依赖**
```bash
cd frontend
npm install
```

2. **启动开发服务器**
```bash
npm run dev
```

前端将在 `http://localhost:5173` 启动

### 访问应用

- **前端界面**：http://localhost:5173
- **API 文档**：http://localhost:8123/api/doc.html
- **健康检查**：http://localhost:8123/api/health

## ⚙️ 配置说明

### 主要配置文件

#### application.yml

```yaml
server:
  port: 8123
  servlet:
    context-path: /api

spring:
  ai:
    dashScope:
      api-key: YOUR_API_KEY
      chat:
        options:
          model: qwen-plus
```

#### mcp-servers.json

配置 MCP 服务器：

```json
{
  "mcpServers": {
    "amap-maps": {
      "command": "npx.cmd",
      "args": ["-y", "@amap/amap-maps-mcp-server"],
      "env": {
        "AMAP_MAPS_API_KEY": "YOUR_API_KEY"
      }
    },
    "image-search-mcp-server": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-jar",
        "image-search-mcp-server/target/image-search-mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

### 敏感词配置

```yaml
app:
  sensitive:
    words:
      - 暴力
      - 色情
      - 赌博
    enabled: true
    strategy: BLOCK  # BLOCK 或 REPLACE
```

## 📡 API 接口

### 理财咨询接口

#### 1. 简单对话

```
POST /api/financial/chat
Content-Type: application/json

{
  "message": "我想了解理财建议",
  "chatId": "user-123"
}
```

#### 2. 使用工具的对话

```
POST /api/financial/chat-with-tools
Content-Type: application/json

{
  "message": "帮我搜索最新的理财资讯",
  "chatId": "user-123"
}
```

#### 3. 手动控制工具执行（推荐）

```
POST /api/financial/chat-with-manual-tools
Content-Type: application/json

{
  "message": "分析我的投资组合",
  "chatId": "user-123"
}
```

#### 4. RAG 知识库对话

```
POST /api/financial/chat-with-rag
Content-Type: application/json

{
  "message": "什么是指数基金",
  "chatId": "user-123"
}
```

### AI 智能体接口

#### 1. 同步对话

```
GET /api/ai/financial_app/chat/sync?message=你好&chatId=user-123
```

#### 2. 流式对话 (SSE)

```
GET /api/ai/financial_app/chat/sse?message=你好&chatId=user-123
```

#### 3. Manus 智能体流式对话

```
GET /api/ai/manus/chat?message=帮我制定理财计划
```

### 健康检查

```
GET /api/health
```

## 💻 开发指南

### 添加新工具

1. 创建工具类并使用 `@Tool` 注解：

```java
@Service
public class MyCustomTool {
    
    @Tool(description = "工具描述")
    public String execute(@ToolParam(description = "参数描述") String param) {
        // 实现逻辑
        return result;
    }
}
```

2. 工具会自动注册到 `ToolCallbackProvider`

### 创建新的智能体

继承 `ToolCallAgent` 或 `ReActAgent`：

```java
@Component
public class MyAgent extends ToolCallAgent {
    
    public MyAgent(List<ToolCallback> allTools, 
                   ChatModel chatModel, 
                   ToolCallingManager manager) {
        super(allTools, manager);
        this.setName("MyAgent");
        this.setSystemPrompt("你的系统提示词");
        this.setNextStepPrompt("下一步提示词");
        this.setMaxSteps(10);
        
        ChatClient client = ChatClient.builder(chatModel).build();
        this.setChatClient(client);
    }
}
```

### 自定义 Advisor

实现 `Advisor` 接口来添加横切关注点：

```java
@Component
public class MyCustomAdvisor implements Advisor {
    // 实现 Advisor 方法
}
```

### RAG 文档导入

支持的文档来源：

1. **本地 Markdown 文件**
2. **语雀文档**：配置 Token 后自动同步
3. **飞书文档**：配置 App ID 和 Secret

文档会自动分割、向量化并存储到向量数据库中。

## 🧪 测试

### 运行单元测试

```bash
mvn test
```

### 主要测试类

- `FinancialAppTest`：理财应用测试
- `LiCaiManusTest`：智能体测试
- `LoopDetectorTest`：循环检测测试
- `FinancialAppRagTest`：RAG 测试
- `FinancialAppMcpTest`：MCP 测试
- 各工具类的单元测试

## ❓ 常见问题

### 1. API Key 配置问题

**Q**: 如何获取 DashScope API Key？

**A**: 访问阿里云灵积平台 (https://dashscope.aliyun.com/)，注册并创建 API Key。

### 2. MCP 服务器启动失败

**Q**: MCP 服务器无法启动？

**A**: 
- 确保已编译 `image-search-mcp-server` 模块
- 检查 `mcp-servers.json` 中的路径是否正确
- 验证环境变量是否配置正确

### 3. 敏感词过滤不生效

**Q**: 敏感词过滤没有工作？

**A**: 
- 检查 `application.yml` 中 `app.sensitive.enabled` 是否为 `true`
- 确认 `SensitiveWordAdvisor` 已添加到 ChatClient 的 advisors 中

### 4. 对话记忆丢失

**Q**: 重启后对话历史丢失？

**A**: 
- 对话记忆保存在 `chat-memory/` 目录下
- 确保该目录有写入权限
- 检查 `FileBasedChatMemory` 配置的文件路径

### 5. RAG 检索效果不佳

**Q**: 如何提高 RAG 检索质量？

**A**: 
- 优化文档质量和格式
- 调整查询重写策略
- 启用翻译功能将查询转换为中文
- 调整向量相似度阈值

### 6. 流式输出中断

**Q**: SSE 连接经常断开？

**A**: 
- 增加 `SseEmitter` 超时时间
- 检查网络连接稳定性
- 确认防火墙未拦截长连接

## 📝 许可证

本项目采用 MIT 许可证

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

---

**注意**：生产环境部署时，请务必：
1. 使用环境变量管理所有敏感信息（API Keys、Secrets）
2. 启用 HTTPS
3. 配置适当的日志级别
4. 设置监控和告警
5. 定期备份对话历史和向量数据库
