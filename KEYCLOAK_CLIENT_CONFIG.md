# 🔧 Configuration Keycloak - Client Frontend

## 📋 Prérequis

- Keycloak 21+ en cours d'exécution sur `http://localhost:8080`
- Realm `afriland` créé
- Accès admin à Keycloak

---

## ✅ Étapes de configuration du Client Keycloak

### **1. Se connecter à Keycloak Admin**
1. Allez à `http://localhost:8080/admin`
2. Connectez-vous avec vos credentials admin
3. Sélectionnez le realm `afriland`

### **2. Créer un nouveau client**
1. Allez à **Clients** dans le menu latéral
2. Cliquez sur **Create**
3. Remplissez les informations:
   - **Client ID**: `control-plafond-frontend`
   - **Client Protocol**: `openid-connect`
   - Cliquez **Save**

### **3. Configurer le client**

#### **Settings**
- **Client Protocol**: `openid-connect`
- **Access Type**: `public` (important pour SPA)
- **Standard Flow Enabled**: ✅ ON
- **Implicit Flow Enabled**: ✅ ON
- **Direct Access Grants Enabled**: ✅ ON (pour tester avec Postman)
- **Valid Redirect URIs**: 
  ```
  http://localhost:4200/*
  http://localhost:4200
  ```
- **Web Origins**:
  ```
  http://localhost:4200
  ```

#### **Advanced Settings**
- **Access Token Lifespan**: `5 minutes` (ou selon vos besoins)
- **Refresh Token Lifespan**: `30 minutes`

### **4. Vérifier les rôles**

Allez à **Roles** et vérifiez/créez les rôles nécessaires:
- `admin` - Pour les administrateurs
- `agent` - Pour les agents
- (Adaptez selon vos besoins)

### **5. Assigner les rôles aux utilisateurs**

1. Allez à **Users**
2. Sélectionnez un utilisateur
3. Allez à **Role Mappings**
4. Assignez les rôles `Realm Roles`

---

## 🔑 Test du Client

### Via Postman

#### **Obtenir un token:**
```
POST http://localhost:8080/realms/afriland/protocol/openid-connect/token

Content-Type: application/x-www-form-urlencoded

Body:
  client_id=control-plafond-frontend
  username=your_username
  password=your_password
  grant_type=password
```

**Réponse:**
```json
{
  "access_token": "eyJhbGc...",
  "refresh_token": "eyJhbGc...",
  "expires_in": 300,
  "token_type": "Bearer"
}
```

#### **Appeler le backend avec le token:**
```
GET http://localhost:8081/api/v1/agents

Header:
  Authorization: Bearer eyJhbGc...
```

---

## 🔐 Configuration Keycloak pour le Backend

Votre backend est déjà configuré pour accepter les tokens Keycloak:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/afriland
```

Cela signifie que le backend va automatiquement valider les tokens JWT émis par ce Keycloak realm.

---

## 📱 Test complet Frontend → Backend

### **Étapes:**

1. **Démarrer Keycloak**
   ```bash
   cd c:\Users\user\Desktop\first tranfer\controle plafond\keycloak
   # Lancer Keycloak (détails selon votre configuration Docker/native)
   ```

2. **Démarrer le Backend**
   ```bash
   cd c:\Users\user\Desktop\first tranfer\controle plafond
   mvn clean spring-boot:run
   ```

3. **Démarrer le Frontend**
   ```bash
   cd c:\Users\user\Desktop\first tranfer\control-plafond-frontend
   npm install
   npm start
   ```

4. **Tester le flux complet:**
   - Naviguer à `http://localhost:4200`
   - Vous êtes redirigé vers `http://localhost:8080/realms/afriland/protocol/openid-connect/auth`
   - Connectez-vous
   - Vous êtes redirigé vers `http://localhost:4200`
   - Les appels API incluent automatiquement le token Keycloak
   - Le backend valide le token et retourne les données

---

## ⚠️ Erreurs courantes

### "Redirect URI mismatch"
- ✅ Vérifier que `http://localhost:4200/*` est dans **Valid Redirect URIs**
- ✅ Vérifier que le client Keycloak ID est `control-plafond-frontend`

### "Access Type: confidential"
- ❌ NE PAS utiliser "confidential" pour les SPAs
- ✅ Utiliser "public"

### "CORS error"
- ✅ Vérifier que `http://localhost:4200` est dans **Web Origins**
- ✅ Le backend CORS inclut `http://localhost:4200`

### "Invalid token"
- ✅ Vérifier que l'issuer URI du backend correspond à Keycloak
- ✅ Vérifier que le token n'est pas expiré

---

## 📚 Ressources

- [Keycloak Admin Console](http://localhost:8080/admin)
- [Keycloak Documentation](https://www.keycloak.org/docs/latest)
- [Keycloak Angular](https://www.keycloak.org/docs/latest/securing_apps/#_javascript_adapter)

