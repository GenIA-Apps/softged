# SoftGED

SoftGED est un POC de **GED intelligente pour documents d’architecture**.

L’objectif du projet est de dépasser une GED classique, qui se limite à stocker et classer des fichiers, pour proposer une GED augmentée par IA capable de rendre des plans PDF interrogeables via un assistant RAG.

Le cas d’usage principal est le suivant :

> Un utilisateur upload des documents PDF d’architecture dans un projet.
> L’application extrait le contenu textuel et visuel des pages, génère des résumés visuels, indexe les contenus dans Qdrant, puis permet de poser des questions sur l’ensemble des documents indexés du projet.

---

## Objectifs du POC

Le projet démontre plusieurs notions importantes :

* gestion documentaire par projet ;
* authentification Firebase ;
* API Spring Boot sécurisée par JWT Firebase ;
* upload de PDF ;
* extraction texte page par page ;
* rendu des pages PDF en images PNG ;
* analyse visuelle des pages avec un modèle vision ;
* indexation vectorielle dans Qdrant ;
* recherche RAG filtrée par utilisateur et projet ;
* réponse sourcée avec document, page et image source ;
* frontend Vue 3 avec login, upload, traitement et chat RAG.

---

## Stack technique

### Backend

* Java
* Spring Boot
* Spring Security
* Firebase Authentication
* Firebase Admin SDK
* MyBatis
* MySQL
* Spring AI
* OpenAI
* Qdrant
* Apache PDFBox

### Frontend

* Vue 3
* TypeScript
* Vue Router
* Pinia
* Firebase JS SDK
* Axios

### Infrastructure applicative

* MySQL comme source de vérité métier ;
* Qdrant comme index vectoriel ;
* stockage local des PDF et des images générées ;
* Firebase pour l’identité utilisateur.

---

## Fonctionnalités actuelles

### Authentification

L’utilisateur se connecte via Firebase Authentication.

Le frontend récupère un Firebase ID Token, puis l’envoie au backend dans le header :

```http
Authorization: Bearer <firebase_id_token>
```

Le backend vérifie ce token avec Firebase Admin SDK.

Toutes les routes API sont protégées par défaut, sauf les routes techniques explicitement autorisées.

---

### Gestion des projets

Un utilisateur peut :

* créer un projet ;
* lister ses projets ;
* ouvrir un projet ;
* supprimer un projet.

Chaque projet est rattaché à l’utilisateur Firebase via son `ownerUid`.

Cela permet d’isoler les données par utilisateur.

---

### Gestion des documents

Dans un projet, un utilisateur peut :

* uploader un PDF ;
* lister les documents du projet ;
* traiter un document ;
* voir son statut.

Les documents sont stockés localement et leurs métadonnées sont persistées en MySQL.

Exemples de statuts :

```txt
UPLOADED
EXTRACTING
EXTRACTED
SUMMARIZING
SUMMARIZED
INDEXING
INDEXED
FAILED
```

---

## Pipeline documentaire

Le pipeline complet est le suivant :

```txt
PDF uploadé
  ↓
Métadonnées document en MySQL
  ↓
Extraction texte page par page
  ↓
Rendu PNG de chaque page
  ↓
Résumé visuel par IA
  ↓
Construction d’un chunk enrichi par page
  ↓
Embedding OpenAI
  ↓
Indexation Qdrant
  ↓
Question utilisateur
  ↓
Recherche vectorielle filtrée
  ↓
Réponse RAG sourcée
```

---

## Pourquoi une ingestion ?

Le projet ne traite pas directement le PDF complet à chaque question.

Une GED RAG doit préparer les documents une fois, puis réutiliser cette représentation préparée.

L’ingestion sert donc à transformer un PDF brut en contenu exploitable :

```txt
PDF brut
→ pages
→ texte extrait
→ images PNG
→ résumés visuels
→ chunks
→ embeddings
→ index vectoriel
```

