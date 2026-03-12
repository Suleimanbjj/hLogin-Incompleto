# hLogin 🔐

Plugin de autenticação para servidores Minecraft (offline mode) com foco em segurança, compatibilidade entre versões e UX visual.

## ✨ Recursos

- Login e registro com comandos dedicados (`/login` e `/register`) 👤
- Suporte a `MYSQL` e `SQLITE` com migração automática de schema 🗄️
- Captcha (inventory, math, text, visual) 🧩
- Sessão lembrada (`remember session`) 🧠
- Proteções para jogadores não autenticados (movimento, comandos, chat, inventário, dano) 🛡️
- Interface visual com title/actionbar/bossbar/holograma (com fallback) 🎨
- Webhooks para integrações externas 🔗
- Multi-idioma com arquivos de tradução 🌍
- Arquitetura modular (`api`, `core`, `bukkit`) 🧱

## 🧩 Compatibilidade

- Java 8+ ☕
- Spigot/Paper e derivados 🧪
- Projeto preparado para múltiplas versões de servidor (adapters legacy e modern no módulo Bukkit) 🕹️

## 📦 Estrutura do projeto

- `hlogin-api`: contratos públicos (eventos, serviços, modelos)
- `hlogin-core`: regras de negócio, segurança, sessão e persistência
- `hlogin-bukkit`: integração Bukkit/Spigot, comandos, listeners e camada visual

## 🚀 Instalação

1. Baixe/compile o `hLogin.jar`.
2. Coloque em `plugins/`.
3. Inicie o servidor para gerar os arquivos.
4. Configure `plugins/hLogin/setup.yml`.
5. Reinicie o servidor.

## ⚙️ Configuração rápida

### SQLite (simples) 🪶

No `setup.yml`:

```yml
storage:
  type: SQLITE
```

### MySQL 🐬

No `setup.yml`:

```yml
storage:
  type: MYSQL

mysql:
  host: 127.0.0.1
  port: 3306
  database: hlogin
  username: root
  password: sua_senha
  table-prefix: hlogin_
  auto-create-tables: true
  auto-migrate: true
  charset: utf8mb4
```

Recomendação: manter `auto-migrate: true` para atualização automática de estrutura.

## 🧾 Comandos

- `/login <senha>`
- `/register <senha> <senha>`
- `/hlogin help`
- `/hlogin reload`
- `/hlogin status`
- `/hlogin user <player>`
- `/hlogin unregister <player>`
- `/hlogin resetpassword <player>`
- `/hlogin setlang <player> <language>`
- `/hlogin forcecaptcha <player>`
- `/hlogin debug`

## 🔑 Permissões principais

- `hlogin.command.login`
- `hlogin.command.register`
- `hlogin.admin`
- `hlogin.reload`
- `hlogin.status`
- `hlogin.user`
- `hlogin.unregister`
- `hlogin.resetpassword`
- `hlogin.setlang`
- `hlogin.forcecaptcha`
- `hlogin.debug`
- `hlogin.bypass.captcha`
- `hlogin.bypass.loginblock`
- `hlogin.bypass.commandblock`
- `hlogin.bypass.movement`

## 🛠️ Build local

```bash
mvn clean package
```

Artefato principal: módulo `hlogin-bukkit`.

## 🗂️ Arquivos de configuração

- `config.yml`: autenticação, segurança, captcha, proteção, visual
- `setup.yml`: storage e idioma padrão
- `api.yml`: webhook e integrações
- `custom.yml`: sobrescrita opcional por ambiente

## 📜 Licença

Uso proprietário/confidencial conforme cabeçalhos do projeto.
