# Menstrual Tracker

A comprehensive menstrual cycle tracking and prediction application built with Spring Boot 3.x + Vue 3.

## Tech Stack
- Backend: Spring Boot 3.4.x, Spring Security, Spring Data JPA, Flyway
- Database: MySQL 8.x
- Auth: JWT (jjwt 0.12.x)
- Cache/Limiting: Guava Cache + RateLimiter
- API Docs: SpringDoc OpenAPI (Swagger UI)
- Frontend: Vue 3 + Element Plus + Vite

## Quick Start
1. Start MySQL: `docker-compose up -d db`
2. Set required environment variables, for example:
   ```bash
   export JWT_SECRET="$(openssl rand -base64 48)"
   export MYSQL_PASSWORD="your_mysql_password"
   export REDIS_PASSWORD="your_redis_password"
   export ADMIN_USERNAME="admin"
   export ADMIN_PASSWORD="initial-admin-password"
   ```
3. Run backend: `cd backend && mvn spring-boot:run`
4. Run frontend: `cd frontend && npm install && npm run dev`

API: http://localhost:8080 | Swagger UI: http://localhost:8080/swagger-ui.html

## Docker
`docker-compose up -d` (starts MySQL + backend)

Before `docker-compose up`, create a `.env` file or export these values:

```bash
MYSQL_ROOT_PASSWORD=your_mysql_password
REDIS_PASSWORD=your_redis_password
JWT_SECRET=your_random_base64_secret
ADMIN_USERNAME=admin
ADMIN_PASSWORD=initial-admin-password
WECHAT_APP_ID=your_wechat_appid
WECHAT_APP_SECRET=your_wechat_appsecret
```

The compose file fails fast when any of these are missing. No fixed default
passwords are committed to the repository.

## 管理员使用说明

### 第一次使用
1. 首次部署时通过 `ADMIN_USERNAME` 和 `ADMIN_PASSWORD` 提供初始管理员账号。
2. 后端启动时只会在没有任何管理员账号的情况下创建一次初始管理员，并且不会修改或提权已有用户。
3. 使用初始账号登录管理后台或小程序管理员入口。
4. 登录接口返回 `mustChangePassword=true`，前端会强制跳到“修改密码”页面。
5. 修改密码后，后端清除 `password_change_required` 标记并重新签发 token，之后才能正常使用后台功能。

### 之后使用
1. 使用修改后的密码正常登录，不再要求改密。
2. 如果管理员需要主动修改密码，可以在后台右上角菜单进入“修改密码”页面。
3. 如果管理员忘记密码，可由其他管理员在“管理员管理/用户管理”中重置，或由运维人员通过受控脚本重置；系统不会再自动重置任何管理员密码。

## Required Environment Variables
| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | Base64-encoded HMAC secret, required, no default |
| `MYSQL_PASSWORD` / `MYSQL_ROOT_PASSWORD` | Database password, required, no default |
| `REDIS_PASSWORD` | Redis password, required, no default |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Initial admin bootstrap, optional but recommended |
| `TRUSTED_PROXIES` | Comma-separated trusted proxy IPs/CIDRs, defaults to loopback |
| `WECHAT_APP_ID` / `WECHAT_APP_SECRET` | WeChat mini program credentials, required for production |

## Key API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register |
| POST | /api/auth/login | Login |
| GET | /api/records | List records |
| POST | /api/records | Create record |
| GET | /api/predictions | Get prediction |
| GET | /api/statistics/overview | Get statistics |
| GET | /api/export/csv | Export CSV |

Response format: `{ "code": 0, "message": "success", "data": {}, "timestamp": 123 }`

## Features
- User registration/login with JWT auth
- Menstrual record CRUD with cycle day auto-calculation
- Next period, ovulation, fertile window, and safe period prediction
- Custom symptom and emotion tags
- Statistics: averages, min/max, trends, symptom frequency
- Upcoming period reminders
- CSV data export
- Soft delete, rate limiting, request tracing
- Flyway database migrations
- Multi-environment config (dev/prod)