Cette approche permet :

* d’éviter de renvoyer le PDF complet au modèle à chaque question ;
* de réduire les coûts et la latence ;
* de filtrer par utilisateur, projet, document ou page ;
* de retourner des sources précises ;
* de rendre la recherche multi-documents possible.

---

## Type de RAG implémenté

Le RAG actuel est un **RAG textuel enrichi par vision**.

Chaque page PDF devient un chunk logique.

Pour chaque page, le contenu indexé est composé de :

```txt
Texte extrait de la page
+
Résumé visuel généré depuis l’image PNG de la page
```

Ensuite, Spring AI génère automatiquement l’embedding lors de l’appel :

```java
vectorStore.add(documents);
```

Dans Qdrant :

```txt
1 page PDF
= 1 chunk
= 1 embedding
= 1 point Qdrant
```

Le vecteur Qdrant contient les coordonnées numériques de l’embedding.
Le payload contient les métadonnées nécessaires pour retrouver la source.

Exemple de payload :

```json
{
  "ownerUid": "...",
  "projectId": "8",
  "documentId": "1",
  "pageId": "1",
  "pageNumber": "1",
  "originalFilename": "interior_model.pdf",
  "imagePath": "./storage/pages/user/document/page-1.png",
  "sourceType": "DOCUMENT_PAGE"
}
```

---

## Portée de la recherche RAG

L’endpoint RAG actuel est :

```http
POST /api/projects/{projectId}/rag
```

Il recherche dans **tous les documents indexés du projet courant**.

Le filtre Qdrant est basé sur :

```txt
ownerUid
projectId
```

Donc une question posée sur un projet peut retrouver des pages issues de plusieurs documents du même projet.

Ce comportement est volontaire pour une GED :

```txt
Projet
├── Document A
├── Document B
└── Document C

Question utilisateur
→ recherche dans toutes les pages indexées du projet
```

Une recherche limitée à un seul document pourra être ajoutée plus tard avec un endpoint dédié.

---

## Architecture backend

Le backend suit un **DDD simplifié**, sans ports/adapters formels.

Le package racine est :

```java
com.mediarium.softged
```

La structure est organisée par domaine métier.

Structure type :

```txt
domain
├── businessmodel
├── controller
├── dataservice
├── dto
└── service
```

Exemple :

```txt
project
├── businessmodel
│   └── Project
├── controller
│   └── ProjectController
├── dataservice
│   ├── ProjectDataService
│   └── ProjectMapper
├── dto
│   ├── CreateProjectRequest
│   ├── UpdateProjectRequest
│   └── ProjectResponse
└── service
    └── ProjectService
```

---

## Règles architecturales retenues

Le projet applique les règles suivantes :

```txt
Controller → Service métier → DataService → Mapper MyBatis → MySQL
```

### Controller

Le controller est responsable :

* de l’exposition HTTP ;
* de la validation des DTO ;
* de la récupération de l’utilisateur connecté ;
* du mapping entre DTO et modèle métier ;
* du mapping entre modèle métier et réponse API.

### Service métier

Le service métier :

* contient les règles métier ;
* manipule des business models ;
* ne retourne pas de DTO ;
* ne dépend pas de MyBatis ;
* ne connaît pas les détails SQL.

### DataService

Le DataService :

* encapsule l’accès base de données ;
* expose des méthodes orientées métier ;
* appelle les mappers MyBatis.

### Mapper MyBatis

Le mapper :

* contient le SQL ;
* reste un détail technique ;
* n’est pas injecté directement dans les services métier.

---

## Décisions importantes

### MySQL est la source de vérité métier

MySQL contient :

* les projets ;
* les documents ;
* les pages extraites ;
* les statuts ;
* les métadonnées ;
* les chemins de stockage.

Qdrant n’est pas utilisé comme base métier.

---

### Qdrant est un index de recherche

Qdrant contient :

