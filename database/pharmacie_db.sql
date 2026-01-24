
DROP DATABASE IF EXISTS pharmacie_db;
CREATE DATABASE pharmacie_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE pharmacie_db;

--CRÉATION DES TABLES

-- Table Utilisateur
-- Gestion des utilisateurs du système (administrateurs et employés)
CREATE TABLE Utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'EMPLOYE')),
    INDEX idx_login (login)
) ENGINE=InnoDB;

-- Table Employe
-- Extension des informations utilisateur pour le personnel
CREATE TABLE Employe (
    id_employe INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT NOT NULL UNIQUE,
    poste VARCHAR(50),
    date_embauche DATE,
    salaire DECIMAL(10,2),
    departement VARCHAR(50),
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id_utilisateur) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table LogActivite
-- Traçabilité de toutes les actions importantes du système
CREATE TABLE LogActivite (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    date_action DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    type_action VARCHAR(50) NOT NULL,
    description TEXT,
    id_utilisateur INT,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id_utilisateur) ON DELETE SET NULL,
    INDEX idx_date_action (date_action),
    INDEX idx_type_action (type_action)
) ENGINE=InnoDB;

-- Table Client
-- Informations des clients et leur historique médical
CREATE TABLE Client (
    id_client INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(100),
    historique_medical TEXT,
    INDEX idx_nom_prenom (nom, prenom)
) ENGINE=InnoDB;

-- Table Fournisseur
-- Informations des fournisseurs de produits pharmaceutiques
CREATE TABLE Fournisseur (
    id_fournisseur INT AUTO_INCREMENT PRIMARY KEY,
    nom_societe VARCHAR(150) NOT NULL,
    adresse VARCHAR(255),
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    INDEX idx_nom_societe (nom_societe)
) ENGINE=InnoDB;

-- Table Produit
-- Catalogue des produits pharmaceutiques
CREATE TABLE Produit (
    id_produit INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    description TEXT,
    code_barre VARCHAR(50) UNIQUE,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    quantite_stock INT NOT NULL DEFAULT 0,
    seuil_alerte INT NOT NULL DEFAULT 5,
    INDEX idx_nom (nom),
    INDEX idx_code_barre (code_barre),
    INDEX idx_stock (quantite_stock)
) ENGINE=InnoDB;

-- Table CommandeFournisseur
-- Gestion des commandes passées aux fournisseurs
CREATE TABLE CommandeFournisseur (
    id_commande INT AUTO_INCREMENT PRIMARY KEY,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_reception DATETIME,
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_COURS' CHECK (statut IN ('EN_COURS', 'RECUE', 'ANNULEE')),
    id_fournisseur INT NOT NULL,
    FOREIGN KEY (id_fournisseur) REFERENCES Fournisseur(id_fournisseur) ON DELETE RESTRICT,
    INDEX idx_statut (statut),
    INDEX idx_date_creation (date_creation)
) ENGINE=InnoDB;

-- Table LigneCommande
-- Détails des produits commandés dans chaque commande
CREATE TABLE LigneCommande (
    id_ligne_cmd INT AUTO_INCREMENT PRIMARY KEY,
    id_commande INT NOT NULL,
    id_produit INT NOT NULL,
    quantite_commandee INT NOT NULL,
    prix_achat DECIMAL(10,2),
    FOREIGN KEY (id_commande) REFERENCES CommandeFournisseur(id_commande) ON DELETE CASCADE,
    FOREIGN KEY (id_produit) REFERENCES Produit(id_produit) ON DELETE RESTRICT,
    INDEX idx_commande (id_commande),
    INDEX idx_produit (id_produit)
) ENGINE=InnoDB;

