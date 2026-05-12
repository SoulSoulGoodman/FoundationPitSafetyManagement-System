# OpenSpec 说明

本项目已配置 OpenSpec 用于 AI 驱动规范开发（Spec-Driven Development）。

## 已完成的配置

- `opencode.json` — 已配置 `"plugin": ["opencode-plugin-openspec"]`
- Opencode 启动时会自动安装该插件（通过 Bun）

## 需要手动完成的步骤（在本地有 Node.js 的环境下）

### 1. 安装 OpenSpec CLI（需要 Node.js >= 20.19）

```bash
npm install -g @fission-ai/openspec@latest
```

### 2. 初始化项目

```bash
cd FoundationPitSafetyManagement-System
openspec init
```

### 3. 使用

初始化后，在 opencode 中可使用以下命令：

| 命令 | 功能 |
|------|------|
| `/opsx:propose <描述>` | 创建变更提案（含 proposal/specs/design/tasks） |
| `/opsx:apply` | 按 tasks.md 逐步实现 |
| `/opsx:archive` | 归档完成的变更，合并 specs |

## 目录结构（`openspec init` 后自动生成）

```
openspec/
├── specs/              # 源规范（系统行为描述）
│   └── <domain>/
│       └── spec.md
├── changes/            # 变更提案
│   └── <change-name>/
│       ├── proposal.md
│       ├── design.md
│       ├── tasks.md
│       └── specs/
└── config.yaml
```
