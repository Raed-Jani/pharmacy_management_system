<p align="center">
  <img src="resources/images/app_icon.png" alt="BioVera Pro Logo" width="120"/>
</p>

<h1 align="center">💊 BioVera Pro</h1>

<p align="center">
  <strong>A comprehensive pharmacy management desktop application</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX 21"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL 8.0"/>
  <img src="https://img.shields.io/badge/Platform-Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white" alt="Windows"/>
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License"/>
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-getting-started">Getting Started</a> •
  <a href="#%EF%B8%8F-architecture">Architecture</a> •
  <a href="#%EF%B8%8F-database-schema">Database</a> •
  <a href="#-contributing">Contributing</a>
</p>

---

## 📖 About

**BioVera Pro** is a full-featured desktop application designed to streamline pharmacy operations. Built with Java and JavaFX, it provides an intuitive interface for managing products, sales, suppliers, orders, clients, and employees — all backed by a MySQL database with role-based access control.

> Whether you're running a small independent pharmacy or managing a larger operation, BioVera Pro gives you the tools to track inventory, process sales, manage supplier orders, and generate insightful reports — all from a single, polished desktop interface.

## ✨ Features

| Module | Description |
|---|---|
| 🔐 **Authentication** | Secure login system with role-based access (Admin / Employee) |
| 📊 **Dashboard** | Real-time statistics and key performance indicators |
| 💊 **Product Management** | Full CRUD for pharmaceutical products with barcode support and low-stock alerts |
| 🛒 **Sales Management** | Point-of-sale interface with automatic stock deduction and receipt generation |
| 📦 **Order Management** | Supplier order tracking with status workflow (Pending → Received → Cancelled) |
| 👥 **Client Management** | Client database with medical history tracking |
| 🏭 **Supplier Management** | Supplier directory with contact information |
| 👤 **User Management** | Admin panel for managing system users and employees |
| 📄 **Reports & Export** | Generate and export reports as PDF documents |
| 🗃️ **Database Backup** | Built-in database backup functionality |
| 📋 **Activity Logs** | Full audit trail of all system actions |
| 🔔 **Notifications** | Toast notification system for real-time user feedback |
| 🌙 **Dark Mode** | Toggle between light and dark themes |



## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| **Java 21** | Core language |
| **JavaFX 21** | UI framework (FXML + CSS) |
| **MySQL 8.0** | Relational database |
| **MySQL Connector/J 8.0.33** | JDBC driver |
| **OpenPDF 1.3.42** | PDF report generation |
| **jpackage** | Native Windows executable packaging |

## 📋 Prerequisites

Before running BioVera Pro, make sure you have the following installed:

