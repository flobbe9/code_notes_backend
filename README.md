# Code Notes backend
Backend service of <a href="https://code-notes.net" target="_blank">https://code-notes.net</a>.

## Using Oauth2 locally
- create an .env.local file at the project root with:
```
OAUTH2_CLIENT_ID_GOOGLE=<yourValue>
OAUTH2_CLIENT_SECRET_GOOGLE=<yourValue>

OAUTH2_CLIENT_ID_GITHUB=<yourValue>
OAUTH2_CLIENT_SECRET_GITHUB=<yourValue>

OAUTH2_CLIENT_ID_AZURE=<yourValue>
OAUTH2_CLIENT_SECRET_AZURE=<yourValue>
```

## Running with code-notes.net db
- follow the steps in [Using Oauth2 locally](#using-oauth2-locally)
- create a `docker-compose.dev.override.yml` file at the root of the project with this content:
```
services:
  backend:
    ports: 
      - 1111:1111
    environment:
      DB_HOST: <replaceMe>
      DB_USER: <replaceMe>
      DB_PASSWORD: <replaceMe>
      PORT: 1111
      GATEWAY_PORT: 1112
```
    - replace <replaceMe> with the code-notes.net secrets
    - ports are interchangable but don't forget to adjust them in code_notes_frontend too
- `docker compose -f 'docker-compose.dev.yml' -f 'docker-compose.dev.override.yml' up backend maildev -d`
- access the backend at `http://localhost:1111`

### Service repositories
- frontend: <a href="https://github.com/flobbe9/code_notes_frontend" target="_blank">https://github.com/flobbe9/code_notes_frontend</a>
- gateway: <a href="https://github.com/flobbe9/code_notes_gateway" target="_blank">https://github.com/flobbe9/code_notes_gateway</a>