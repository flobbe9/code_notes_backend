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

### Service repositories
- frontend: <a href="https://github.com/flobbe9/code_notes_frontend" target="_blank">https://github.com/flobbe9/code_notes_frontend</a>
- gateway: <a href="https://github.com/flobbe9/code_notes_gateway" target="_blank">https://github.com/flobbe9/code_notes_gateway</a>