# Smart Campus Lost & Found Management System

A desktop application designed for universities and campuses to help students, faculty, and security personnel report, search, match, and claim lost or found belongings.

Built with **Java**, **Swing**, and **SQLite**, adhering to Object-Oriented Programming (OOP) principles, clean layered architecture, robust database management, and custom exception handling.

---

## 📌 Project Overview & Features

- **Centralized Campus Repository**: Report both **Lost** items and **Found** items across campus locations (Library, Labs, Cafeteria, Sports Ground, Hostels, etc.).
- **Smart Matching Engine**: Algorithmic scoring heuristic that cross-matches Lost and Found reports based on category, location, keyword overlap (tokenization & Jaccard index), and occurrence date. Automatically alerts users of potential matches.
- **Verification & Claim System**:
  - Finders can attach security verification questions (e.g., *"What is the sticker on the back?"*, *"What name is inside?"*).
  - Claimants provide proof of ownership.
  - Finders or Campus Security Admins can review, approve, or reject claims with custom resolution notes.
- **Multi-Role Authentication**:
  - **Student / Faculty**: Report items, view belongings, submit claims, track claims, and receive in-app notifications.
  - **Campus Security Admin**: Full system oversight, audit dashboard, item moderation, user management, and CSV report export.
- **Modern Swing UI**:
  - Clean card-based design with custom status pill badges (`OPEN`, `PENDING REVIEW`, `RETURNED`, `LOST`, `FOUND`).
  - Search & multi-criteria filtering (Type, Category, Campus Location, Status).
  - Photo attachment & thumbnail preview support.
  - Real-time in-app notification center.
- **Data Export & Campus Auditing**: One-click CSV export of all campus item reports and claim histories.

---

## 🛠️ Technology Stack & Tools Used

| Tool / Technology | Version / Specification | Role in Project |
|---|---|---|
| **Java** | Standard Edition (SE 8+) | Core Programming Language |
| **Java Swing & AWT** | Built-in Java Desktop GUI | User Interface (Cards, Tables, Modals, Forms) |
| **SQLite & JDBC** | SQLite 3 (`sqlite-jdbc-3.49.1.0.jar`) | Embedded Relational Database |

---

## 🏛️ Architecture & OOP Design

The project is structured into distinct architectural layers:

```
src/com/campus/lostfound/
├── config/             # Application constants, locations, file paths
├── model/              # OOP Domain Models (Inheritance, Polymorphism)
│   ├── BaseItem.java       (Abstract base class)
│   ├── LostItem.java       (Concrete subclass for Lost belongings)
│   ├── FoundItem.java      (Concrete subclass for Found belongings)
│   ├── User.java           (User domain model)
│   ├── Claim.java          (Claim & verification model)
│   ├── Category.java       (Category domain model)
│   └── MatchSuggestion.java(Smart matching suggestion)
├── exception/          # Custom Exception Hierarchy
│   ├── AppException.java
│   ├── DatabaseException.java
│   ├── AuthenticationException.java
│   ├── ValidationException.java
│   └── ResourceNotFoundException.java
├── dao/                # Data Access Object (DAO) Pattern
│   ├── UserDao.java, ItemDao.java, ClaimDao.java, ...
│   └── impl/SQLite*Dao.java (Prepared statements, transaction safety)
├── service/            # Business Logic Layer
│   ├── AuthService.java, ItemService.java, ClaimService.java, ...
├── util/               # Helper Utilities
│   ├── DatabaseManager.java (SQLite singleton, DDL migrations, seeding)
│   ├── PasswordUtil.java    (SHA-256 with secure random salt)
│   ├── SmartMatcher.java    (Smart similarity scoring engine)
│   ├── ImageUtil.java       (Image processing & thumbnailing)
│   └── DateUtil.java        (ISO-8601 formatting & parsing)
└── ui/                 # Swing Presentation Layer
    ├── MainWindow.java      (Main navigation frame)
    ├── auth/                (Login & Registration Dialogs)
    ├── components/          (ModernButton, StatusBadge, CardPanel, CustomTableRenderer)
    ├── panels/              (DashboardPanel, BrowseItemsPanel, ClaimsPanel, AdminPanel, ...)
    └── theme/               (Design tokens, fonts, colors)
```

