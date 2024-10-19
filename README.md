# Run
- `docker-compose -f 'docker-compose.dev.yml up -d --build` 


## Using Oauth2 locally
- create an .env.secrets file at the project root (same level as /src)
- use these keys:
```
OAUTH2_CLIENT_ID_GOOGLE=
OAUTH2_CLIENT_SECRET_GOOGLE=
OAUTH2_ISSUER_URI_GOOGLE=
OAUTH2_CLIENT_ID_GITHUB=
OAUTH2_CLIENT_SECRET_GITHUB=
OAUTH2_AUTHORIZATION_URI_GITHUB=
OAUTH2_TOKEN_URI_GITHUB=
OAUTH2_USER_INFO_URI_GITHUB=
OAUTH2_CLIENT_ID_AZURE=
OAUTH2_CLIENT_SECRET_AZURE=
OAUTH2_TENANT_ID_AZURE=
OAUTH2_ISSUER_URI_AZURE=
```

### Note
- values from .env.secrets wont override .env values