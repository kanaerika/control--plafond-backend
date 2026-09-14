# Keycloak — service client for backend provisioning

The backend creates the Keycloak user + assigns the realm role + sends the
"set your password" e-mail whenever an admin creates a **partner** or an
**agent** (`KeycloakInvitationAdapter`). It authenticates to the Keycloak Admin
API with the **client-credentials** grant of a dedicated service client.

If that client is missing or its secret is not set, provisioning is now
**non-fatal**: the partner/agent is still saved, a `WARN` is logged, and the
admin can trigger **"Renvoyer l'invitation"** once this setup is done. Nothing is
left half-created.

## What the code expects

`afb-boot/src/main/resources/application.yml`:

```yaml
keycloak:
  admin:
    server-url: ${KEYCLOAK_ADMIN_SERVER_URL:http://localhost:8080}
    realm:      ${KEYCLOAK_ADMIN_REALM:afriland}
    client-id:  ${KEYCLOAK_ADMIN_CLIENT_ID:afriland-backend-admin}
    client-secret: ${KEYCLOAK_ADMIN_CLIENT_SECRET:}   # <-- never committed
    role-agent: ${KEYCLOAK_ADMIN_ROLE_AGENT:AGENT}
```

Set `KEYCLOAK_ADMIN_CLIENT_SECRET` as an environment variable (or in a local,
git-ignored `application-local.yml`) — never in a committed file.

## One-time Keycloak configuration

Realm **afriland** → **Clients** → **Create client**

| Field | Value |
| --- | --- |
| Client type | OpenID Connect |
| Client ID | `afriland-backend-admin` |
| Client authentication | **On** (confidential) |
| Authorization | Off |
| Standard flow | Off |
| Direct access grants | Off |
| **Service accounts roles** | **On** |

Save, then:

1. **Credentials** tab → copy **Client secret** → export it as
   `KEYCLOAK_ADMIN_CLIENT_SECRET` for the backend process.
2. **Service account roles** tab → **Assign role** → filter by clients →
   from **`realm-management`** assign:
   - `manage-users` — create users, set required actions, send action e-mails
   - `view-realm` (or `manage-realm`) — read realm roles to assign `ADMIN` / `AGENT`
   - `query-users`

   (Assigning `realm-admin` also works but is broader than needed.)

3. Realm **afriland** → **Roles** — make sure realm roles **`ADMIN`** and
   **`AGENT`** exist (they are in `keycloak/realm-export.json`).

4. Realm **afriland** → **Realm settings → Email** — configure SMTP so the
   "set your password" e-mail can actually be sent. Without SMTP the user is
   still created; the e-mail step is skipped silently and can be retried.

## Verify

```bash
# should return an access_token, not an error
curl -s -X POST \
  http://localhost:8080/realms/afriland/protocol/openid-connect/token \
  -d grant_type=client_credentials \
  -d client_id=afriland-backend-admin \
  -d client_secret="$KEYCLOAK_ADMIN_CLIENT_SECRET" | jq .access_token
```

Then create a partner from the admin UI — the backend log should show the
Keycloak user being created instead of the `Provisioning Keycloak indisponible`
warning.

## Adding the client to realm-export.json (optional)

For fresh imports you can pre-declare the client, but the **secret** must still
be supplied at runtime (Keycloak generates its own on import unless you pin one,
and a pinned secret in a committed file is exactly what we want to avoid):

```json
{
  "clientId": "afriland-backend-admin",
  "name": "AFB Transfert - Backend (service account)",
  "enabled": true,
  "publicClient": false,
  "serviceAccountsEnabled": true,
  "standardFlowEnabled": false,
  "directAccessGrantsEnabled": false,
  "protocol": "openid-connect"
}
```

Service-account role mappings (`manage-users`, …) still have to be assigned in
the admin console or via `kcadm.sh` after import.
