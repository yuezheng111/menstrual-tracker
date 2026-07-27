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
2. Run backend: `cd backend && mvn spring-boot:run`
3. Run frontend: `cd frontend && npm install && npm run dev`

API: http://localhost:8080 | Swagger UI: http://localhost:8080/swagger-ui.html

## Docker
`docker-compose up -d` (starts MySQL + backend)

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