-- Table Vente
-- Enregistrement des transactions de vente
CREATE TABLE Vente (
    id_vente INT AUTO_INCREMENT PRIMARY KEY,
    date_vente DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_vente DECIMAL(10,2) NOT NULL,
    id_client INT,
    id_utilisateur INT NOT NULL,
    FOREIGN KEY (id_client) REFERENCES Client(id_client) ON DELETE SET NULL,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id_utilisateur) ON DELETE RESTRICT,
    INDEX idx_date_vente (date_vente),
    INDEX idx_client (id_client),
    INDEX idx_utilisateur (id_utilisateur)
) ENGINE=InnoDB;

-- Table LigneVente
-- Détails des produits vendus dans chaque vente
CREATE TABLE LigneVente (
    id_ligne_vente INT AUTO_INCREMENT PRIMARY KEY,
    id_vente INT NOT NULL,
    id_produit INT NOT NULL,
    quantite_vendue INT NOT NULL,
    prix_applique DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_vente) REFERENCES Vente(id_vente) ON DELETE CASCADE,
    FOREIGN KEY (id_produit) REFERENCES Produit(id_produit) ON DELETE RESTRICT,
    INDEX idx_vente (id_vente),
    INDEX idx_produit (id_produit)
) ENGINE=InnoDB;

-- Insertion des utilisateurs (mot de passe: 'password123' - à hasher en production)
INSERT INTO Utilisateur (login, mot_de_passe, role) VALUES
('admin', 'password123', 'ADMIN'),
('employe1', 'password123', 'EMPLOYE'),
('employe2', 'password123', 'EMPLOYE');

-- Insertion des clients
INSERT INTO Client (nom, prenom, telephone, email, historique_medical) VALUES
('Benzarti', 'Ahmed', '22123456', 'ahmed.benzarti@email.tn', 'Allergie à la pénicilline'),
('Trabelsi', 'Leila', '98765432', 'leila.trabelsi@email.tn', 'Diabète de type 2'),
('Mansouri', 'Mohamed', '55444333', 'mohamed.mansouri@email.tn', 'Hypertension artérielle'),
('Gharbi', 'Fatma', '27888999', NULL, NULL);

-- Insertion des fournisseurs
INSERT INTO Fournisseur (nom_societe, adresse, telephone, email) VALUES
('Pharmacie Centrale de Tunisie', 'Avenue Habib Bourguiba, Tunis', '71234567', 'contact@pct.tn'),
('SIPHAT', 'Zone Industrielle La Charguia, Tunis', '71890123', 'commercial@siphat.tn'),
('Laboratoires Médis', 'Rue de Marseille, Tunis', '71456789', 'ventes@medis.tn');

-- Insertion des produits
INSERT INTO Produit (nom, description, code_barre, prix_unitaire, quantite_stock, seuil_alerte) VALUES
('Paracétamol 500mg', 'Boîte de 20 comprimés', '6191234567890', 3.50, 150, 20),
('Amoxicilline 1g', 'Boîte de 12 gélules', '6191234567891', 12.80, 80, 15),
('Doliprane 1000mg', 'Boîte de 8 comprimés effervescents', '6191234567892', 5.20, 200, 30),
('Aspirine 100mg', 'Boîte de 30 comprimés', '6191234567893', 4.50, 120, 25),
('Voltarène Gel 1%', 'Tube de 100g', '6191234567894', 18.90, 45, 10),
('Sirop pour la toux', 'Flacon de 125ml', '6191234567895', 8.30, 60, 15),
('Ibuprofène 400mg', 'Boîte de 20 comprimés', '6191234567896', 6.70, 95, 20),
('Vitamine C 500mg', 'Boîte de 30 comprimés effervescents', '6191234567897', 7.50, 40, 10);

-- Insertion des commandes fournisseur
INSERT INTO CommandeFournisseur (date_creation, date_reception, statut, id_fournisseur) VALUES
('2025-01-10 09:30:00', '2025-01-12 14:00:00', 'RECUE', 1),
('2025-01-15 11:00:00', NULL, 'EN_COURS', 2),
('2025-01-16 10:15:00', NULL, 'EN_COURS', 3);

