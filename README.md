# Task Management API

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue)
![Redis](https://img.shields.io/badge/Redis-Caching-red)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-blue)

A robust and scalable REST API for task management with advanced features including workspace collaboration, real-time notifications, and comprehensive analytics.

## 🚀 Features

### Core Functionality
- **User Management** - Registration, authentication, profile management
- **Task Management** - Create, read, update, delete tasks with advanced filtering
- **Workspace Collaboration** - Team workspaces with role-based access control
- **Real-time Updates** - WebSocket support for live notifications

### Advanced Features
- **JWT Authentication** - Secure token-based authentication
- **Role-based Authorization** - Fine-grained access control (OWNER, ADMIN, MEMBER, VIEWER)
- **File Uploads** - Support for task attachments and user avatars
- **Search & Filtering** - Full-text search and advanced filtering capabilities
- **Analytics & Reporting** - Comprehensive workspace and user analytics
- **Export Functionality** - Export tasks in JSON, CSV, and Excel formats

## 🛠 Technology Stack

### Backend
- **Java 21** - Modern Java features and performance
- **Spring Boot 3.2** - Rapid application development framework
- **Spring Security 6** - Robust security and authentication
- **Spring Data JPA** - Database access and ORM
- **Hibernate 6** - Object-relational mapping

### Database & Caching
- **PostgreSQL 14+** - Primary relational database
- **Redis** - Caching and session storage
- **Liquibase** - Database migration management

## 📋 Quick Start

### Prerequisites
- Java 21
- Maven 3.6+
- PostgreSQL 14
- Redis 7

### Running with Docker
```bash
docker-compose up -d

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run with test coverage
mvn jacoco:report

# Run checkstyle
mvn checkstyle:check

# Run everything (tests, checkstyle, coverage)
mvn clean verify