* les embeddings ;
* le contenu indexé ;
* les payloads nécessaires au filtrage ;
* les références vers les sources.

Si Qdrant est vidé, les données métier restent en MySQL.

---

### Les DTO ne traversent pas le domaine métier

Les DTO sont des contrats d’API.

Ils ne doivent pas polluer les services métier.

Le service métier retourne des business models, puis le controller les transforme en DTO de réponse.

---

### Les documents visuels sont traités page par page

Chaque page PDF est rendue en image PNG complète.

Ce choix permet :

* de garder une source visuelle exploitable ;
* d’envoyer l’image au modèle vision ;
* de générer un résumé visuel ;
* d’afficher plus tard la page source dans le frontend.

Pour le POC, le niveau de granularité est :

```txt
1 page = 1 chunk
```

Plus tard, le système pourra évoluer vers :

```txt
1 page
→ plusieurs zones
→ plusieurs crops
→ highlights
→ bbox
```

---

## Architecture frontend

Le frontend Vue 3 est organisé par domaines.

Structure simplifiée :

```txt
src
├── app
│   └── router.ts
├── shared
│   ├── api
│   │   └── apiClient.ts
│   └── firebase
│       └── firebase.ts
├── auth
│   ├── stores
│   │   └── auth.store.ts
│   └── views
│       └── LoginView.vue
├── project
│   ├── services
│   │   └── projectApi.ts
│   └── views
│       ├── ProjectListView.vue
│       └── ProjectDetailView.vue
├── document
│   └── services
│       └── documentApi.ts
└── rag
    └── services
        └── ragApi.ts
```

Le frontend permet actuellement :

* de se connecter avec Firebase ;
* de créer des projets ;
* d’ouvrir un projet ;
* d’uploader un PDF ;
* de lancer le traitement complet du document ;
* de poser une question RAG sur le projet ;
* d’afficher la réponse et les sources.

---

## Configuration locale

Les secrets ne doivent pas être commités.

Fichiers ignorés :

```gitignore
.env
.env.local
src/main/resources/application-local.yml
src/main/resources/firebase-service-account.json
```

Le backend utilise un fichier local :

```txt
src/main/resources/application-local.yml
```

Le frontend utilise :

```txt
.env.local
```

---

## Exemple de configuration backend

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/softged
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}

  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
      embedding:
        options:
          model: text-embedding-3-small

    vectorstore:
      qdrant:
        host: ${QDRANT_HOST}
        port: 6334
        api-key: ${QDRANT_API_KEY}
        collection-name: softged_documents
        use-tls: true

firebase:
  service-account-path: firebase-service-account.json

app:
  storage:
    documents-path: ./storage/documents
    pages-path: ./storage/pages
```

---

## Exemple de configuration frontend

```env
VITE_API_BASE_URL=http://localhost:8080/api