-- Insertion des lignes de commande
INSERT INTO LigneCommande (id_commande, id_produit, quantite_commandee, prix_achat) VALUES
(1, 1, 100, 2.80),
(1, 3, 150, 4.20),
(1, 4, 80, 3.60),
(2, 2, 50, 10.50),
(2, 5, 30, 15.20),
(3, 6, 40, 6.80),
(3, 7, 60, 5.40);

-- Insertion des ventes
INSERT INTO Vente (date_vente, total_vente, id_client, id_utilisateur) VALUES
('2025-01-17 08:45:00', 24.20, 1, 2),
('2025-01-17 10:30:00', 13.40, 2, 2),
('2025-01-17 14:15:00', 42.50, NULL, 3),
('2025-01-17 16:20:00', 18.90, 3, 2);

-- Insertion des lignes de vente
INSERT INTO LigneVente (id_vente, id_produit, quantite_vendue, prix_applique) VALUES
(1, 1, 2, 3.50),
(1, 3, 2, 5.20),
(1, 6, 1, 8.30),
(2, 4, 2, 4.50),
(2, 1, 1, 3.50),
(3, 5, 2, 18.90),
(3, 7, 1, 6.70),
(4, 5, 1, 18.90);

-- Insertion des logs d'activité
INSERT INTO LogActivite (date_action, type_action, description, id_utilisateur) VALUES
('2025-01-17 08:00:00', 'CONNEXION', 'Connexion de l\'employé au système', 2),
('2025-01-17 08:45:00', 'VENTE', 'Vente ID 1 - Montant: 24.20 TND', 2),
('2025-01-17 10:30:00', 'VENTE', 'Vente ID 2 - Montant: 13.40 TND', 2),
('2025-01-17 12:00:00', 'MAJ_STOCK', 'Réception de commande ID 1', 1),
('2025-01-17 14:00:00', 'CONNEXION', 'Connexion de l\'employé au système', 3),
('2025-01-17 14:15:00', 'VENTE', 'Vente ID 3 - Montant: 42.50 TND', 3);

-- CRÉATION DES UTILISATEURS MYSQL ET PRIVILÈGES
-- Suppression des utilisateurs s'ils existent déjà
DROP USER IF EXISTS 'admin_pharmacie'@'localhost';
DROP USER IF EXISTS 'employe_pharmacie'@'localhost';

-- Création de l'utilisateur administrateur avec tous les privilèges
CREATE USER 'admin_pharmacie'@'localhost' IDENTIFIED BY 'Admin@Pharma2025';
GRANT ALL PRIVILEGES ON pharmacie_db.* TO 'admin_pharmacie'@'localhost';

-- Création de l'utilisateur employé avec privilèges limités
CREATE USER 'employe_pharmacie'@'localhost' IDENTIFIED BY 'Employe@Pharma2025';

-- Droits SELECT, INSERT, UPDATE sur les tables de vente et stock
GRANT SELECT, INSERT, UPDATE ON pharmacie_db.Vente TO 'employe_pharmacie'@'localhost';
GRANT SELECT, INSERT, UPDATE ON pharmacie_db.LigneVente TO 'employe_pharmacie'@'localhost';
GRANT SELECT, INSERT, UPDATE ON pharmacie_db.Produit TO 'employe_pharmacie'@'localhost';
GRANT SELECT, INSERT, UPDATE ON pharmacie_db.CommandeFournisseur TO 'employe_pharmacie'@'localhost';
GRANT SELECT, INSERT, UPDATE ON pharmacie_db.LigneCommande TO 'employe_pharmacie'@'localhost';

-- Droits en lecture seule sur les tables annexes nécessaires
GRANT SELECT ON pharmacie_db.Client TO 'employe_pharmacie'@'localhost';
GRANT SELECT ON pharmacie_db.Fournisseur TO 'employe_pharmacie'@'localhost';

-- Droits pour insérer des logs (pas de lecture pour protéger les données sensibles)
GRANT INSERT ON pharmacie_db.LogActivite TO 'employe_pharmacie'@'localhost';

