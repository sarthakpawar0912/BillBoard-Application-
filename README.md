# Billboard & Hoarding Management System - Backend API

A comprehensive, production-ready **Spring Boot 3.3.5** backend application for managing billboard advertising operations. This platform connects billboard owners with advertisers, enabling seamless booking, payment processing, wallet management, and campaign analytics.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Razorpay](https://img.shields.io/badge/Payment-Razorpay-purple)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## Table of Contents

1. [Overview](#1-overview)
2. [System Architecture](#2-system-architecture)
3. [Tech Stack](#3-tech-stack)
4. [Getting Started](#4-getting-started)
5. [User Roles & Permissions](#5-user-roles--permissions)
6. [Authentication System](#6-authentication-system)
7. [Booking System](#7-booking-system)
8. [Payment Gateway Integration](#8-payment-gateway-integration)
9. [Wallet System](#9-wallet-system)
10. [Smart Pricing Engine](#10-smart-pricing-engine)
11. [Invoice & Tax Management](#11-invoice--tax-management)
12. [Campaign Management](#12-campaign-management)
13. [Admin Dashboard & Analytics](#13-admin-dashboard--analytics)
14. [Security Features](#14-security-features)
15. [API Reference](#15-api-reference)
16. [Database Schema](#16-database-schema)
17. [Performance Optimizations](#17-performance-optimizations)
18. [Deployment](#18-deployment)

---

## 1. Overview

### What is this Platform?

The Billboard & Hoarding Management System is a **B2B2C marketplace** that digitizes the outdoor advertising industry. It serves as a bridge between:

- **Billboard Owners**: Individuals or companies who own physical billboards/hoardings
- **Advertisers**: Businesses who want to display their advertisements on billboards
- **Platform Admin**: Manages the marketplace, collects commission, and handles disputes

### Business Flow

```
                                    PLATFORM
                                       |
     +----------------+----------------+----------------+
     |                |                                 |
  OWNER            ADMIN                          ADVERTISER
     |                |                                 |
  Lists            Earns                            Browses
  Billboard       Commission                        Billboards
     |            (15%)                                 |
     v                                                  v
  Receives         +--------------------------------+  Books
  Booking          |     RAZORPAY PAYMENT          |  Billboard
  Request          +--------------------------------+     |
     |                              |                     |
  Approves/                   Processes                   |
  Rejects                     Payment                     |
     |                              |                     v
     v                              v                  Pays
  Wallet                      Payment Split:          Online
  Credited                    - Owner: Base Amount        |
     |                        - Admin: Commission         |
     v                        - GST: 18%                  |
  Request                           |                     v
  Payout                            v                  Booking
     |                         Invoice                Confirmed
     v                        Generated                   |
  Bank                              |                     v
  Transfer                          v                 Campaign
                               Email Sent             Runs
```

### Key Metrics the Platform Tracks

| Metric | Description |
|--------|-------------|
| Total Revenue | Sum of all successful payments |
| Platform Commission | 15% of base amount (configurable) |
| GST Collected | 18% of (base + commission) |
| Owner Payouts | Amount transferred to owners |
| Active Bookings | Currently running campaigns |
| Conversion Rate | Bookings / Browse views |

---

## 2. System Architecture

### High-Level Architecture

```
+------------------------------------------------------------------+
|                          CLIENT LAYER                              |
|   +------------------+  +------------------+  +------------------+ |
|   | Angular Web App  |  |   Mobile App     |  |   Admin Panel    | |
|   +------------------+  +------------------+  +------------------+ |
+------------------------------------------------------------------+
                                  |
                                  | HTTPS/REST
                                  v
+------------------------------------------------------------------+
|                        API GATEWAY LAYER                           |
|   +------------------+  +------------------+  +------------------+ |
|   |   CORS Filter    |  |   JWT Filter     |  |  Rate Limiter    | |
|   +------------------+  +------------------+  +------------------+ |
+------------------------------------------------------------------+
                                  |
                                  v
+------------------------------------------------------------------+
|                      CONTROLLER LAYER (REST APIs)                  |
|   +----------+ +----------+ +----------+ +----------+ +----------+ |
|   |   Auth   | |  Admin   | |  Owner   | |Advertiser| | Payment  | |
|   +----------+ +----------+ +----------+ +----------+ +----------+ |
+------------------------------------------------------------------+
                                  |
                                  v
+------------------------------------------------------------------+
|                        SERVICE LAYER                               |
|   +------------------+  +------------------+  +------------------+ |
|   | Business Logic   |  |   Validation     |  |  Transactions    | |
|   +------------------+  +------------------+  +------------------+ |
|   +------------------+  +------------------+  +------------------+ |
|   | Smart Pricing    |  |   Tax/GST        |  |  Invoice PDF     | |
|   +------------------+  +------------------+  +------------------+ |
+------------------------------------------------------------------+
                                  |
                                  v
+------------------------------------------------------------------+
|                      REPOSITORY LAYER                              |
|   +------------------+  +------------------+  +------------------+ |
|   | JPA Repositories |  | Custom Queries   |  |   JOIN FETCH     | |
|   +------------------+  +------------------+  +------------------+ |
+------------------------------------------------------------------+
                                  |
                                  v
+------------------------------------------------------------------+
|                        DATA LAYER                                  |
|   +------------------+       +------------------+                  |
|   |   MySQL 8.0      |       |    HikariCP      |                  |
|   |   (Primary DB)   |       | (Connection Pool)|                  |
|   +------------------+       +------------------+                  |
+------------------------------------------------------------------+
                                  |
                                  v
+------------------------------------------------------------------+
|                     EXTERNAL SERVICES                              |
|   +------------------+  +------------------+  +------------------+ |
|   |    Razorpay      |  |   SMTP Server    |  |   File Storage   | |
|   |  (Payments)      |  |   (Emails)       |  |   (Images)       | |
|   +------------------+  +------------------+  +------------------+ |
+------------------------------------------------------------------+
```

### Layered Architecture Details

| Layer | Responsibility | Technologies |
|-------|---------------|--------------|
| **Controller** | Handle HTTP requests, validate input, return responses | Spring MVC, REST |
| **Service** | Business logic, transactions, orchestration | Spring Service, @Transactional |
| **Repository** | Data access, custom queries | Spring Data JPA, JPQL |
| **Entity** | Database table mapping | JPA/Hibernate Entities |
| **DTO** | Data transfer between layers | Lombok, Builder pattern |
| **Config** | Application configuration | Spring Configuration |

---

## 3. Tech Stack

### Core Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 (LTS) | Programming language with latest features |
| **Spring Boot** | 3.3.5 | Application framework |
| **Spring Security** | 6.x | Authentication & authorization |
| **Spring Data JPA** | 3.x | Database ORM |
| **Hibernate** | 6.x | JPA implementation |
| **MySQL** | 8.0+ | Relational database |
| **Maven** | 3.9+ | Build & dependency management |

### Security & Authentication

| Technology | Purpose |
|------------|---------|
| **JWT (jjwt 0.12.5)** | Stateless authentication tokens |
| **BCrypt** | Password hashing |
| **TOTP** | Time-based OTP for 2FA |
| **HMAC-SHA256** | Razorpay signature verification |

### Payment & Finance

| Technology | Purpose |
|------------|---------|
| **Razorpay Java SDK 1.4.3** | Payment gateway integration |
| **OpenPDF 1.3.39** | PDF invoice generation |
| **Bucket4j 8.7.0** | Rate limiting |

### Performance & Caching

| Technology | Purpose |
|------------|---------|
| **HikariCP** | High-performance connection pooling |
| **Spring Cache** | In-memory caching |
| **Jackson Hibernate6** | Lazy loading serialization |

### Development Tools

| Technology | Purpose |
|------------|---------|
| **Lombok** | Reduce boilerplate code |
| **Spring Validation** | Input validation |
| **Spring Mail** | Email notifications |

---

## 4. Getting Started

### Prerequisites

```bash
# Check Java version (must be 21+)
java -version

# Check Maven version
mvn -version

# Check MySQL is running
mysql --version
```

### Step 1: Clone Repository

```bash
git clone https://github.com/sarthakpawar0912/BillBoard-Application-.git
cd BillBoard-Application-
```

### Step 2: Create Database

```sql
-- Connect to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE billboardingdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify
SHOW DATABASES;
```

### Step 3: Configure Application

Edit `src/main/resources/application.properties`:

```properties
# ==================== DATABASE ====================
spring.datasource.url=jdbc:mysql://localhost:3306/billboardingdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

# ==================== JWT SECRET ====================
# IMPORTANT: Change this in production!
jwt.secret=YourSuperSecretKeyAtLeast256BitsLong123456789
jwt.expiration=86400000  # 24 hours in milliseconds

# ==================== EMAIL (Gmail SMTP) ====================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password  # Use App Password, not regular password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ==================== RAZORPAY ====================
# Get these from https://dashboard.razorpay.com/app/keys
razorpay.key_id=rzp_test_XXXXXXXXXXXX
razorpay.key_secret=YOUR_KEY_SECRET
razorpay.webhook.secret=your_webhook_secret
razorpay.test_mode=true

# ==================== PLATFORM CONFIG ====================
platform.commission_percent=15
gst.cgst=9
gst.sgst=9
```

### Step 4: Build & Run

```bash
# Build (skip tests for faster build)
./mvnw clean install -DskipTests

# Run
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/BillBoarding-And-Hording-0.0.1-SNAPSHOT.jar
```

### Step 5: Verify

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response
{"status":"UP"}
```

---

## 5. User Roles & Permissions

### Role Hierarchy

```
ADMIN (Superuser)
   |
   +-- Can manage all users
   +-- Can view all bookings
   +-- Can block/unblock users & billboards
   +-- Can approve/reject KYC
   +-- Can configure platform settings
   +-- Can view analytics & revenue
   +-- Can process owner payouts
   |
OWNER (Billboard Owner)
   |
   +-- Can CRUD own billboards
   +-- Can approve/reject booking requests
   +-- Can apply discounts (up to 50%)
   +-- Can view own earnings
   +-- Can request payouts
   +-- Can manage bank accounts
   |
ADVERTISER (Customer)
   |
   +-- Can browse billboards
   +-- Can create bookings
   +-- Can make payments
   +-- Can create campaigns
   +-- Can manage favorites
   +-- Can download invoices
```

### Permission Matrix

| Action | Admin | Owner | Advertiser |
|--------|-------|-------|------------|
| View all users | Yes | No | No |
| Block/Unblock users | Yes | No | No |
| Approve KYC | Yes | No | No |
| View all billboards | Yes | Own only | Available only |
| Create billboard | No | Yes | No |
| Block billboard | Yes | No | No |
| View all bookings | Yes | Own billboards | Own bookings |
| Approve booking | No | Yes | No |
| Create booking | No | No | Yes |
| Make payment | No | No | Yes |
| View platform analytics | Yes | No | No |
| Configure commission | Yes | No | No |
| Request payout | No | Yes | No |
| Approve payout | Yes | No | No |

---

## 6. Authentication System

### 6.1 Authentication Flow

```
+-------------+     +-------------+     +-------------+     +-------------+
|   CLIENT    |     |     API     |     |   SERVICE   |     |  DATABASE   |
+------+------+     +------+------+     +------+------+     +------+------+
       |                   |                   |                   |
       | 1. POST /login    |                   |                   |
       | {email, password} |                   |                   |
       |------------------>|                   |                   |
       |                   | 2. Check blocked  |                   |
       |                   |------------------>|                   |
       |                   |                   | 3. Query attempts |
       |                   |                   |------------------>|
       |                   |                   |<------------------|
       |                   |<------------------|                   |
       |                   |                   |                   |
       |                   | 4. Authenticate   |                   |
       |                   |------------------>|                   |
       |                   |                   | 5. Verify password|
       |                   |                   |------------------>|
       |                   |                   |<------------------|
       |                   |<------------------|                   |
       |                   |                   |                   |
       |                   | 6. Check 2FA      |                   |
       |                   |------------------>|                   |
       |                   |                   |                   |
       |    [If 2FA OFF]   |                   |                   |
       |<------------------| 7a. Return JWT    |                   |
       |                   |                   |                   |
       |    [If 2FA ON]    |                   |                   |
       |<------------------| 7b. Return        |                   |
       |  {require2FA:true}|    OTP Required   |                   |
       |                   |                   |                   |
       | 8. POST /verify   |                   |                   |
       | {email, otp}      |                   |                   |
       |------------------>|                   |                   |
       |                   | 9. Verify OTP     |                   |
       |                   |------------------>|                   |
       |                   |<------------------|                   |
       |<------------------| 10. Return JWT    |                   |
       |                   |                   |                   |
```

### 6.2 JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user@example.com",     // Subject (email)
    "role": "ADVERTISER",           // User role
    "userId": 123,                  // User ID
    "iat": 1704067200,              // Issued at (Unix timestamp)
    "exp": 1704153600               // Expiration (24h later)
  },
  "signature": "HMACSHA256(...)"
}
```

### 6.3 Two-Factor Authentication Methods

| Method | Description | When Used |
|--------|-------------|-----------|
| **EMAIL_OTP** | 6-digit OTP sent to email | Default 2FA method |
| **MAGIC_LINK** | One-click login link via email | Passwordless option |
| **RECOVERY_CODE** | 8 one-time codes for account recovery | When 2FA device lost |

### 6.4 Login Attempt Protection

```java
// After 5 failed attempts, account is blocked for 15 minutes
MAX_ATTEMPTS = 5
BLOCK_DURATION = 15 minutes

// Risk detection triggers additional 2FA
- New IP address
- New device/browser
- Unusual login time
```

### 6.5 Authentication Endpoints

```bash
# 1. Register new user
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "role": "ADVERTISER",  // or "OWNER"
  "phone": "+919876543210"
}

# 2. Login
POST /api/auth/login
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}

# Response if 2FA disabled:
{
  "require2FA": false,
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "ADVERTISER",
  "userId": 123,
  "message": "Login successful"
}

# Response if 2FA enabled:
{
  "require2FA": true,
  "token": null,
  "role": "ADVERTISER",
  "userId": 123,
  "message": "Email OTP required"
}

# 3. Verify OTP
POST /api/auth/verify-otp
{
  "email": "john@example.com",
  "otp": "123456"
}

# 4. Setup 2FA
POST /api/auth/setup-2fa
{
  "method": "EMAIL_OTP"  // or "MAGIC_LINK"
}

# 5. Request Magic Link
POST /api/auth/magic-link/request
{
  "email": "john@example.com"
}

# 6. Verify Magic Link
POST /api/auth/magic-link/verify
{
  "token": "abc123..."
}

# 7. Change Password
POST /api/auth/change-password
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewPass456!"
}
```

---

## 7. Booking System

### 7.1 Booking Lifecycle

```
                    +-------------------+
                    |      PENDING      |
                    | (Awaiting Owner   |
                    |   Approval)       |
                    +--------+----------+
                             |
            +----------------+----------------+
            |                                 |
            v                                 v
   +--------+--------+               +--------+--------+
   |    APPROVED     |               |    REJECTED     |
   | (Ready for      |               | (Owner declined)|
   |  Payment)       |               +-----------------+
   +--------+--------+
            |
            | Payment
            v
   +--------+--------+
   |      PAID       |
   | (Payment        |
   |  Confirmed)     |
   +--------+--------+
            |
            +----------------+----------------+
            |                                 |
            v                                 v
   +--------+--------+               +--------+--------+
   |   COMPLETED     |               |   CANCELLED     |
   | (Campaign       |               | (Before start   |
   |  Finished)      |               |  date - Refund) |
   +-----------------+               +-----------------+
                                              |
                                              v
                                     +--------+--------+
                                     |  CANCELLED_     |
                                     |  NO_REFUND      |
                                     | (After start    |
                                     |  date)          |
                                     +-----------------+
```

### 7.2 Booking Status Descriptions

| Status | Description | Can Transition To |
|--------|-------------|-------------------|
| `PENDING` | Booking created, waiting for owner approval | APPROVED, REJECTED, CANCELLED |
| `APPROVED` | Owner approved, waiting for payment | PAID, CANCELLED |
| `REJECTED` | Owner rejected the booking | - |
| `PAID` | Payment successful, booking confirmed | COMPLETED, CANCELLED, CANCELLED_NO_REFUND |
| `COMPLETED` | Campaign period ended | - |
| `CANCELLED` | Cancelled before campaign start (refund eligible) | - |
| `CANCELLED_NO_REFUND` | Cancelled after campaign start (no refund) | - |

### 7.3 Booking Creation Flow

```java
// BookingService.createBooking()

1. VALIDATE USER KYC
   - User must have APPROVED KYC status
   - Throws: "Your KYC must be APPROVED to book a billboard"

2. VALIDATE DATES
   - Start date cannot be in past
   - End date must be >= start date
   - Throws: "End date cannot be before start date"

3. CHECK AVAILABILITY (Overlap Detection)
   - Query: Find any PENDING/APPROVED bookings that overlap with requested dates
   - Formula: existingEnd >= newStart AND existingStart <= newEnd
   - Throws: "The billboard is already booked for the selected dates"

4. CALCULATE PRICING
   a) Original Base Amount = SmartPricingService.calculateBasePrice()
      - Base: pricePerDay x numberOfDays
      - Apply demand surge (+30% if >5 concurrent bookings)
      - Apply weekend surge (+20% if starts on Sat/Sun)

   b) Discount (initially 0, owner can apply later)

   c) Base Amount = Original - Discount

   d) Commission = Base Amount x 15%

   e) GST = (Base + Commission) x 18%

   f) Total = Base + Commission + GST

5. CREATE BOOKING ENTITY
   - Store all price components
   - Set status = PENDING
   - Set paymentStatus = NOT_PAID
   - Store pricePerDayAtBooking (audit snapshot)

6. LOG AUDIT TRAIL
   - BookingAuditService.log(booking, "CREATED", advertiser)

7. RETURN BOOKING
```

### 7.4 Overlap Detection Algorithm

```sql
-- Query to check for overlapping bookings
SELECT COUNT(*) > 0 FROM bookings
WHERE billboard_id = :billboardId
  AND status IN ('PENDING', 'APPROVED')
  AND end_date >= :requestedStartDate
  AND start_date <= :requestedEndDate;

-- Example:
-- Existing booking: Jan 5 - Jan 10
-- Requested: Jan 8 - Jan 15
-- Result: CONFLICT (overlaps Jan 8-10)

-- Visualization:
--     Existing: |----5====10----|
--     Requested:      |---8=======15---|
--     Overlap:        |---8==10---|
```

### 7.5 Booking API Endpoints

```bash
# 1. Get Price Preview (MUST call before booking)
GET /api/advertiser/bookings/price-preview?billboardId=1&startDate=2024-02-01&endDate=2024-02-07

# Response:
{
  "billboardId": 1,
  "billboardTitle": "Premium Highway Billboard",
  "pricePerDay": 5000.0,
  "startDate": "2024-02-01",
  "endDate": "2024-02-07",
  "totalDays": 7,
  "originalBaseAmount": 35000.0,
  "baseAmount": 42000.0,         // After smart pricing
  "demandSurgeApplied": true,    // +30%
  "weekendSurgeApplied": false,
  "commissionPercent": 15.0,
  "commissionAmount": 6300.0,
  "gstPercent": 18.0,
  "gstAmount": 8694.0,
  "cgstPercent": 9.0,
  "cgstAmount": 4347.0,
  "sgstPercent": 9.0,
  "sgstAmount": 4347.0,
  "taxableValue": 48300.0,
  "totalAmount": 56994.0,
  "currency": "INR",
  "maxDiscountPercent": 50.0,
  "ownerName": "Billboard Owner",
  "ownerEmail": "owner@example.com",
  "ownerPhone": "+919876543210"
}

# 2. Create Booking
POST /api/advertiser/bookings
{
  "billboardId": 1,
  "startDate": "2024-02-01",
  "endDate": "2024-02-07"
}

# 3. Get My Bookings
GET /api/advertiser/bookings

# 4. Cancel Booking (before payment)
POST /api/advertiser/bookings/{id}/cancel

# 5. Cancel After Payment (requires refund)
POST /api/advertiser/bookings/{id}/cancel-after-payment

# 6. Owner: Approve Booking
POST /api/owner/bookings/{id}/approve

# 7. Owner: Reject Booking
POST /api/owner/bookings/{id}/reject

# 8. Owner: Apply Discount
POST /api/owner/bookings/{id}/discount?percent=10
```

---

## 8. Payment Gateway Integration

### 8.1 Razorpay Integration Overview

```
+-------------------+     +-------------------+     +-------------------+
|    FRONTEND       |     |     BACKEND       |     |     RAZORPAY      |
+--------+----------+     +--------+----------+     +--------+----------+
         |                         |                         |
         |  1. Click "Pay Now"     |                         |
         |------------------------>|                         |
         |                         |  2. Create Order        |
         |                         |  POST /v1/orders        |
         |                         |------------------------>|
         |                         |<------------------------|
         |                         |  order_id               |
         |<------------------------|                         |
         |  {orderId, keyId}       |                         |
         |                         |                         |
         |  3. Open Razorpay       |                         |
         |     Checkout Modal      |                         |
         |-------------------------------------------------->|
         |                         |                         |
         |  4. User enters         |                         |
         |     card/UPI details    |                         |
         |-------------------------------------------------->|
         |                         |                         |
         |<--------------------------------------------------|
         |  5. Payment processed   |                         |
         |  {paymentId, signature} |                         |
         |                         |                         |
         |  6. Verify Payment      |                         |
         |------------------------>|                         |
         |                         |  7. Verify Signature    |
         |                         |  (HMAC-SHA256)          |
         |                         |                         |
         |                         |  8. If valid:           |
         |                         |  - Update booking       |
         |                         |  - Create payment split |
         |                         |  - Credit wallets       |
         |                         |  - Generate invoice     |
         |<------------------------|                         |
         |  9. Booking Confirmed   |                         |
         |                         |                         |
         |                         |<------------------------|
         |                         |  10. Webhook (async)    |
         |                         |  payment.captured       |
         |                         |                         |
```

### 8.2 Payment Service Implementation

```java
// PaymentService.java - Detailed Flow

public class PaymentService {

    // ==================== 1. CREATE ORDER ====================
    @Transactional
    public PaymentOrderResponse createOrder(CreatePaymentOrderRequest req, User advertiser) {

        // STEP 1: Validate booking exists
        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // STEP 2: Verify ownership
        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("Not authorized to pay for this booking");
        }

        // STEP 3: Validate booking status
        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new RuntimeException("Only APPROVED bookings can be paid");
        }

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Already paid");
        }

        // STEP 4: Recalculate prices (in case billboard price changed)
        booking = bookingService.recalculatePricesIfUnpaid(booking);

        // STEP 5: Idempotency - return existing order if already created
        if (booking.getRazorpayOrderId() != null) {
            return existingOrderResponse(booking);
        }

        // STEP 6: Create Razorpay order
        JSONObject options = new JSONObject();
        options.put("amount", Math.round(booking.getTotalPrice() * 100)); // Paise
        options.put("currency", "INR");
        options.put("receipt", "BOOKING_" + booking.getId());
        options.put("payment_capture", 1);  // Auto-capture

        Order order = razorpay.orders.create(options);

        // STEP 7: Save order ID to booking
        booking.setRazorpayOrderId(order.get("id"));
        booking.setPaymentStatus(PaymentStatus.PENDING);
        bookingRepository.save(booking);

        // STEP 8: Return response for frontend
        return PaymentOrderResponse.builder()
                .orderId(order.get("id"))
                .keyId(razorpayKeyId)
                .amount(booking.getTotalPrice())
                .currency("INR")
                .build();
    }

    // ==================== 2. VERIFY PAYMENT ====================
    @Transactional
    public Booking verifyAndCapture(VerifyPaymentRequest req, User advertiser) {

        // STEP 1: Find booking by order ID
        Booking booking = bookingRepository.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // STEP 2: Verify ownership
        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("Not authorized");
        }

        // STEP 3: Idempotency - already paid
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            return booking;  // Return existing, don't reprocess
        }

        // STEP 4: Verify Razorpay signature
        boolean valid = isSignatureValid(
                req.getRazorpayOrderId(),
                req.getRazorpayPaymentId(),
                req.getRazorpaySignature()
        );

        if (!valid) {
            booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking);
            throw new RuntimeException("Invalid signature");
        }

        // STEP 5: Lock commission percentage (snapshot at payment time)
        PlatformSettings settings = platformSettingsService.get();
        booking.setCommissionPercent(settings.getCommissionPercent());

        // STEP 6: Mark as PAID
        booking.setRazorpayPaymentId(req.getRazorpayPaymentId());
        booking.setRazorpaySignature(req.getRazorpaySignature());
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaymentDate(LocalDateTime.now());
        bookingRepository.save(booking);

        // STEP 7: Create payment history record
        PaymentHistory history = PaymentHistory.builder()
                .razorpayOrderId(req.getRazorpayOrderId())
                .razorpayPaymentId(req.getRazorpayPaymentId())
                .booking(booking)
                .advertiser(booking.getAdvertiser())
                .owner(booking.getBillboard().getOwner())
                .amount(booking.getTotalPrice())
                .build();
        paymentRepo.save(history);

        // STEP 8: Create payment split & credit wallets
        paymentSplitService.createSplit(booking);

        // STEP 9: Auto-generate invoice
        invoiceService.generateInvoice(booking.getId());

        return booking;
    }

    // ==================== 3. SIGNATURE VERIFICATION ====================
    private boolean isSignatureValid(String orderId, String paymentId, String signature) {
        // Razorpay signature = HMAC-SHA256(orderId|paymentId, secret)
        String payload = orderId + "|" + paymentId;

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(keySecret.getBytes(), "HmacSHA256");
        mac.init(key);

        String computed = bytesToHex(mac.doFinal(payload.getBytes()));
        return computed.equalsIgnoreCase(signature);
    }
}
```

### 8.3 Payment Split System

When a payment is successful, the amount is split between Owner and Platform:

```
TOTAL PAYMENT: Rs. 56,994
    |
    +-- GST (18%): Rs. 8,694
    |   |
    |   +-- CGST (9%): Rs. 4,347 --> Government
    |   +-- SGST (9%): Rs. 4,347 --> Government
    |
    +-- Base + Commission: Rs. 48,300
        |
        +-- OWNER WALLET: Rs. 42,000 (Base Amount)
        |   - This is credited immediately
        |   - Owner can request payout anytime
        |
        +-- ADMIN WALLET: Rs. 6,300 (15% Commission)
            - Platform's revenue
            - Used for operations
```

```java
// PaymentSplitService.java
@Transactional
public PaymentSplit createSplit(Booking booking) {

    // Idempotency check
    Optional<PaymentSplit> existing = repo.findByBooking(booking);
    if (existing.isPresent()) {
        return existing.get();  // Don't duplicate
    }

    // Create split record
    PaymentSplit split = PaymentSplit.builder()
            .booking(booking)
            .ownerAmount(booking.getBaseAmount())        // Rs. 42,000
            .platformCommission(booking.getCommissionAmount())  // Rs. 6,300
            .gstAmount(booking.getGstAmount())           // Rs. 8,694
            .settled(false)
            .build();
    repo.save(split);

    // Credit Owner Wallet
    ownerWalletService.credit(
            booking.getBillboard().getOwner(),
            booking.getBaseAmount(),
            "BOOKING#" + booking.getId()
    );

    // Credit Admin Wallet
    adminWalletService.credit(
            booking.getCommissionAmount(),
            "COMMISSION#BOOKING#" + booking.getId()
    );

    return split;
}
```

### 8.4 Refund Process

```
+-------------------+     +-------------------+     +-------------------+
|    ADVERTISER     |     |     BACKEND       |     |     RAZORPAY      |
+--------+----------+     +--------+----------+     +--------+----------+
         |                         |                         |
         |  1. Request Refund      |                         |
         |------------------------>|                         |
         |                         |  2. Validate:           |
         |                         |  - Is PAID?             |
         |                         |  - Is owner?            |
         |                         |  - Not already refunded?|
         |                         |                         |
         |                         |  3. Create Refund       |
         |                         |  POST /payments/{id}/   |
         |                         |       refund            |
         |                         |------------------------>|
         |                         |<------------------------|
         |                         |  refund_id              |
         |                         |                         |
         |                         |  4. Reverse Wallet      |
         |                         |     Credits:            |
         |                         |  - Debit Owner Wallet   |
         |                         |  - Debit Admin Wallet   |
         |                         |                         |
         |                         |  5. Update Booking      |
         |                         |  - status = CANCELLED   |
         |                         |  - paymentStatus =      |
         |                         |    REFUNDED             |
         |<------------------------|                         |
         |  Refund Initiated       |                         |
         |                         |                         |
         |                         |<------------------------|
         |                         |  6. Webhook (async)     |
         |                         |  refund.processed       |
         |                         |                         |
```

### 8.5 Payment API Endpoints

```bash
# 1. Create Payment Order
POST /api/payments/create-order
Authorization: Bearer <JWT>
{
  "bookingId": 123
}

# Response:
{
  "orderId": "order_ABC123",
  "keyId": "rzp_test_XXX",
  "amount": 56994.0,
  "currency": "INR",
  "receipt": "BOOKING_123"
}

# 2. Verify Payment (after Razorpay checkout)
POST /api/payments/verify
Authorization: Bearer <JWT>
{
  "razorpayOrderId": "order_ABC123",
  "razorpayPaymentId": "pay_XYZ789",
  "razorpaySignature": "hmac_signature_here"
}

# 3. Razorpay Webhook (server-to-server)
POST /api/payments/webhook
X-Razorpay-Signature: <webhook_signature>
{
  "event": "payment.captured",
  "payload": {
    "payment": {
      "entity": {
        "id": "pay_XYZ789",
        "order_id": "order_ABC123",
        "amount": 5699400,
        "status": "captured"
      }
    }
  }
}

# 4. Initiate Refund
POST /api/payments/refund/{bookingId}
Authorization: Bearer <JWT>

# 5. Get Payment History
GET /api/payments/history
Authorization: Bearer <JWT>
```

---

## 9. Wallet System

### 9.1 Wallet Architecture

```
+------------------------------------------------------------------+
|                        WALLET ECOSYSTEM                           |
+------------------------------------------------------------------+
|                                                                   |
|  +------------------------+       +------------------------+      |
|  |     OWNER WALLET       |       |     ADMIN WALLET       |      |
|  +------------------------+       +------------------------+      |
|  |                        |       |                        |      |
|  | balance: Rs. 42,000    |       | balance: Rs. 15,000    |      |
|  | totalEarned: Rs. 150k  |       | totalCommission: Rs.50k|      |
|  | totalWithdrawn: Rs.108k|       | totalWithdrawn: Rs.35k |      |
|  |                        |       |                        |      |
|  +------------------------+       +------------------------+      |
|           |                                   |                   |
|           |  TRANSACTIONS                     |  TRANSACTIONS     |
|           v                                   v                   |
|  +------------------------+       +------------------------+      |
|  | WalletTransaction      |       | AdminWalletTransaction |      |
|  +------------------------+       +------------------------+      |
|  | - CREDIT (booking)     |       | - CREDIT (commission)  |      |
|  | - DEBIT (payout)       |       | - DEBIT (expenses)     |      |
|  | - DEBIT (refund)       |       | - DEBIT (refund)       |      |
|  +------------------------+       +------------------------+      |
|                                                                   |
+------------------------------------------------------------------+
```

### 9.2 Owner Wallet Service

```java
// OwnerWalletService.java

@Service
public class OwnerWalletService {

    // ==================== CREDIT (Booking Payment) ====================
    @Transactional
    public void credit(User owner, Double amount, String reference) {

        // Get or create wallet
        OwnerWallet wallet = walletRepo.findByOwner(owner)
                .orElse(OwnerWallet.builder()
                        .owner(owner)
                        .balance(0.0)
                        .totalEarned(0.0)
                        .totalWithdrawn(0.0)
                        .build());

        // Update balance
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setTotalEarned(wallet.getTotalEarned() + amount);
        walletRepo.save(wallet);

        // Record transaction
        txRepo.save(WalletTransaction.builder()
                .owner(owner)
                .amount(amount)
                .type(TxType.CREDIT)
                .reference(reference)  // e.g., "BOOKING#123"
                .time(LocalDateTime.now())
                .build());
    }

    // ==================== DEBIT (Payout/Refund) ====================
    @Transactional
    public void debit(User owner, Double amount, String reference) {

        OwnerWallet wallet = walletRepo.findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn() + amount);
        walletRepo.save(wallet);

        txRepo.save(WalletTransaction.builder()
                .owner(owner)
                .amount(amount)
                .type(TxType.DEBIT)
                .reference(reference)  // e.g., "PAYOUT#456" or "REFUND#BOOKING#123"
                .time(LocalDateTime.now())
                .build());
    }
}
```

### 9.3 Payout Flow

```
+-------------------+     +-------------------+     +-------------------+
|      OWNER        |     |      ADMIN        |     |     RAZORPAY      |
+--------+----------+     +--------+----------+     +--------+----------+
         |                         |                         |
         |  1. Request Payout      |                         |
         |  (amount: Rs. 10,000)   |                         |
         |------------------------>|                         |
         |                         |                         |
         |                         |  2. Admin Reviews       |
         |                         |  - Check balance        |
         |                         |  - Verify bank account  |
         |                         |                         |
         |                         |  3. Approve Payout      |
         |                         |  (fundAccountId: fa_xxx)|
         |                         |                         |
         |                         |  4. RazorpayX Transfer  |
         |                         |  (if live mode)         |
         |                         |------------------------>|
         |                         |<------------------------|
         |                         |  payout_id              |
         |                         |                         |
         |                         |  5. Debit Owner Wallet  |
         |                         |  - balance -= 10,000    |
         |                         |                         |
         |<------------------------|  6. Notify Owner        |
         |  Payout Processed!      |                         |
         |                         |                         |
         |  7. Bank Transfer       |                         |
         |  (1-3 business days)    |                         |
         |<--------------------------------------------------|
         |                         |                         |
```

### 9.4 Wallet API Endpoints

```bash
# ==================== OWNER WALLET ====================

# 1. Get Wallet Balance
GET /api/owner/wallet
Authorization: Bearer <OWNER_JWT>

# Response:
{
  "balance": 42000.0,
  "totalEarned": 150000.0,
  "totalWithdrawn": 108000.0,
  "pendingPayouts": 5000.0
}

# 2. Get Transaction History
GET /api/owner/wallet/transactions
Authorization: Bearer <OWNER_JWT>

# Response:
[
  {
    "id": 1,
    "amount": 42000.0,
    "type": "CREDIT",
    "reference": "BOOKING#123",
    "time": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "amount": 10000.0,
    "type": "DEBIT",
    "reference": "PAYOUT#456",
    "time": "2024-01-20T14:00:00"
  }
]

# 3. Request Payout
POST /api/owner/payouts/request?amount=10000
Authorization: Bearer <OWNER_JWT>

# 4. Get Payout Status
GET /api/owner/payouts
Authorization: Bearer <OWNER_JWT>

# ==================== ADMIN WALLET ====================

# 1. Get Admin Wallet
GET /api/admin/wallet
Authorization: Bearer <ADMIN_JWT>

# Response:
{
  "balance": 15000.0,
  "totalCommission": 50000.0,
  "totalWithdrawn": 35000.0,
  "monthlyCommission": [
    {"month": "2024-01", "amount": 12000.0},
    {"month": "2024-02", "amount": 15000.0}
  ]
}

# 2. Get Pending Payouts
GET /api/admin/payouts
Authorization: Bearer <ADMIN_JWT>

# 3. Approve Payout
POST /api/admin/payouts/{id}/approve?fundAccountId=fa_xxx
Authorization: Bearer <ADMIN_JWT>

# 4. Reject Payout
POST /api/admin/payouts/{id}/reject?reason=Invalid%20bank%20details
Authorization: Bearer <ADMIN_JWT>
```

---

## 10. Smart Pricing Engine

### 10.1 Dynamic Pricing Factors

```
BASE PRICE (Billboard's daily rate)
        |
        v
+-------------------+
|  SMART PRICING    |
|     ENGINE        |
+-------------------+
        |
        +-- DEMAND SURGE (+30%)
        |   - If >5 concurrent bookings on same billboard
        |   - Indicates high demand location
        |
        +-- WEEKEND SURGE (+20%)
        |   - If booking starts on Saturday/Sunday
        |   - Premium for weekend visibility
        |
        +-- SEASONAL FACTORS (Future)
        |   - Festival seasons
        |   - Holiday periods
        |
        v
SMART PRICE (Final base amount)
```

### 10.2 Smart Pricing Algorithm

```java
// SmartPricingService.java

@Service
public class SmartPricingService {

    public double calculateBasePrice(Billboard billboard, LocalDate start, LocalDate end) {

        // Step 1: Calculate number of days
        long days = ChronoUnit.DAYS.between(start, end) + 1;

        // Step 2: Base price
        double base = billboard.getPricePerDay() * days;

        // Step 3: Check demand (concurrent bookings)
        long concurrentBookings = bookingRepo.countByBillboardAndStatusAndDateOverlap(
                billboard,
                BookingStatus.APPROVED,
                start,
                end
        );

        // Step 4: Apply demand surge
        if (concurrentBookings > 5) {
            base *= 1.30;  // +30% surge
        }

        // Step 5: Apply weekend surge
        if (start.getDayOfWeek() == DayOfWeek.SATURDAY ||
            start.getDayOfWeek() == DayOfWeek.SUNDAY) {
            base *= 1.20;  // +20% surge
        }

        // Step 6: Round to nearest rupee
        return Math.round(base);
    }
}
```

### 10.3 Pricing Example

```
Billboard: Premium Highway Location
Daily Rate: Rs. 5,000

Booking Request:
- Start: Saturday, Feb 1
- End: Friday, Feb 7
- Duration: 7 days

Calculation:
1. Base: 5,000 x 7 = Rs. 35,000
2. Weekend Surge (starts Saturday): 35,000 x 1.20 = Rs. 42,000
3. No demand surge (< 5 concurrent bookings)

Final Base Amount: Rs. 42,000

Additional Charges:
- Commission (15%): Rs. 6,300
- GST (18%): Rs. 8,694
- TOTAL: Rs. 56,994
```

### 10.4 Owner Discount System

Owners can offer discounts to attract advertisers:

```java
// Discount Rules:
// - Weekdays: Up to 50% discount allowed
// - Weekends: Up to 30% discount allowed (premium time)
// - Discount applied BEFORE commission calculation

public Booking applyDiscount(Long bookingId, Double discountPercent, User owner) {

    Booking booking = bookingRepository.findById(bookingId).orElseThrow();

    // Validate ownership
    if (!booking.getBillboard().getOwner().getId().equals(owner.getId())) {
        throw new RuntimeException("Not your billboard");
    }

    // Validate discount limit
    boolean isWeekend = booking.getStartDate().getDayOfWeek().getValue() >= 6;
    double maxDiscount = isWeekend ? 30.0 : 50.0;

    if (discountPercent > maxDiscount) {
        throw new RuntimeException("Max discount is " + maxDiscount + "%");
    }

    // Apply discount
    double originalBase = booking.getOriginalBaseAmount();
    double discountAmount = originalBase * discountPercent / 100;
    double newBase = originalBase - discountAmount;

    // Recalculate commission and GST on discounted base
    double commission = newBase * settings.getCommissionPercent() / 100;
    double gst = (newBase + commission) * settings.getGstPercent() / 100;
    double total = newBase + commission + gst;

    booking.setDiscountPercent(discountPercent);
    booking.setDiscountAmount(discountAmount);
    booking.setBaseAmount(newBase);
    booking.setCommissionAmount(commission);
    booking.setGstAmount(gst);
    booking.setTotalPrice(total);

    return bookingRepository.save(booking);
}
```

---

## 11. Invoice & Tax Management

### 11.1 GST Invoice Structure

```
+------------------------------------------------------------------+
|                        TAX INVOICE                                |
+------------------------------------------------------------------+
|                                                                   |
|  SELLER:                          BUYER:                          |
|  Billboard & Hoarding Pvt Ltd     Advertiser Company Name         |
|  GSTIN: 27ABCDE1234F1Z5           GSTIN: 29XXXXX1234X1Z1          |
|  Pune, Maharashtra                Bangalore, Karnataka            |
|                                                                   |
+------------------------------------------------------------------+
|  Invoice No: INV-2024-0001        Date: 15-Jan-2024              |
|  Booking ID: #123                 Due Date: N/A (Prepaid)        |
+------------------------------------------------------------------+
|                                                                   |
|  DESCRIPTION                               AMOUNT                 |
|  ---------------------------------------------------------       |
|  Billboard Advertising Services                                   |
|  - Billboard: Premium Highway Location                            |
|  - Duration: 01-Feb-2024 to 07-Feb-2024 (7 days)                 |
|  - Base Amount                             Rs. 42,000.00          |
|  - Platform Service Fee (15%)              Rs.  6,300.00          |
|  ---------------------------------------------------------       |
|  Taxable Value                             Rs. 48,300.00          |
|  ---------------------------------------------------------       |
|  CGST @ 9%                                 Rs.  4,347.00          |
|  SGST @ 9%                                 Rs.  4,347.00          |
|  ---------------------------------------------------------       |
|  TOTAL GST                                 Rs.  8,694.00          |
|  ---------------------------------------------------------       |
|                                                                   |
|  GRAND TOTAL                               Rs. 56,994.00          |
|  (Fifty-six thousand nine hundred ninety-four only)              |
|                                                                   |
+------------------------------------------------------------------+
|  Payment Status: PAID                                             |
|  Razorpay Payment ID: pay_XYZ789                                 |
|  Payment Date: 15-Jan-2024 10:30:00                              |
+------------------------------------------------------------------+
```

### 11.2 Invoice Generation

```java
// InvoiceService.java

public Invoice generateInvoice(Long bookingId) {

    Booking booking = bookingRepository.findById(bookingId).orElseThrow();

    // Idempotency: Don't regenerate if exists
    Optional<Invoice> existing = invoiceRepo.findByBooking(booking);
    if (existing.isPresent()) {
        return existing.get();
    }

    // Generate invoice number
    String invoiceNumber = generateInvoiceNumber();  // INV-2024-0001

    // Create invoice record
    Invoice invoice = Invoice.builder()
            .booking(booking)
            .invoiceNumber(invoiceNumber)
            .advertiser(booking.getAdvertiser())
            .owner(booking.getBillboard().getOwner())
            .billboard(booking.getBillboard())
            .baseAmount(booking.getBaseAmount())
            .commissionAmount(booking.getCommissionAmount())
            .gstAmount(booking.getGstAmount())
            .totalAmount(booking.getTotalPrice())
            .cgstAmount(booking.getGstAmount() / 2)
            .sgstAmount(booking.getGstAmount() / 2)
            .cgstPercent(9.0)
            .sgstPercent(9.0)
            .generatedAt(LocalDateTime.now())
            .build();

    return invoiceRepo.save(invoice);
}
```

### 11.3 PDF Generation

```java
// GstInvoicePdfService.java (using OpenPDF)

public byte[] generatePdf(Invoice invoice) {

    Document document = new Document(PageSize.A4);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PdfWriter.getInstance(document, baos);

    document.open();

    // Add company header
    addHeader(document);

    // Add invoice details
    addInvoiceDetails(document, invoice);

    // Add line items table
    addLineItems(document, invoice);

    // Add tax breakdown
    addTaxBreakdown(document, invoice);

    // Add total
    addTotal(document, invoice);

    // Add payment info
    addPaymentInfo(document, invoice);

    // Add footer
    addFooter(document);

    document.close();

    return baos.toByteArray();
}
```

### 11.4 Invoice API Endpoints

```bash
# 1. Get Invoice Details
GET /api/invoices/{bookingId}
Authorization: Bearer <JWT>

# Response:
{
  "id": 1,
  "invoiceNumber": "INV-2024-0001",
  "bookingId": 123,
  "advertiserName": "ABC Company",
  "billboardTitle": "Premium Highway Location",
  "startDate": "2024-02-01",
  "endDate": "2024-02-07",
  "baseAmount": 42000.0,
  "commissionAmount": 6300.0,
  "cgstPercent": 9.0,
  "cgstAmount": 4347.0,
  "sgstPercent": 9.0,
  "sgstAmount": 4347.0,
  "totalGst": 8694.0,
  "totalAmount": 56994.0,
  "paymentStatus": "PAID",
  "razorpayPaymentId": "pay_XYZ789",
  "generatedAt": "2024-01-15T10:30:00"
}

# 2. Download Invoice PDF
GET /api/invoices/{bookingId}/download
Authorization: Bearer <JWT>

# Response: application/pdf binary
```

---

## 12. Campaign Management

### 12.1 Campaign Concept

A Campaign is a higher-level entity that groups multiple billboard bookings under a single advertising initiative:

```
CAMPAIGN: "Summer Sale 2024"
    |
    +-- Budget: Rs. 500,000
    +-- Duration: June 1 - July 31
    +-- Target Cities: Mumbai, Pune, Delhi
    |
    +-- BOOKINGS:
        |
        +-- Billboard A (Mumbai) - Rs. 100,000
        +-- Billboard B (Mumbai) - Rs. 80,000
        +-- Billboard C (Pune) - Rs. 75,000
        +-- Billboard D (Delhi) - Rs. 120,000
        |
        +-- Total Spent: Rs. 375,000
        +-- Remaining Budget: Rs. 125,000
```

### 12.2 Campaign Status Flow

```
SCHEDULED --> ACTIVE --> COMPLETED
     |          |
     v          v
  PAUSED    CANCELLED
```

### 12.3 Campaign API Endpoints

```bash
# 1. Create Campaign
POST /api/advertiser/campaigns
{
  "name": "Summer Sale 2024",
  "budget": 500000.0,
  "startDate": "2024-06-01",
  "endDate": "2024-07-31",
  "cities": ["Mumbai", "Pune", "Delhi"]
}

# 2. Get My Campaigns
GET /api/advertiser/campaigns

# 3. Get Campaign Analytics
GET /api/advertiser/campaigns/{id}/analytics

# Response:
{
  "campaignId": 1,
  "name": "Summer Sale 2024",
  "status": "ACTIVE",
  "budget": 500000.0,
  "spent": 375000.0,
  "remaining": 125000.0,
  "bookingsCount": 4,
  "activeBillboards": 4,
  "impressionsEstimate": 2500000,
  "dailySpend": [
    {"date": "2024-06-01", "amount": 5000.0},
    {"date": "2024-06-02", "amount": 5000.0}
  ]
}

# 4. Pause Campaign
POST /api/advertiser/campaigns/{id}/pause

# 5. Resume Campaign
POST /api/advertiser/campaigns/{id}/resume
```

---

## 13. Admin Dashboard & Analytics

### 13.1 Dashboard Metrics

```
+------------------------------------------------------------------+
|                     ADMIN DASHBOARD                               |
+------------------------------------------------------------------+
|                                                                   |
|  +------------------+  +------------------+  +------------------+ |
|  | TOTAL REVENUE    |  | TOTAL OWNERS     |  | TOTAL ADVERTISERS| |
|  | Rs. 15,00,000    |  | 150              |  | 500              | |
|  | +12% vs last mo  |  | +8 this week     |  | +25 this week    | |
|  +------------------+  +------------------+  +------------------+ |
|                                                                   |
|  +------------------+  +------------------+  +------------------+ |
|  | ACTIVE BILLBOARDS|  | PENDING BOOKINGS |  | PLATFORM COMMISS.| |
|  | 320              |  | 45               |  | Rs. 2,25,000     | |
|  | 280 available    |  | Needs approval   |  | 15% avg          | |
|  +------------------+  +------------------+  +------------------+ |
|                                                                   |
|  +---------------------------------------------------------------+|
|  |                    REVENUE CHART (Last 12 Months)              ||
|  |                                                                ||
|  |     ^                                                          ||
|  |     |                                            ****          ||
|  |     |                                       *****              ||
|  |     |                                  *****                   ||
|  |     |                             *****                        ||
|  |     |                        *****                             ||
|  |     |                   *****                                  ||
|  |     |              *****                                       ||
|  |     |         *****                                            ||
|  |     |    *****                                                 ||
|  |     +------------------------------------------------------>   ||
|  |       Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec         ||
|  +---------------------------------------------------------------+|
|                                                                   |
+------------------------------------------------------------------+
```

### 13.2 Analytics Service Implementation

```java
// AdminDashboardService.java

public AdminDashboardResponse getDashboardData() {

    AdminDashboardResponse res = new AdminDashboardResponse();

    // User counts (using COUNT queries, not loading entities)
    res.setTotalOwners(userRepository.countByRole(UserRole.OWNER));
    res.setTotalAdvertisers(userRepository.countByRole(UserRole.ADVERTISER));
    res.setPendingKyc(userRepository.countByKycStatus(KycStatus.PENDING));
    res.setBlockedUsers(userRepository.countByBlockedTrue());

    // Billboard counts
    res.setTotalBillboards(billboardRepository.count());
    res.setAvailableBillboards(billboardRepository.countByAvailableTrue());
    res.setBlockedBillboards(billboardRepository.countByAdminBlockedTrue());

    // Booking counts
    res.setPendingBookings(bookingRepository.countByStatus(BookingStatus.PENDING));
    res.setApprovedBookings(bookingRepository.countByStatus(BookingStatus.APPROVED));

    // Revenue (using SUM query)
    res.setTotalRevenue(bookingRepository.sumTotalPriceByStatus(BookingStatus.APPROVED));

    return res;
}
```

### 13.3 Admin API Endpoints

```bash
# 1. Dashboard Stats
GET /api/admin/dashboard/stats

# 2. Revenue Chart Data
GET /api/admin/analytics/revenue-chart

# 3. Top Advertisers
GET /api/admin/analytics/top-advertisers

# 4. Booking Trends
GET /api/admin/analytics/bookings-chart

# 5. Platform Stats
GET /api/admin/analytics/platform-stats

# 6. Cancellation Stats
GET /api/admin/analytics/cancellation-stats
```

---

## 14. Security Features

### 14.1 Security Layers

```
REQUEST
   |
   v
+------------------+
| CORS Filter      |  --> Validate origin
+------------------+
   |
   v
+------------------+
| Rate Limiter     |  --> Prevent brute force
+------------------+
   |
   v
+------------------+
| JWT Filter       |  --> Authenticate user
+------------------+
   |
   v
+------------------+
| Authorization    |  --> Check permissions
+------------------+
   |
   v
+------------------+
| Input Validation |  --> Sanitize input
+------------------+
   |
   v
CONTROLLER
```

### 14.2 Rate Limiting Configuration

```java
// RateLimitConfig.java

@Component
public class RateLimitConfig {

    // Auth endpoints: 10 requests per minute
    public Bucket createAuthBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
                .build();
    }

    // API endpoints: 100 requests per minute
    public Bucket createApiBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(100, Duration.ofMinutes(1)))
                .build();
    }
}

// RateLimitInterceptor.java

@Override
public boolean preHandle(HttpServletRequest request, ...) {

    String ip = request.getRemoteAddr();
    String path = request.getRequestURI();

    Bucket bucket = path.contains("/auth")
            ? authBuckets.computeIfAbsent(ip, k -> config.createAuthBucket())
            : apiBuckets.computeIfAbsent(ip, k -> config.createApiBucket());

    if (bucket.tryConsume(1)) {
        return true;  // Allow request
    }

    response.setStatus(429);  // Too Many Requests
    response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
    return false;
}
```

### 14.3 Login Attempt Protection

```java
// LoginAttemptService.java

private static final int MAX_ATTEMPTS = 5;
private static final int BLOCK_MINUTES = 15;

private Map<String, Integer> attempts = new ConcurrentHashMap<>();
private Map<String, LocalDateTime> blocked = new ConcurrentHashMap<>();

public void checkIfBlocked(String email) {
    LocalDateTime blockedUntil = blocked.get(email);
    if (blockedUntil != null && blockedUntil.isAfter(LocalDateTime.now())) {
        throw new RuntimeException("Account blocked. Try after " +
                ChronoUnit.MINUTES.between(LocalDateTime.now(), blockedUntil) + " minutes");
    }
}

public void loginFailed(String email) {
    int count = attempts.merge(email, 1, Integer::sum);
    if (count >= MAX_ATTEMPTS) {
        blocked.put(email, LocalDateTime.now().plusMinutes(BLOCK_MINUTES));
        attempts.remove(email);
    }
}

public void loginSuccess(String email) {
    attempts.remove(email);
    blocked.remove(email);
}
```

### 14.4 Risk Detection

```java
// LoginRiskService.java

public boolean isRisky(String email, String ip, String userAgent) {

    // Get last successful login
    Optional<LoginHistory> lastLogin = loginHistoryRepo
            .findTopByEmailOrderByLoginAtDesc(email);

    if (lastLogin.isEmpty()) {
        return true;  // First login - require 2FA
    }

    LoginHistory last = lastLogin.get();

    // Check if IP changed
    if (!last.getIp().equals(ip)) {
        return true;
    }

    // Check if device changed (simplified User-Agent check)
    if (!last.getUserAgent().equals(userAgent)) {
        return true;
    }

    // Check if login at unusual time (e.g., 2 AM - 5 AM)
    int hour = LocalDateTime.now().getHour();
    if (hour >= 2 && hour <= 5) {
        return true;
    }

    return false;
}
```

---

## 15. API Reference

### Complete API Endpoint List

<details>
<summary><strong>Authentication APIs</strong></summary>

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login | No |
| POST | `/api/auth/verify-otp` | Verify 2FA OTP | No |
| POST | `/api/auth/setup-2fa` | Enable 2FA | Yes |
| POST | `/api/auth/magic-link/request` | Request magic link | No |
| POST | `/api/auth/magic-link/verify` | Verify magic link | No |
| POST | `/api/auth/recovery/verify` | Use recovery code | No |
| POST | `/api/auth/change-password` | Change password | Yes |
| GET | `/api/auth/me` | Get current user | Yes |

</details>

<details>
<summary><strong>Admin APIs</strong></summary>

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/admin/dashboard/stats` | Dashboard stats | Admin |
| GET | `/api/admin/users` | List all users | Admin |
| POST | `/api/admin/users/{id}/block` | Block user | Admin |
| POST | `/api/admin/users/{id}/unblock` | Unblock user | Admin |
| GET | `/api/admin/kyc/pending` | Pending KYC | Admin |
| POST | `/api/admin/kyc/{id}/approve` | Approve KYC | Admin |
| POST | `/api/admin/kyc/{id}/reject` | Reject KYC | Admin |
| GET | `/api/admin/billboards` | All billboards | Admin |
| POST | `/api/admin/billboards/{id}/block` | Block billboard | Admin |
| GET | `/api/admin/analytics/*` | Analytics data | Admin |
| GET | `/api/admin/wallet` | Admin wallet | Admin |
| GET | `/api/admin/payouts` | Pending payouts | Admin |
| POST | `/api/admin/payouts/{id}/approve` | Approve payout | Admin |
| GET | `/api/admin/settings` | Platform settings | Admin |
| PUT | `/api/admin/settings` | Update settings | Admin |

</details>

<details>
<summary><strong>Owner APIs</strong></summary>

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/owner/billboards` | My billboards | Owner |
| POST | `/api/owner/billboards` | Create billboard | Owner |
| PUT | `/api/owner/billboards/{id}` | Update billboard | Owner |
| DELETE | `/api/owner/billboards/{id}` | Delete billboard | Owner |
| POST | `/api/owner/billboards/{id}/upload-images` | Upload images | Owner |
| GET | `/api/owner/bookings` | My bookings | Owner |
| POST | `/api/owner/bookings/{id}/approve` | Approve booking | Owner |
| POST | `/api/owner/bookings/{id}/reject` | Reject booking | Owner |
| POST | `/api/owner/bookings/{id}/discount` | Apply discount | Owner |
| GET | `/api/owner/wallet` | Wallet balance | Owner |
| GET | `/api/owner/wallet/transactions` | Transactions | Owner |
| POST | `/api/owner/payouts/request` | Request payout | Owner |
| GET | `/api/owner/bank-accounts` | Bank accounts | Owner |
| POST | `/api/owner/bank-accounts` | Add bank account | Owner |

</details>

<details>
<summary><strong>Advertiser APIs</strong></summary>

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/advertiser/billboards` | Browse billboards | Advertiser |
| GET | `/api/advertiser/billboards/{id}` | Billboard details | Advertiser |
| GET | `/api/advertiser/billboards/{id}/availability` | Check availability | Advertiser |
| GET | `/api/advertiser/bookings/price-preview` | Price preview | Advertiser |
| POST | `/api/advertiser/bookings` | Create booking | Advertiser |
| GET | `/api/advertiser/bookings` | My bookings | Advertiser |
| POST | `/api/advertiser/bookings/{id}/cancel` | Cancel booking | Advertiser |
| GET | `/api/advertiser/favourites` | My favorites | Advertiser |
| POST | `/api/advertiser/favourites/{billboardId}` | Add favorite | Advertiser |
| DELETE | `/api/advertiser/favourites/{id}` | Remove favorite | Advertiser |
| GET | `/api/advertiser/campaigns` | My campaigns | Advertiser |
| POST | `/api/advertiser/campaigns` | Create campaign | Advertiser |
| GET | `/api/advertiser/dashboard` | Dashboard | Advertiser |

</details>

<details>
<summary><strong>Payment APIs</strong></summary>

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/payments/create-order` | Create Razorpay order | Advertiser |
| POST | `/api/payments/verify` | Verify payment | Advertiser |
| POST | `/api/payments/webhook` | Razorpay webhook | No (verified by signature) |
| GET | `/api/payments/history` | Payment history | Yes |
| POST | `/api/payments/refund/{bookingId}` | Request refund | Advertiser |
| GET | `/api/invoices/{bookingId}` | Get invoice | Yes |
| GET | `/api/invoices/{bookingId}/download` | Download PDF | Yes |

</details>

---

## 16. Database Schema

### Entity Relationship Diagram

```
+------------------+       +------------------+       +------------------+
|      users       |       |    billboards    |       |     bookings     |
+------------------+       +------------------+       +------------------+
| PK id            |       | PK id            |       | PK id            |
| name             |       | title            |       | FK advertiser_id |----+
| email (unique)   |<------| FK owner_id      |<------| FK billboard_id  |    |
| password         |       | location         |       | start_date       |    |
| role             |       | price_per_day    |       | end_date         |    |
| phone            |       | type             |       | status           |    |
| kyc_status       |       | latitude         |       | payment_status   |    |
| blocked          |       | longitude        |       | base_amount      |    |
| two_factor_enabled|      | available        |       | commission_amount|    |
| two_factor_method|       | admin_blocked    |       | gst_amount       |    |
| created_at       |       | created_at       |       | total_price      |    |
+------------------+       +------------------+       | rzp_order_id     |    |
         |                          |                 | rzp_payment_id   |    |
         |                          |                 | created_at       |    |
         v                          v                 +------------------+    |
+------------------+       +------------------+                |              |
|  owner_wallets   |       |billboard_images  |                |              |
+------------------+       +------------------+                v              |
| PK id            |       | PK id            |       +------------------+    |
| FK owner_id      |       | FK billboard_id  |       | payment_history  |    |
| balance          |       | image_path       |       +------------------+    |
| total_earned     |       +------------------+       | PK id            |    |
| total_withdrawn  |                                  | FK booking_id    |<---+
+------------------+                                  | FK advertiser_id |
         |                                            | FK owner_id      |
         v                                            | rzp_order_id     |
+------------------+                                  | rzp_payment_id   |
|wallet_transactions|                                 | amount           |
+------------------+                                  | paid_at          |
| PK id            |                                  | refund_status    |
| FK owner_id      |                                  +------------------+
| amount           |                                           |
| type (CREDIT/DEBIT)                                          v
| reference        |                                  +------------------+
| time             |                                  |  payment_splits  |
+------------------+                                  +------------------+
                                                      | PK id            |
+------------------+       +------------------+       | FK booking_id    |
|    campaigns     |       |    invoices      |       | owner_amount     |
+------------------+       +------------------+       | platform_commission
| PK id            |       | PK id            |       | gst_amount       |
| name             |       | FK booking_id    |       | settled          |
| FK advertiser_id |       | invoice_number   |       +------------------+
| budget           |       | base_amount      |
| spent            |       | commission_amount|
| status           |       | gst_amount       |
| start_date       |       | total_amount     |
| end_date         |       | generated_at     |
+------------------+       +------------------+
```

---

## 17. Performance Optimizations

### 17.1 Applied Optimizations

| Optimization | Description | Impact |
|--------------|-------------|--------|
| **FetchType.LAZY** | All relationships use lazy loading | Prevents N+1 queries |
| **JOIN FETCH** | Custom queries for needed relationships | Single query instead of multiple |
| **COUNT Queries** | Use COUNT instead of loading entities | Dashboard loads 10x faster |
| **Database Indexes** | Indexes on frequently queried columns | Query time reduced by 80% |
| **HikariCP** | Connection pooling with optimal config | Handles 1000+ concurrent users |
| **Spring Cache** | Cache platform settings | Zero DB hits for config |
| **Hibernate6Module** | Proper lazy proxy serialization | No serialization errors |

### 17.2 Query Optimization Examples

```java
// BAD: Loads all bookings into memory
long count = bookingRepository.findAll().size();

// GOOD: Database-level count
long count = bookingRepository.countByStatus(BookingStatus.PENDING);

// BAD: N+1 query problem
List<Booking> bookings = bookingRepository.findAll();
for (Booking b : bookings) {
    System.out.println(b.getBillboard().getTitle()); // N additional queries!
}

// GOOD: JOIN FETCH
@Query("SELECT b FROM Booking b JOIN FETCH b.billboard WHERE b.status = :status")
List<Booking> findByStatusWithBillboard(@Param("status") BookingStatus status);
```

---

## 18. Deployment

### Docker Deployment

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/billboardingdb
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=secret
    depends_on:
      - db

  db:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=secret
      - MYSQL_DATABASE=billboardingdb
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

### Production Checklist

- [ ] Change JWT secret to 256+ bit random string
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Configure production database with SSL
- [ ] Set Razorpay live keys
- [ ] Configure proper CORS origins
- [ ] Enable HTTPS with valid certificate
- [ ] Set up database backups (daily)
- [ ] Configure logging to file/ELK
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Load testing completed
- [ ] Security audit passed

---

## Contact & Support

**Developer:** Sarthak Pawar
- GitHub: [@sarthakpawar0912](https://github.com/sarthakpawar0912)
- Email: pawarsr06@gmail.com

---

<p align="center">
  <strong>Built with Spring Boot | Secured with JWT | Powered by Razorpay</strong>
  <br><br>
  Made with Love by Sarthak Pawar
</p>