VITE_FIREBASE_API_KEY=
VITE_FIREBASE_AUTH_DOMAIN=
VITE_FIREBASE_PROJECT_ID=
VITE_FIREBASE_STORAGE_BUCKET=
VITE_FIREBASE_MESSAGING_SENDER_ID=
VITE_FIREBASE_APP_ID=
```

---

## Qdrant

La collection Qdrant utilisée est :

```txt
softged_documents
```

Configuration recommandée :

```txt
dimension: 1536
distance: cosine
```

Cette dimension correspond au modèle d’embedding utilisé :

```txt
text-embedding-3-small
```

Les champs utilisés pour le filtrage doivent être indexés dans Qdrant.

Payload indexes recommandés :

```txt
ownerUid      keyword
projectId     keyword
documentId    keyword
pageId        keyword
pageNumber    keyword
sourceType    keyword
```

Pour éviter les erreurs de typage dans les filtres Spring AI, les identifiants sont stockés comme chaînes dans le payload Qdrant.

---

## Endpoints principaux

### Auth

```http
GET /api/me
```

Retourne l’utilisateur Firebase courant.

---

### Projets

```http
GET    /api/projects
POST   /api/projects
GET    /api/projects/{projectId}
PUT    /api/projects/{projectId}
DELETE /api/projects/{projectId}
```

---

### Documents

```http
GET    /api/projects/{projectId}/documents
POST   /api/projects/{projectId}/documents
GET    /api/projects/{projectId}/documents/{documentId}
DELETE /api/projects/{projectId}/documents/{documentId}
```

---

### Traitement documentaire

Endpoint consolidé :

```http
POST /api/documents/{documentId}/processing
```

Il lance :

```txt
extraction
→ résumé visuel
→ indexation Qdrant
```

---

### RAG

```http
POST /api/projects/{projectId}/rag
```

Exemple de body :

```json
{
  "question": "Que montre ce document ?"
}
```

Exemple de réponse :

```json
{
  "answer": "Le document montre un plan de remodelage de salle de bain...",
  "sources": [
    {
      "documentId": "1",
      "originalFilename": "interior_model.pdf",
      "pageId": "1",
      "pageNumber": "1",
      "imagePath": "./storage/pages/user/document/page-1.png"
    }
  ]
}
```

---

## Gestion des erreurs

Le backend utilise `ProblemDetail`, l’API native Spring pour représenter des erreurs HTTP standardisées.

Exemple :

```json
{
  "type": "https://softged.mediarium.local/problems/resource_not_found",
  "title": "Not Found",
  "status": 404,
  "detail": "Project not found",
  "instance": "/api/projects/42",
  "code": "RESOURCE_NOT_FOUND"
}
```

---

## Limites actuelles

Le projet est un POC.

Les limites connues sont :

* traitement synchrone des documents ;
* stockage local des fichiers ;
* pas encore de queue ou worker asynchrone ;
* granularité RAG au niveau page ;
* pas encore de découpage en zones ou crops ;
* pas encore d’affichage visuel de la page source dans le chat ;
* pas encore de score minimum exposé côté frontend ;
* pas encore de gestion avancée des versions documentaires ;
* pas encore de droits collaboratifs par projet.

---

## Améliorations prévues

Prochaines évolutions possibles :

* afficher l’image PNG source dans le frontend ;
* ajouter un viewer documentaire ;
* créer un endpoint backend pour servir les images de pages ;
* ajouter un mode RAG limité à un document ;
* ajouter un score minimum de similarité ;
* afficher les scores des sources ;
* découper les grandes pages en zones visuelles ;
* générer des crops de plans ;
* ajouter des bounding boxes pour surligner les zones utilisées ;
* rendre le pipeline asynchrone ;
* ajouter une file de traitement ;
* gérer les versions de documents ;
* ajouter des permissions par projet.

---

## Note sur l’IA Driven Development

Ce projet a aussi servi à illustrer un point important : l’IA peut produire rapidement du code fonctionnel, mais pas toujours architecturalement sain.

Pendant la construction du backend, plusieurs choix ont été corrigés manuellement :

* éviter que les services métier retournent des DTO ;
* éviter que les services métier dépendent directement des mappers MyBatis ;
* isoler la persistance dans des DataServices ;
* garder les business models indépendants du contrat REST ;
* distinguer MySQL comme source métier et Qdrant comme index de recherche.

Le projet illustre donc une approche d’IA Driven Development où l’IA accélère la production, mais où le développeur garde la responsabilité des frontières architecturales et de la maintenabilité.

---

## Statut du projet

MVP backend et frontend en cours.

Fonctionnel actuellement :

```txt
Login Firebase
→ création projet
→ upload PDF
→ traitement documentaire complet
→ indexation Qdrant
→ question RAG
→ réponse sourcée
```

Le prochain objectif est d’améliorer l’expérience frontend avec un affichage visuel des sources et un viewer de pages PDF rendues en PNG.
