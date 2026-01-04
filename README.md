# Billboard & Hoarding Management System - Backend API

A comprehensive **Spring Boot 3.3.5** backend application for managing billboard advertising operations. This platform connects billboard owners with advertisers, enabling seamless booking, payment processing, and campaign management.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Security](#security)
- [Payment Integration](#payment-integration)
- [Performance Optimizations](#performance-optimizations)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)

---

## Overview

The Billboard & Hoarding Management System is a multi-tenant platform that serves three primary user roles:

| Role | Description |
|------|-------------|
| **Admin** | Platform administrator who manages users, monitors transactions, approves KYC, and controls platform settings |
| **Owner** | Billboard owners who list their billboards, manage bookings, and receive payments |
| **Advertiser** | Businesses/individuals who browse billboards, create bookings, and run advertising campaigns |

---

## Features

### Authentication & Security
- JWT-based authentication with refresh tokens
- Two-Factor Authentication (2FA) with TOTP
- Magic link passwordless login
- Recovery codes for account recovery
- Login history tracking with device/location info
- Rate limiting on sensitive endpoints
- Password encryption with BCrypt

### Admin Features
- **Dashboard**: Real-time statistics and analytics
- **User Management**: View, block/unblock users, manage KYC approvals
- **Billboard Management**: Monitor all billboards, block/unblock listings
- **Booking Management**: View and manage all platform bookings
- **Wallet & Payouts**: Track platform commission, process owner payouts
- **Platform Settings**: Configure commission rates, GST percentages
- **Analytics**: Revenue charts, booking trends, top advertisers

### Owner Features
- **Billboard Management**: Create, update, delete billboards with images
- **Booking Management**: Approve/reject booking requests, apply discounts
- **Earnings Dashboard**: Track revenue, view payment history
- **Wallet System**: View balance, request payouts
- **Bank Account Management**: Add/update bank details for payouts
- **Analytics**: Revenue heatmaps, booking calendar, performance metrics

### Advertiser Features
- **Billboard Discovery**: Browse available billboards with filters
- **Booking System**: Book billboards with date selection
- **Campaign Management**: Create and track advertising campaigns
- **Price Preview**: Real-time pricing calculation before booking
- **Favorites**: Save billboards for later
- **Payment Integration**: Secure payments via Razorpay
- **Invoice Generation**: Download GST-compliant invoices

### Payment System
- **Razorpay Integration**: Secure payment processing
- **Payment Splits**: Automatic commission calculation
- **Refund Management**: Process refunds for cancellations
- **Invoice Generation**: GST-compliant PDF invoices
- **Webhook Support**: Real-time payment status updates

---

## Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | Spring Boot 3.3.5 |
| **Language** | Java 21 |
| **Database** | MySQL 8.0 |
| **ORM** | Spring Data JPA / Hibernate |
| **Security** | Spring Security + JWT |
| **2FA** | TOTP (Time-based One-Time Password) |
| **Payment** | Razorpay |
| **PDF Generation** | OpenPDF |
| **Rate Limiting** | Bucket4j |
| **Caching** | Spring Cache (ConcurrentMapCache) |
| **Connection Pool** | HikariCP |
| **Build Tool** | Maven |
| **Email** | Spring Mail (SMTP) |

---

## Architecture

```
+-----------------------------------------------------------------+
|                         CLIENT LAYER                             |
|              (Angular Frontend / Mobile App)                     |
+-----------------------------------------------------------------+
                                |
                                v
+-----------------------------------------------------------------+
|                      API GATEWAY / FILTERS                       |
|         +------------+  +------------+  +------------+          |
|         | JWT Filter |  |Rate Limiter|  |CORS Filter |          |
|         +------------+  +------------+  +------------+          |
+-----------------------------------------------------------------+
                                |
                                v
+-----------------------------------------------------------------+
|                      CONTROLLER LAYER                            |
|  +---------+ +---------+ +---------+ +---------+ +---------+    |
|  |  Auth   | |  Admin  | |  Owner  | |Advertiser| | Payment |   |
|  +---------+ +---------+ +---------+ +---------+ +---------+    |
+-----------------------------------------------------------------+
                                |
                                v
+-----------------------------------------------------------------+
|                       SERVICE LAYER                              |
|         (Business Logic, Validation, Transaction Mgmt)           |
+-----------------------------------------------------------------+
                                |
                                v
+-----------------------------------------------------------------+
|                     REPOSITORY LAYER                             |
|              (Spring Data JPA Repositories)                      |
+-----------------------------------------------------------------+
                                |
                                v
+-----------------------------------------------------------------+
|                       DATABASE LAYER                             |
|                     (MySQL + HikariCP)                           |
+-----------------------------------------------------------------+
```

---

## Project Structure

```
src/main/java/com/billboarding/
|-- BillBoardingAndHordingApplication.java    # Main Application
|
|-- configs/                                   # Configuration Classes
|   |-- CacheConfig.java                      # Spring Cache configuration
|   |-- CustomUserDetailsService.java         # User authentication
|   |-- JacksonConfig.java                    # JSON serialization (Hibernate)
|   |-- JwtAuthFilter.java                    # JWT authentication filter
|   |-- RateLimitConfig.java                  # Bucket4j rate limiting
|   |-- RateLimitInterceptor.java             # Rate limit interceptor
|   |-- SecurityConfig.java                   # Spring Security config
|   +-- WebMvcConfig.java                     # MVC configuration
|
|-- Controller/                                # REST Controllers
|   |-- AUTH/                                 # Authentication endpoints
|   |   |-- AuthController.java               # Login, Register, 2FA
|   |   |-- AdminUserController.java          # Admin user management
|   |   +-- UserController.java               # User profile
|   |
|   |-- Admin/                                # Admin endpoints
|   |   |-- AdminController.java              # User management
|   |   |-- AdminDashboardController.java     # Dashboard stats
|   |   |-- AdminAnalyticsController.java     # Analytics & charts
|   |   |-- AdminBillboardController.java     # Billboard management
|   |   |-- PlatformSettingsController.java   # Platform config
|   |   |-- Dashboard/
|   |   |   +-- AdminDashboardChartController.java
|   |   +-- Wallet/
|   |       |-- AdminWalletController.java    # Commission wallet
|   |       |-- AdminPayoutController.java    # Payout management
|   |       +-- AdminBankAccountController.java
|   |
|   |-- Owner/                                # Owner endpoints
|   |   |-- OwnerController.java              # Billboard CRUD
|   |   |-- OwnerBookingController.java       # Booking management
|   |   |-- OwnerDashboardController.java     # Dashboard
|   |   |-- OwnerAnalyticsController.java     # Analytics
|   |   |-- OwnerRevenueController.java       # Revenue tracking
|   |   |-- OwnerMapController.java           # Heatmap data
|   |   |-- Bank/
|   |   |   +-- OwnerBankAccountController.java
|   |   |-- Wallet/
|   |   |   |-- OwnerWalletController.java
|   |   |   +-- OwnerPayoutController.java
|   |   +-- Setting/
|   |       |-- OwnerProfileController.java
|   |       |-- OwnerSecurityController.java
|   |       +-- OwnerNotificationController.java
|   |
|   |-- Advertiser/                           # Advertiser endpoints
|   |   |-- AdvertiserController.java         # Bookings, Favorites
|   |   |-- AdvertiserDashboardController.java
|   |   |-- AdvertiserMapController.java
|   |   |-- CampaignController.java           # Campaign CRUD
|   |   +-- CampaignAnalyticsController.java
|   |
|   |-- Payment/                              # Payment endpoints
|   |   |-- PaymentController.java            # Razorpay integration
|   |   |-- RazorpayWebhookController.java    # Webhook handler
|   |   |-- InvoiceController.java            # Invoice generation
|   |   |-- RefundController.java             # Refund processing
|   |   +-- PaymentSplitAdminController.java
|   |
|   |-- Security/                             # Security endpoints
|   |   |-- SecurityControllers.java          # Login history
|   |   |-- RecoveryController.java           # Recovery codes
|   |   |-- TwoFactorResetController.java     # 2FA reset
|   |   +-- UserSecurityController.java
|   |
|   +-- Availability/                         # Availability check
|       +-- AdvertiserAvailabilityController.java
|
|-- DTO/                                       # Data Transfer Objects
|   |-- ADMIN/                                # Admin DTOs
|   |-- Advertiser/                           # Advertiser DTOs
|   |-- Booking/                              # Booking DTOs
|   |-- OWNER/                                # Owner DTOs
|   |-- Payment/                              # Payment DTOs
|   |-- ApiResponse.java                      # Standard response wrapper
|   |-- AuthResponse.java                     # Auth response
|   +-- LoginRequest.java                     # Login request
|
|-- Entity/                                    # JPA Entities
|   |-- User.java                             # User entity
|   |-- Admin/
|   |   |-- PlatformSettings.java             # Platform configuration
|   |   +-- wallet/                           # Admin wallet entities
|   |-- Advertiser/
|   |   |-- Campaign.java                     # Campaign entity
|   |   |-- CampaignBooking.java
|   |   +-- FavouriteBillboard.java
|   |-- Bookings/
|   |   |-- Booking.java                      # Main booking entity
|   |   +-- BookingAudit.java                 # Audit trail
|   |-- OWNER/
|   |   |-- Billboard.java                    # Billboard entity
|   |   |-- bank/                             # Bank account entity
|   |   |-- wallet/                           # Owner wallet entities
|   |   +-- profile/                          # Owner profile
|   |-- Payment/
|   |   |-- PaymentHistory.java               # Payment records
|   |   |-- Invoice.java                      # Invoice entity
|   |   +-- PaymentSplit.java                 # Commission splits
|   +-- Security/
|       |-- LoginHistory.java                 # Login tracking
|       |-- TwoFactorOTP.java                 # 2FA tokens
|       |-- RecoveryCode.java                 # Recovery codes
|       +-- MagicLinkToken.java               # Magic link tokens
|
|-- ENUM/                                      # Enumerations
|   |-- UserRole.java                         # ADMIN, OWNER, ADVERTISER
|   |-- BookingStatus.java                    # PENDING, APPROVED, etc.
|   |-- PaymentStatus.java                    # Payment states
|   |-- KycStatus.java                        # KYC states
|   +-- CampaignStatus.java                   # Campaign states
|
|-- Exception/                                 # Exception Handling
|   |-- GlobalExceptionHandler.java           # Central exception handler
|   |-- ApiError.java                         # Error response format
|   |-- BusinessException.java                # Business logic exceptions
|   |-- ResourceNotFoundException.java
|   |-- ValidationException.java
|   |-- UnauthorizedException.java
|   +-- ForbiddenException.java
|
|-- Repository/                                # Data Access Layer
|   |-- UserRepository.java
|   |-- ADMIN/                                # Admin repositories
|   |-- Advertiser/                           # Advertiser repositories
|   |-- BillBoard/
|   |   +-- BillboardRepository.java
|   |-- Booking/
|   |   +-- BookingRepository.java
|   |-- Owner/                                # Owner repositories
|   |-- Payment/                              # Payment repositories
|   +-- Security/                             # Security repositories
|
+-- Services/                                  # Business Logic
    |-- UserService.java
    |-- Admin/                                # Admin services
    |   |-- AdminDashboardService.java
    |   |-- AdminAnalyticsService.java
    |   |-- PlatformSettingsService.java
    |   +-- Wallet/
    |-- Advertiser/                           # Advertiser services
    |   |-- AdvertiserDashboardService.java
    |   |-- CampaignService.java
    |   +-- CampaignAnalyticsService.java
    |-- Auth/
    |   +-- AuthService.java                  # Authentication service
    |-- BillBoard/
    |   +-- BillboardService.java
    |-- BookingService/
    |   |-- BookingService.java               # Core booking logic
    |   +-- OwnerBooking.java
    |-- Owner/                                # Owner services
    |   |-- OwnerDashboardService.java
    |   |-- OwnerRevenueReportService.java
    |   +-- Wallet/
    |-- Payment/                              # Payment services
    |   |-- PaymentService.java               # Razorpay integration
    |   |-- InvoiceService.java
    |   |-- GstInvoicePdfService.java         # PDF generation
    |   +-- PaymentSplitService.java
    |-- SmartPricing/
    |   +-- SmartPricingService.java          # Dynamic pricing
    |-- Tax/
    |   +-- GstService.java                   # GST calculations
    +-- security/                             # Security services
        |-- SecurityService.java
        |-- TwoFactorService.java
        |-- RecoveryCodeService.java
        |-- MagicLinkService.java
        +-- LoginAttemptService.java
```

---

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **MySQL 8.0+**
- **Git**

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/sarthakpawar0912/BillBoard-Application-.git
   cd BillBoard-Application-
   ```

2. **Create MySQL database**
   ```sql
   CREATE DATABASE billboardingdb;
   ```

3. **Configure application properties**

   Update `src/main/resources/application.properties`:
   ```properties
   # Database
   spring.datasource.url=jdbc:mysql://localhost:3306/billboardingdb
   spring.datasource.username=your_username
   spring.datasource.password=your_password

   # JWT Secret (change in production!)
   jwt.secret=YourSuperSecretKeyHere123456789

   # Email (for OTP/notifications)
   spring.mail.username=your_email@gmail.com
   spring.mail.password=your_app_password

   # Razorpay (get from dashboard.razorpay.com)
   razorpay.key_id=rzp_test_xxx
   razorpay.key_secret=your_secret
   ```

4. **Build the project**
   ```bash
   ./mvnw clean install
   ```

5. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

6. **Access the API**
   ```
   http://localhost:8080
   ```

---

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Database connection URL | `jdbc:mysql://localhost:3306/billboardingdb` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | - |
| `JWT_SECRET` | Secret key for JWT signing | - |
| `JWT_EXPIRATION` | Token expiration time (ms) | `86400000` (24h) |
| `RAZORPAY_KEY_ID` | Razorpay API Key | - |
| `RAZORPAY_KEY_SECRET` | Razorpay Secret | - |
| `PLATFORM_COMMISSION_PERCENT` | Platform commission % | `15` |

### HikariCP Connection Pool

```properties
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.pool-name=BillboardHikariPool
```

---

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/register` | Register new user |
| `POST` | `/auth/login` | Login with email/password |
| `POST` | `/auth/verify-otp` | Verify 2FA OTP |
| `POST` | `/auth/setup-2fa` | Enable 2FA |
| `POST` | `/auth/magic-link/request` | Request magic link |
| `POST` | `/auth/magic-link/verify` | Verify magic link |
| `POST` | `/auth/recovery/verify` | Use recovery code |
| `POST` | `/auth/change-password` | Change password |

### Admin Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/admin/dashboard/stats` | Get dashboard statistics |
| `GET` | `/admin/users` | List all users |
| `POST` | `/admin/users/{id}/block` | Block user |
| `POST` | `/admin/users/{id}/unblock` | Unblock user |
| `GET` | `/admin/kyc/pending` | Get pending KYC requests |
| `POST` | `/admin/kyc/{id}/approve` | Approve KYC |
| `POST` | `/admin/kyc/{id}/reject` | Reject KYC |
| `GET` | `/admin/billboards` | List all billboards |
| `POST` | `/admin/billboards/{id}/block` | Block billboard |
| `GET` | `/admin/analytics/revenue-chart` | Revenue chart data |
| `GET` | `/admin/analytics/top-advertisers` | Top advertisers |
| `GET` | `/admin/wallet` | Admin wallet balance |
| `GET` | `/admin/payouts` | Pending payouts |
| `POST` | `/admin/payouts/{id}/approve` | Approve payout |
| `GET` | `/admin/settings` | Platform settings |
| `PUT` | `/admin/settings` | Update settings |

### Owner Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/owner/billboards` | List my billboards |
| `POST` | `/owner/billboards` | Create billboard |
| `PUT` | `/owner/billboards/{id}` | Update billboard |
| `DELETE` | `/owner/billboards/{id}` | Delete billboard |
| `POST` | `/owner/billboards/{id}/upload-images` | Upload images |
| `POST` | `/owner/billboards/{id}/toggle-availability` | Toggle availability |
| `GET` | `/owner/bookings` | List my bookings |
| `POST` | `/owner/bookings/{id}/approve` | Approve booking |
| `POST` | `/owner/bookings/{id}/reject` | Reject booking |
| `POST` | `/owner/bookings/{id}/discount` | Apply discount |
| `GET` | `/owner/dashboard` | Dashboard stats |
| `GET` | `/owner/wallet` | Wallet balance |
| `GET` | `/owner/wallet/transactions` | Transaction history |
| `POST` | `/owner/payouts/request` | Request payout |
| `GET` | `/owner/bank-accounts` | List bank accounts |
| `POST` | `/owner/bank-accounts` | Add bank account |

### Advertiser Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/advertiser/billboards` | Browse available billboards |
| `GET` | `/advertiser/billboards/{id}` | Get billboard details |
| `GET` | `/advertiser/billboards/{id}/availability` | Check availability |
| `GET` | `/advertiser/bookings/price-preview` | Get price preview |
| `POST` | `/advertiser/bookings` | Create booking |
| `GET` | `/advertiser/bookings` | My bookings |
| `POST` | `/advertiser/bookings/{id}/cancel` | Cancel booking |
| `GET` | `/advertiser/favourites` | My favorites |
| `POST` | `/advertiser/favourites/{billboardId}` | Add to favorites |
| `DELETE` | `/advertiser/favourites/{id}` | Remove favorite |
| `GET` | `/advertiser/campaigns` | List campaigns |
| `POST` | `/advertiser/campaigns` | Create campaign |
| `GET` | `/advertiser/campaigns/{id}/analytics` | Campaign analytics |
| `GET` | `/advertiser/dashboards` | Dashboard |

### Payment Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/payments/create-order` | Create Razorpay order |
| `POST` | `/payments/verify` | Verify payment |
| `POST` | `/payments/webhook` | Razorpay webhook |
| `GET` | `/payments/history` | Payment history |
| `POST` | `/payments/refund/{bookingId}` | Request refund |
| `GET` | `/invoices/{bookingId}` | Get invoice |
| `GET` | `/invoices/{bookingId}/download` | Download PDF |

### Example Requests

**Register User**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "role": "ADVERTISER"
  }'
```

**Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

**Create Booking**
```bash
curl -X POST http://localhost:8080/api/advertiser/bookings \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "billboardId": 1,
    "startDate": "2024-02-01",
    "endDate": "2024-02-07"
  }'
```

---

## Database Schema

### Core Entities

```
+------------------+       +------------------+       +------------------+
|     users        |       |   billboards     |       |    bookings      |
+------------------+       +------------------+       +------------------+
| id               |       | id               |       | id               |
| name             |       | title            |       | advertiser_id    |
| email            |<------| owner_id         |<------| billboard_id     |
| password         |       | location         |       | start_date       |
| role             |       | price_per_day    |       | end_date         |
| kyc_status       |       | type             |       | status           |
| blocked          |       | available        |       | total_price      |
| two_factor       |       | latitude         |       | payment_status   |
+------------------+       | longitude        |       | rzp_order_id     |
                           | image_paths      |       | commission_amt   |
                           | admin_blocked    |       | gst_amount       |
                           +------------------+       +------------------+
                                                             |
                                                             v
+------------------+       +------------------+       +------------------+
|   campaigns      |       | payment_history  |       |    invoices      |
+------------------+       +------------------+       +------------------+
| id               |       | id               |       | id               |
| name             |       | booking_id       |       | booking_id       |
| advertiser_id    |       | razorpay_id      |       | invoice_number   |
| budget           |       | amount           |       | amount           |
| spent            |       | status           |       | gst_amount       |
| status           |       | paid_at          |       | generated_at     |
| start_date       |       | refund_status    |       | pdf_path         |
| end_date         |       +------------------+       +------------------+
+------------------+
```

### Indexes

The following database indexes are automatically created for query optimization:

```sql
-- Users
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_kyc ON users(kyc_status);
CREATE INDEX idx_user_blocked ON users(blocked);

-- Billboards
CREATE INDEX idx_billboard_owner ON billboards(owner_id);
CREATE INDEX idx_billboard_available ON billboards(available);
CREATE INDEX idx_billboard_location ON billboards(latitude, longitude);
CREATE INDEX idx_billboard_type ON billboards(type);

-- Bookings
CREATE INDEX idx_booking_advertiser ON bookings(advertiser_id);
CREATE INDEX idx_booking_billboard ON bookings(billboard_id);
CREATE INDEX idx_booking_status ON bookings(status);
CREATE INDEX idx_booking_dates ON bookings(start_date, end_date);
CREATE INDEX idx_booking_payment ON bookings(payment_status);
```

---

## Security

### Authentication Flow

```
+----------+      +----------+      +----------+      +----------+
|  Client  |      |   API    |      |  Auth    |      | Database |
+----+-----+      +----+-----+      +----+-----+      +----+-----+
     |                 |                 |                 |
     |  POST /login    |                 |                 |
     |---------------->|                 |                 |
     |                 |  Validate       |                 |
     |                 |---------------->|                 |
     |                 |                 |  Check User     |
     |                 |                 |---------------->|
     |                 |                 |<----------------|
     |                 |                 |                 |
     |                 |  If 2FA Enabled |                 |
     |                 |<----------------|                 |
     |  OTP Required   |                 |                 |
     |<----------------|                 |                 |
     |                 |                 |                 |
     |  POST /verify   |                 |                 |
     |---------------->|                 |                 |
     |                 |  Verify OTP     |                 |
     |                 |---------------->|                 |
     |                 |                 |                 |
     |                 |  Generate JWT   |                 |
     |                 |<----------------|                 |
     |  JWT Token      |                 |                 |
     |<----------------|                 |                 |
```

### JWT Token Structure

```json
{
  "sub": "user@example.com",
  "role": "ADVERTISER",
  "userId": 123,
  "iat": 1704067200,
  "exp": 1704153600
}
```

### Rate Limiting

| Endpoint Type | Limit | Window |
|---------------|-------|--------|
| Authentication | 10 requests | 1 minute |
| General API | 100 requests | 1 minute |

### Security Headers

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
```

---

## Payment Integration

### Razorpay Flow

```
+----------+     +----------+     +----------+     +----------+
|  Client  |     | Backend  |     | Razorpay |     | Webhook  |
+----+-----+     +----+-----+     +----+-----+     +----+-----+
     |                |                |                |
     | Create Order   |                |                |
     |--------------->|                |                |
     |                | Create Order   |                |
     |                |--------------->|                |
     |                |<---------------|                |
     | Order ID       |                |                |
     |<---------------|                |                |
     |                |                |                |
     | Pay via Razorpay Checkout       |                |
     |-------------------------------->|                |
     |                |                |                |
     | Payment Success|                |                |
     |<--------------------------------|                |
     |                |                |                |
     | Verify Payment |                |                |
     |--------------->|                |                |
     |                | Verify Signature                |
     |                |--------------->|                |
     |                |                |  Webhook       |
     |                |                |--------------->|
     |                |                |                | Update DB
     |                |                |                |
     | Booking Confirmed               |                |
     |<---------------|                |                |
```

### Price Calculation

```
Original Base Amount = Billboard Price x Number of Days
                            |
                            v
Apply Discount (Owner can give 0-50% off)
Discounted Amount = Original - (Original x Discount%)
                            |
                            v
Add Platform Commission (Default 15%)
Subtotal = Discounted Amount + Commission
                            |
                            v
Add GST (18%)
Total = Subtotal x 1.18
```

---

## Performance Optimizations

### 1. FetchType.LAZY
All entity relationships use `FetchType.LAZY` to prevent N+1 query problems.

### 2. JOIN FETCH Queries
Custom repository queries with JOIN FETCH for eager loading when needed:
```java
@Query("SELECT b FROM Booking b LEFT JOIN FETCH b.billboard LEFT JOIN FETCH b.advertiser WHERE b.id = :id")
Optional<Booking> findByIdWithDetails(@Param("id") Long id);
```

### 3. COUNT Queries
Using COUNT queries instead of loading full entity lists:
```java
long countByStatus(BookingStatus status);
```

### 4. Caching
Platform settings are cached to reduce database hits:
```java
@Cacheable(value = "platformSettings", key = "'default'")
public PlatformSettings get() { ... }
```

### 5. Connection Pooling
HikariCP configured for optimal connection management.

### 6. Database Indexes
Strategic indexes on frequently queried columns.

---

## Error Handling

### Standard Error Response

```json
{
  "status": 400,
  "message": "Validation failed",
  "path": "/api/advertiser/bookings",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2024-01-15T10:30:00",
  "fieldErrors": {
    "startDate": "Start date cannot be in the past",
    "endDate": "End date must be after start date"
  }
}
```

### Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `BAD_CREDENTIALS` | 401 | Invalid email/password |
| `ACCESS_DENIED` | 403 | Insufficient permissions |
| `RESOURCE_NOT_FOUND` | 404 | Entity not found |
| `ENDPOINT_NOT_FOUND` | 404 | API endpoint not found |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## Testing

### Run Tests
```bash
./mvnw test
```

### Test Coverage
```bash
./mvnw test jacoco:report
```

---

## Deployment

### Docker

```dockerfile
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
docker build -t billboard-backend .
docker run -p 8080:8080 billboard-backend
```

### Production Checklist

- [ ] Change `jwt.secret` to a strong random string
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Configure production database credentials
- [ ] Set Razorpay live keys
- [ ] Configure proper CORS origins
- [ ] Enable HTTPS
- [ ] Set up database backups
- [ ] Configure logging levels
- [ ] Set up monitoring (health endpoints)

---

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Contact

**Sarthak Pawar**
- GitHub: [@sarthakpawar0912](https://github.com/sarthakpawar0912)
- Email: pawarsr06@gmail.com

---

## Acknowledgments

- Spring Boot Team
- Razorpay for payment integration
- All contributors and testers

---

<p align="center">
  Made with Love by Sarthak Pawar
</p>
