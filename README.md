# Warehouse Manager Application 📦

A modern, high-performance Desktop Warehouse Management System built with **Java 17**, **JavaFX 21**, **JDBC (MS SQL Server)**, and standard object-oriented design patterns. Features a sleek dark-mode UI powered by the Catppuccin Mocha color palette.

---

## ✨ Features

- 🔒 **Authentication & Role-Based Access Control (RBAC)**
  - Secure password hashing using PBKDF2 with unique salts (`PasswordHasher`).
  - Strict two-layer authorization guards (UI & Logic layers) distinguishing **Admin** and **User** roles.
  - In-memory thread-safe user session management (`UserSession`).

- 📦 **Inventory Management**
  - Full CRUD support for polymorphic inventory items: **Raw Materials** (`RawMaterial`) and **Finished Products** (`FinishedProduct`).
  - Dynamic form UI with interactive toggle buttons switching relevant fields (Supplier vs. Sell Price).
  - Customizable low-stock alert thresholds (`minStockLevel`) for individual items.

- ⚡ **Asynchronous Operations & Performance**
  - Background database loading, importing, exporting, and sorting utilizing Java worker threads and `Platform.runLater()` to preserve smooth 60 FPS UI responsiveness.
  - Custom in-place Selection Sort algorithm (`sortByQuantityDesc`) and `Comparator` sorting.

- 🎨 **Modern Dark Mode UI & Dynamic TableView**
  - Custom CSS styling (`main.css`) using Catppuccin Mocha colors.
  - JavaFX `TableView` custom cell rendering (`setCellFactory`):
    - Color-coded category tags (Orange for Raw Materials, Green for Finished Products).
    - Dynamic red highlight for items falling below their minimum stock threshold.

- 💾 **Data Persistence & Offline Backup**
  - Primary relational persistence layer via Microsoft SQL Server (JDBC).
  - Built-in binary object serialization (`saveToFile` / `loadFromFile`) for data backup and restoration.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 (LTS) |
| **UI Framework** | JavaFX 21 (FXML & CSS) |
| **Database** | Microsoft SQL Server (JDBC Driver `12.6.2`) |
| **Build & Dependency Management** | Apache Maven |
| **Design Patterns** | DAO Pattern, Singleton, Factory/Polymorphism, MVC Controller |

---

## 📁 Project Structure

```
WarehouseManagerWithUI/
├── src/
│   ├── main/
│   │   ├── java/com/warehousemanager/
│   │   │   ├── App.java                   # Application entry point
│   │   │   ├── config/                    # Database configuration (DatabaseConfig)
│   │   │   ├── exceptions/                # Custom application exceptions (AppException)
│   │   │   ├── models/
│   │   │   │   ├── dto/                   # Data Transfer Objects (LoginRequest)
│   │   │   │   └── entity/                # Domain entities (Goods, RawMaterial, FinishedProduct, User, Warehouse, IGoods)
│   │   │   ├── repositories/              # Data Access layer (GoodsRepository, UserRepository)
│   │   │   ├── services/                  # Business logic (AuthService, WarehouseService)
│   │   │   ├── session/                   # In-memory user session singleton (UserSession)
│   │   │   ├── ui/
│   │   │   │   └── controllers/           # JavaFX FXML controllers & SceneSwitcher
│   │   │   └── utils/                     # Utility helpers (Validator, PasswordHasher)
│   │   └── resources/com/warehousemanager/ui/
│   │       ├── styles/main.css            # Catppuccin dark mode CSS stylesheet
│   │       └── views/                     # FXML view layouts (login, register, dashboard, add_goods, goods_list)
├── pom.xml                                # Maven build configuration
├── README.md                              # Project documentation
├── RoadMap_DuAn.md                        # Development roadmap
├── NhatKy_HocTap.md                       # Study journal and concepts explanation
└── PhongVan_CoreJava_QA.md                # Core Java interview Q&A
```

---

## 🚀 Getting Started

### Prerequisites

- **JDK 17** or higher installed and configured in system PATH.
- **Apache Maven 3.8+** installed.
- *(Optional)* **Microsoft SQL Server** running for database connectivity.

### Running the Application

1. **Clone the repository:**
   ```bash
   git clone https://github.com/tuantutanghuynh/WarehouseManager-JavaFx.git
   cd WarehouseManager-JavaFx
   ```

2. **Build and launch with Maven:**
   ```bash
   mvn clean javafx:run
   ```

---

## 🔑 Default Test Accounts

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin` | `admin123` | **ADMIN** | Full access (Import, Export, Sort, Delete, Backup & Restore) |
| `user1` | `user123` | **USER** | Standard access (Import, Export, Sort; Delete button disabled) |

---

## 📄 License

This project is open source and available for educational and commercial reference.