### OOP Principles Applied:
1. **Abstraction**:
   - `BaseItem` defines common attributes and abstract methods (`getItemType()`, `getSpecificAttributeLabel()`, `getSpecificAttributeValue()`, `getDetailsSummary()`).
   - DAO interfaces decouple database implementations from business services.
2. **Inheritance**:
   - `LostItem` extends `BaseItem` with lost-specific fields (`rewardAmount`, `lastSeenDetails`).
   - `FoundItem` extends `BaseItem` with found-specific fields (`storageLocation`, `verificationQuestion`).
   - Custom exceptions extend `AppException`.
3. **Polymorphism**:
   - Items table and services operate uniformly on `BaseItem` references, dynamically resolving type-specific behavior and UI badges at runtime.
4. **Encapsulation**:
   - All fields are private/protected with controlled getters, setters, and validation methods.
5. **Exception Handling**:
   - Granular exception types (`ValidationException`, `AuthenticationException`, `DatabaseException`, `ResourceNotFoundException`) allow clean user feedback and maintain stable application execution.

---

## 🗄️ Database Management (SQLite)

- **Database File**: `data/campus_lost_found.db`
- **Foreign Keys**: Enforced using `PRAGMA foreign_keys = ON;`.
- **Prepared Statements**: Used for all queries to prevent SQL injection.
- **Automatic Schema Migration & Seeding**: The system automatically provisions tables and seeds default categories, administrator account, and realistic demo campus items on first run.

### Database Tables:
- `users`: User profiles, credentials (SHA-256 hash + salt), contact, roles (`STUDENT`, `FACULTY`, `ADMIN`).
- `categories`: Item categories (Electronics, Wallets, IDs, Keys, Books, etc.).
- `items`: Polymorphic item storage with discriminator `item_type` (`LOST` or `FOUND`), location, dates, photos, and status.
- `claims`: Verification claims submitted by students with proof details and reviewer feedback.
- `notifications`: User notifications for claim status updates and smart match alerts.
- `audit_logs`: Activity logging.

---

## 🚀 How to Run the Project

### Option 1: Using One-Click Batch Scripts (Windows)
1. Double-click **`build_and_run.bat`** to compile and launch.
2. Or run **`build.bat`** to compile, followed by **`run.bat`** to start.

### Option 2: Running via Terminal / Command Prompt

```powershell
# Compile all sources into bin
javac -cp ".;lib\sqlite-jdbc-3.49.1.0.jar" -d bin src\com\campus\lostfound\*.java src\com\campus\lostfound\config\*.java src\com\campus\lostfound\model\*.java src\com\campus\lostfound\exception\*.java src\com\campus\lostfound\util\*.java src\com\campus\lostfound\dao\*.java src\com\campus\lostfound\dao\impl\*.java src\com\campus\lostfound\service\*.java src\com\campus\lostfound\ui\theme\*.java src\com\campus\lostfound\ui\components\*.java src\com\campus\lostfound\ui\auth\*.java src\com\campus\lostfound\ui\panels\*.java src\com\campus\lostfound\ui\*.java

# Launch the Application
java --enable-native-access=ALL-UNNAMED -cp "bin;lib\sqlite-jdbc-3.49.1.0.jar" com.campus.lostfound.Main
```

---

## 🔑 Demo Credentials (Pre-populated)

| Role | Username | Password | Notes |
|---|---|---|---|
| **Campus Admin** | `admin` | `admin123` | Full access: Admin Audit tab, Moderation, CSV Exports |
| **Student** | `john_student` | `student123` | Standard student account with reported items |
| **Student** | `emma_watson` | `student123` | Student account with reported items |

> 💡 **Tip:** The Login Dialog includes convenient **"Quick Fill"** buttons to log in as Admin or Student with a single click!