-- Application des privilèges
FLUSH PRIVILEGES;
-- VUES UTILES

-- Vue pour les produits en alerte de stock
CREATE VIEW V_Produits_Alerte AS
SELECT 
    id_produit,
    nom,
    code_barre,
    quantite_stock,
    seuil_alerte,
    (seuil_alerte - quantite_stock) AS quantite_manquante
FROM Produit
WHERE quantite_stock <= seuil_alerte;

-- Vue pour le chiffre d'affaires par jour
CREATE VIEW V_CA_Journalier AS
SELECT 
    DATE(date_vente) AS jour,
    COUNT(id_vente) AS nombre_ventes,
    SUM(total_vente) AS total_ca
FROM Vente
GROUP BY DATE(date_vente)
ORDER BY jour DESC;

-- Vue pour les produits les plus vendus
CREATE VIEW V_Top_Produits AS
SELECT 
    p.id_produit,
    p.nom,
    SUM(lv.quantite_vendue) AS total_vendu,
    SUM(lv.quantite_vendue * lv.prix_applique) AS ca_genere
FROM Produit p
INNER JOIN LigneVente lv ON p.id_produit = lv.id_produit
GROUP BY p.id_produit, p.nom
ORDER BY total_vendu DESC;

-- Affichage des informations de connexion
SELECT 'Base de données créée avec succès!' AS Message;
SELECT 'Utilisateur admin: admin_pharmacie / Mot de passe: Admin@Pharma2025' AS Info_Admin;
SELECT 'Utilisateur employé: employe_pharmacie / Mot de passe: Employe@Pharma2025' AS Info_Employe;



-- Créer la base de données
DROP DATABASE IF EXISTS pharmacie_db;
CREATE DATABASE pharmacie_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pharmacie_db;
-- Tables
-- Table Utilisateur
CREATE TABLE Utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'EMPLOYE'))
) ENGINE=InnoDB;

-- Table Client
CREATE TABLE Client (
    id_client INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(100),
    historique_medical TEXT
) ENGINE=InnoDB;

-- Table Fournisseur
CREATE TABLE Fournisseur (
    id_fournisseur INT AUTO_INCREMENT PRIMARY KEY,
    nom_societe VARCHAR(150) NOT NULL,
    adresse VARCHAR(255),
    telephone VARCHAR(20),
    email VARCHAR(100)
) ENGINE=InnoDB;

-- Table Produit
CREATE TABLE Produit (
    id_produit INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    description TEXT,
    code_barre VARCHAR(50) UNIQUE,
    prix_unitaire DECIMAL(10,2) NOT NULL CHECK (prix_unitaire >= 0),
    quantite_stock INT NOT NULL DEFAULT 0 CHECK (quantite_stock >= 0),
    seuil_alerte INT NOT NULL DEFAULT 10 CHECK (seuil_alerte >= 0)
) ENGINE=InnoDB;