| Requirement | Version | Link |
|---|---|---|
| JDK | 21+ | [Download](https://jdk.java.net/21/) |
| MySQL | 8.0+ | [Download](https://dev.mysql.com/downloads/mysql/) |
| JavaFX SDK | 21 | Included in `lib/lib/` |

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Raed-Jani/pharmacy_management_system.git
cd pharmacy
```

### 2. Set up the database

Start your MySQL server, then run the provided SQL script:

```bash
mysql -u root -p < database/pharmacie_db.sql
```

This will:
- Create the `pharmacie_db` database with all required tables
- Set up database indexes and optimized views
- Create MySQL users with appropriate privileges
- Insert sample data for testing

> [!NOTE]
> **Default database users created by the script:**
>
> | User | Password | Privileges |
> |---|---|---|
> | `admin_pharmacie` | `admin_password_123` | Full access |
> | `employe_pharmacie` | `employe_password_123` | Limited access |

### 3. Configure the database connection

Open `src/com/pharmacie/utils/DBConnection.java` and set your MySQL root password:

```java
private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD"; // ← Replace this
```

### 4. Compile and run

#### Option A — Using IntelliJ IDEA (Recommended)

1. Open the project in IntelliJ IDEA
2. Right-click the `resources/` folder → **Mark Directory as → Resources Root**
3. Add all JARs from `lib/` and `lib/lib/` to your module dependencies
4. Add VM options for JavaFX modules:
   ```
   --module-path lib/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base
   ```
5. Run `Launcher.java`

#### Option B — Using the command line

```bash
# Compile
javac --release 21 \
  -d out/production/pharmacie \
  -cp "lib/mysql-connector-j-8.0.33.jar;lib/openpdf-1.3.42.jar;lib/lib/javafx.base.jar;lib/lib/javafx.controls.jar;lib/lib/javafx.fxml.jar;lib/lib/javafx.graphics.jar" \
  -sourcepath src \
  src/com/pharmacie/Launcher.java

# Run
java --module-path lib/lib \
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
  -cp "out/production/pharmacie;lib/mysql-connector-j-8.0.33.jar;lib/openpdf-1.3.42.jar" \
  com.pharmacie.Launcher
```

### 5. Build a standalone executable (Windows)

A PowerShell script is included to package the app as a native Windows `.exe` using `jpackage`:

```powershell
.\Build_BioVera_Exe.ps1
```

The executable will be generated at `output_exe/BioVera Pro/BioVera Pro.exe`.

You can also create a desktop shortcut:

```powershell
.\Create_Desktop_Shortcut.ps1
```

## 🔑 Default Login Credentials

| Username | Password | Role |
|---|---|---|
| `admin` | `password123` | Administrator |
| `employe1` | `password123` | Employee |
| `employe2` | `password123` | Employee |

> [!WARNING]
> These are development credentials. **Change all default passwords** before deploying to a production environment.

## 🏗️ Architecture

The project follows a clean **layered architecture** (Model → DAO → Service → UI):

```
src/com/pharmacie/
│
├── Launcher.java                  # Application entry point
├── MainApp.java                   # JavaFX Application class
│
├── model/                         # 📦 Data models (POJOs)
│   ├── Client.java
│   ├── CommandeFournisseur.java
│   ├── Fournisseur.java
│   ├── LigneCommandeFournisseur.java
│   ├── LigneVente.java
│   ├── LogActivite.java
│   ├── Produit.java
│   ├── Utilisateur.java
│   └── Vente.java
│
├── dao/                           # 🗄️ Data Access Objects (JDBC)
│   ├── ClientDAO.java
│   ├── CommandeFournisseurDAO.java
│   ├── FournisseurDAO.java
│   ├── LigneCommandeFournisseurDAO.java
│   ├── LigneVenteDAO.java
│   ├── LogActiviteDAO.java
│   ├── ProduitDAO.java
│   ├── UtilisateurDAO.java
│   └── VenteDAO.java
│
├── service/                       # ⚙️ Business logic layer
│   ├── AuthenticationService.java
│   ├── DashboardStatisticsService.java
│   ├── DatabaseBackupService.java
│   ├── ExportService.java
│   ├── GestionClientService.java
│   ├── GestionCommande.java
│   ├── GestionStock.java
│   └── GestionVente.java
│
├── ui/                            # 🖥️ User interface layer
│   ├── controller/                #    FXML Controllers (12 screens)
│   │   ├── AccueilController.java
│   │   ├── BaseController.java
│   │   ├── ConsultationLogsController.java
│   │   ├── DashboardController.java
│   │   ├── GestionClientsController.java
│   │   ├── GestionCommandesController.java
│   │   ├── GestionFournisseursController.java
│   │   ├── GestionProduitsController.java
│   │   ├── GestionUtilisateursController.java
│   │   ├── GestionVentesController.java
│   │   ├── LoginController.java
│   │   └── RapportsController.java
│   └── notification/
│       └── ToastNotification.java
│
├── util/
│   └── ThemeManager.java          # 🎨 Light/Dark theme toggling
├── utils/
│   └── DBConnection.java          # 🔗 Database connection manager
└── exception/                     # ❌ Custom exceptions
    ├── ConnexionEchoueeException.java
    ├── ProduitIntrouvableException.java
    └── StockInsuffisantException.java
```

## 🗄️ Database Schema

The application uses **10 tables** organized around core pharmacy operations:

```
┌──────────────┐     ┌──────────────┐     ┌──────────────────────┐
│  Utilisateur │     │    Client     │     │    Fournisseur       │
│──────────────│     │──────────────│     │──────────────────────│
│ id_utilisateur│    │ id_client    │     │ id_fournisseur       │
│ login        │     │ nom          │     │ nom_societe          │
│ mot_de_passe │     │ prenom       │     │ adresse              │
│ role         │     │ telephone    │     │ telephone            │
└──────┬───────┘     │ email        │     │ email                │
       │             │ historique_  │     └──────────┬───────────┘
       │             │  medical     │                │
       │             └──────┬───────┘                │
       │                    │                        │
       ▼                    ▼                        ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────────────┐
│    Vente     │     │ LigneVente   │     │ CommandeFournisseur  │
│──────────────│     │──────────────│     │──────────────────────│
│ id_vente     │◄───▶│ id_vente     │     │ id_commande          │
│ date_vente   │     │ id_produit   │     │ date_creation        │
│ total_vente  │     │ quantite     │     │ date_reception       │
│ id_client    │     │ prix_applique│     │ statut               │
│ id_utilisateur│    └──────┬───────┘     │ id_fournisseur       │
└──────────────┘            │             └──────────┬───────────┘
                            │                        │
                            ▼                        ▼
                     ┌──────────────┐     ┌──────────────────────┐
                     │   Produit    │     │   LigneCommande      │
                     │──────────────│     │──────────────────────│
                     │ id_produit   │◄───▶│ id_commande          │
                     │ nom          │     │ id_produit           │
                     │ code_barre   │     │ quantite_commandee   │
                     │ prix_unitaire│     │ prix_achat           │
                     │ quantite_stock│    └──────────────────────┘
                     │ seuil_alerte │
                     └──────────────┘

┌──────────────┐
│ LogActivite  │     Tracks all system actions
│──────────────│     for auditing purposes
│ id_log       │
│ date_action  │
│ type_action  │
│ description  │
│id_utilisateur│
└──────────────┘
```

**Database views** included for quick analytics:

| View | Purpose |
|---|---|
| `V_Produits_Alerte` | Products below their stock alert threshold |
| `V_CA_Journalier` | Daily revenue summary |
| `V_Top_Produits` | Best-selling products ranking |

## 📁 Project Structure

```
pharmacie/
├── src/                           # Java source code
├── resources/
│   ├── fxml/                      # FXML view definitions (11 screens)
│   ├── css/                       # Stylesheets (light & dark themes)
│   └── images/                    # Application icons and assets
├── database/
│   └── pharmacie_db.sql           # Complete database setup script
├── lib/
│   ├── mysql-connector-j-8.0.33.jar
│   ├── openpdf-1.3.42.jar
│   └── lib/                       # JavaFX SDK JARs & native DLLs
├── Build_BioVera_Exe.ps1          # Build native Windows executable
├── Create_Desktop_Shortcut.ps1    # Create desktop shortcut
├── app_icon.ico                   # Windows executable icon
├── .editorconfig                  # Code style configuration
├── .gitignore
├── LICENSE
└── README.md
```


## 📄 License

This project is licensed under the [MIT License](LICENSE).

## 👨‍💻 Author

**Raed** — [GitHub](https://github.com/Raed-Jani)

---

<p align="center">
  Made with ❤️ using Java & JavaFX
</p>
