
# Billboarding Management System – Backend

A full-featured backend system for managing billboard and hoarding advertisements, built using **Spring Boot** and **MySQL**.

## 🚀 Features

### 🔐 Authentication & Security
- JWT-based authentication
- Role-based access (Admin, Owner, Advertiser)
- Two-Factor Authentication (Email OTP)
- Login history tracking
- Password change & security settings

### 🏢 Billboard & Campaign Management
- Billboard listing with geo-location support
- Campaign creation & booking
- Availability checking
- Favorites management
- Heatmap & analytics APIs

### 💳 Payments & Invoicing
- Razorpay payment integration
- Invoice generation (PDF)
- GST invoice support
- Refund management
- Payment history tracking
- Webhook handling

### 📊 Dashboards & Analytics
- Admin dashboard & reports
- Owner revenue analytics
- Advertiser campaign analytics
- Monthly & daily revenue reports

### 🔔 Notifications
- Email notifications
- Notification preferences
- Campaign & booking alerts

---

## 🛠 Tech Stack

- **Java 21**
- **Spring Boot**
- **Spring Security**
- **Spring Data JPA**
- **JWT**
- **MySQL**
- **Razorpay**
- **OpenPDF (Invoice generation)**

---

## ⚙️ Setup & Run

```bash
./mvnw clean install
./mvnw spring-boot:run
