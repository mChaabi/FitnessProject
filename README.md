# 🏋️ FitnessProject – Fullstack Web Application

**FitnessProject** est une application web fullstack moderne dédiée à la gestion d’une plateforme de fitness.
Elle offre une expérience utilisateur fluide côté frontend et une architecture backend robuste, sécurisée et évolutive.


L’objectif principal est de permettre aux utilisateurs de :
- Consulter les cours et exercices disponibles
- Contacter l’administration via un formulaire sécurisé
- S’authentifier et interagir avec l’application de manière fiable

## 🚀 Technologies & Concepts Clés

### 🎨 Frontend
- **Angular 17+**
- Dependency Injection moderne (`inject()`)
- Interfaces TypeScript fortement typées
- Services réactifs avec `HttpClient`
- Architecture modulaire et composants standalone

### ⚙️ Backend
- **Java 21**
- Records
- Streams API
- Expressions Lambda
- **Spring Boot 3.2+**
- Spring Web
- Spring Security
- Validation JSR 380
- JPA / Hibernate


### 🛠️ Productivité & Qualité
- **Lombok** : réduction du boilerplate (`@Data`, `@Builder`, `@NoArgsConstructor`)
- **MapStruct** : mapping propre entre Entités et DTOs
- **Architecture en couches** (Controller → Service → Repository)

## 🛠️ Installation & Tests

### ⚙️ Backend

1. Configurer le fichier `application.properties`
- Base de données
- Sécurité
- Port du serveur

- ## 🧪 Tests API avec Postman


Les endpoints REST du backend peuvent être testés à l’aide de **Postman**.
Les tests suivants couvrent l’authentification, la gestion des exercices et l’envoi de messages de contact.


### 🔐 Authentification

#### ➤ Login utilisateur
- **Méthode** : `POST`
- **URL** : `/api/auth/login`
- **Headers** :
- - **Body (JSON)** :
```json
{
"email": "user@test.com",
"password": "password123"
}

👨‍💻 Auteur
Mohamed Chaabi
Développeur Fullstack – Angular | Spring Boot | Java 21