-- Table CommandeFournisseur
CREATE TABLE CommandeFournisseur (
    id_commande INT AUTO_INCREMENT PRIMARY KEY,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_reception DATETIME,
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_COURS' CHECK (statut IN ('EN_COURS', 'RECUE', 'ANNULEE')),
    id_fournisseur INT NOT NULL,
    FOREIGN KEY (id_fournisseur) REFERENCES Fournisseur(id_fournisseur) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table LigneCommande
CREATE TABLE LigneCommande (
    id_ligne_cmd INT AUTO_INCREMENT PRIMARY KEY,
    id_commande INT NOT NULL,
    id_produit INT NOT NULL,
    quantite_commandee INT NOT NULL CHECK (quantite_commandee > 0),
    prix_achat DECIMAL(10,2) NOT NULL CHECK (prix_achat >= 0),
    FOREIGN KEY (id_commande) REFERENCES CommandeFournisseur(id_commande) ON DELETE CASCADE,
    FOREIGN KEY (id_produit) REFERENCES Produit(id_produit) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table Vente
CREATE TABLE Vente (
    id_vente INT AUTO_INCREMENT PRIMARY KEY,
    date_vente DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_vente DECIMAL(10,2) NOT NULL CHECK (total_vente >= 0),
    id_client INT,
    id_utilisateur INT NOT NULL,
    FOREIGN KEY (id_client) REFERENCES Client(id_client) ON DELETE SET NULL,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id_utilisateur) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table LigneVente
CREATE TABLE LigneVente (
    id_ligne_vente INT AUTO_INCREMENT PRIMARY KEY,
    id_vente INT NOT NULL,
    id_produit INT NOT NULL,
    quantite_vendue INT NOT NULL CHECK (quantite_vendue > 0),
    prix_applique DECIMAL(10,2) NOT NULL CHECK (prix_applique >= 0),
    FOREIGN KEY (id_vente) REFERENCES Vente(id_vente) ON DELETE CASCADE,
    FOREIGN KEY (id_produit) REFERENCES Produit(id_produit) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table LogActivite
CREATE TABLE LogActivite (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    date_action DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    type_action VARCHAR(50) NOT NULL,
    description TEXT,
    id_utilisateur INT,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id_utilisateur) ON DELETE SET NULL
) ENGINE=InnoDB;

-- INDEX POUR OPTIMISATION DES REQUÊTES
CREATE INDEX idx_produit_nom ON Produit(nom);
CREATE INDEX idx_produit_code_barre ON Produit(code_barre);
CREATE INDEX idx_vente_date ON Vente(date_vente);
CREATE INDEX idx_vente_client ON Vente(id_client);
CREATE INDEX idx_log_type ON LogActivite(type_action);
CREATE INDEX idx_log_date ON LogActivite(date_action);

-- CRÉATION DES UTILISATEURS MYSQL
-- Utilisateur Administrateur (tous droits)
DROP USER IF EXISTS 'admin_pharmacie'@'localhost';
CREATE USER 'admin_pharmacie'@'localhost' IDENTIFIED BY 'admin_password_123';
GRANT ALL PRIVILEGES ON pharmacie_db.* TO 'admin_pharmacie'@'localhost';

-- Utilisateur Employé (droits limités)
DROP USER IF EXISTS 'employe_pharmacie'@'localhost';
CREATE USER 'employe_pharmacie'@'localhost' IDENTIFIED BY 'employe_password_123';
GRANT SELECT, INSERT, UPDATE ON pharmacie_db.* TO 'employe_pharmacie'@'localhost';
GRANT DELETE ON pharmacie_db.LigneVente TO 'employe_pharmacie'@'localhost';
GRANT DELETE ON pharmacie_db.LigneCommande TO 'employe_pharmacie'@'localhost';

FLUSH PRIVILEGES;
-- DONNÉES DE TEST
-- Utilisateurs de test
INSERT INTO Utilisateur (login, mot_de_passe, role) VALUES
('admin', 'password123', 'ADMIN'),
('employe1', 'password123', 'EMPLOYE'),
('employe2', 'password123', 'EMPLOYE');

-- Clients de test
INSERT INTO Client (nom, prenom, telephone, email, historique_medical) VALUES
('Dupont', 'Jean', '71234567', 'jean.dupont@email.com', 'Allergie aux pénicillines'),
('Martin', 'Marie', '71234568', 'marie.martin@email.com', NULL),
('Bernard', 'Pierre', '71234569', 'pierre.bernard@email.com', 'Diabète de type 2');

-- Fournisseurs de test
INSERT INTO Fournisseur (nom_societe, adresse, telephone, email) VALUES
('PharmaDistrib SA', '15 Avenue de la Santé, Tunis', '71123456', 'contact@pharmadistrib.tn'),
('MediSupply', '28 Rue des Médicaments, Ariana', '71123457', 'info@medisupply.tn'),
('BioMed Import', '42 Boulevard de la République, Sfax', '74123458', 'sales@biomed.tn');

-- Produits de test
INSERT INTO Produit (nom, description, code_barre, prix_unitaire, quantite_stock, seuil_alerte) VALUES
('Paracétamol 500mg', 'Antalgique et antipyrétique - Boîte de 20 comprimés', '3401234567890', 3.50, 150, 30),
('Ibuprofène 400mg', 'Anti-inflammatoire - Boîte de 30 comprimés', '3401234567891', 5.20, 100, 25),
('Amoxicilline 1g', 'Antibiotique - Boîte de 14 comprimés', '3401234567892', 12.00, 50, 15),
('Doliprane 1000mg', 'Antalgique - Boîte de 8 comprimés', '3401234567893', 4.80, 200, 40),
('Aspirine 100mg', 'Antiagrégant plaquettaire - Boîte de 30 comprimés', '3401234567894', 3.00, 80, 20),
('Ventoline 100μg', 'Bronchodilatateur - Inhalateur', '3401234567895', 18.50, 30, 10),
('Sérum physiologique', 'Solution nasale - Boîte de 40 unidoses', '3401234567896', 6.50, 120, 30),
('Cétrizine 10mg', 'Antihistaminique - Boîte de 15 comprimés', '3401234567897', 8.20, 60, 15);

-- Commandes fournisseurs de test
INSERT INTO CommandeFournisseur (date_creation, date_reception, statut, id_fournisseur) VALUES
('2026-01-10 09:00:00', '2026-01-15 14:30:00', 'RECUE', 1),
('2026-01-12 10:30:00', NULL, 'EN_COURS', 2);

-- Lignes de commande de test
INSERT INTO LigneCommande (id_commande, id_produit, quantite_commandee, prix_achat) VALUES
(1, 1, 100, 2.50),
(1, 2, 50, 3.80),
(2, 3, 30, 9.00);

-- Ventes de test
INSERT INTO Vente (date_vente, total_vente, id_client, id_utilisateur) VALUES
('2026-01-16 10:30:00', 15.70, 1, 2),
('2026-01-16 14:20:00', 8.00, NULL, 2),
('2026-01-17 09:15:00', 24.80, 3, 3);

-- Lignes de vente de test
INSERT INTO LigneVente (id_vente, id_produit, quantite_vendue, prix_applique) VALUES
(1, 1, 2, 3.50),
(1, 4, 2, 4.80),
(2, 5, 2, 3.00),
(2, 8, 1, 8.20),
(3, 3, 2, 12.00);

-- Logs d'activité de test
INSERT INTO LogActivite (date_action, type_action, description, id_utilisateur) VALUES
('2026-01-16 08:00:00', 'CONNEXION', 'Connexion de l''utilisateur: employe1', 2),
('2026-01-16 10:30:00', 'VENTE', 'Vente ID 1 - Montant: 15.70 TND', 2),
('2026-01-17 08:00:00', 'CONNEXION', 'Connexion de l''utilisateur: employe2', 3),
('2026-01-17 09:15:00', 'VENTE', 'Vente ID 3 - Montant: 24.80 TND', 3);

-- MISE À JOUR DES STOCKS APRÈS LES VENTES
UPDATE Produit SET quantite_stock = quantite_stock - 2 WHERE id_produit = 1;
UPDATE Produit SET quantite_stock = quantite_stock - 2 WHERE id_produit = 4;
UPDATE Produit SET quantite_stock = quantite_stock - 2 WHERE id_produit = 5;
UPDATE Produit SET quantite_stock = quantite_stock - 1 WHERE id_produit = 8;
UPDATE Produit SET quantite_stock = quantite_stock - 2 WHERE id_produit = 3;

-- VÉRIFICATION
SELECT 'Base de données créée avec succès !' AS Message;
SELECT COUNT(*) AS NombreUtilisateurs FROM Utilisateur;
SELECT COUNT(*) AS NombreProduits FROM Produit;
SELECT COUNT(*) AS NombreClients FROM Client;
SELECT COUNT(*) AS NombreVentes FROM Vente;