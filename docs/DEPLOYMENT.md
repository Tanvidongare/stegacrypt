# StegaCrypt Deployment Guide

This project deploys as two services:

- Backend: Spring Boot API, deploy to Render as a Docker web service.
- Frontend: Vite React app, deploy to Netlify.

## Backend on Render

Use the root-level `render.yaml` blueprint, or create a manual Web Service with these settings:

- Service type: Web Service
- Runtime: Docker
- Root directory: `backend`
- Dockerfile path: `backend/Dockerfile`
- Health check path: `/api/health`

Environment variable:

```text
FRONTEND_ORIGINS=http://localhost:3000,http://localhost:5173,https://*.netlify.app
```

After Render deploys, copy the backend service URL, for example:

```text
https://stegacrypt-backend.onrender.com
```

Check:

```text
https://stegacrypt-backend.onrender.com/api/health
```

## Frontend on Netlify

The root-level `netlify.toml` already sets:

```text
Base directory: frontend
Build command: npm run build
Publish directory: dist
```

Add this Netlify environment variable before deploying:

```text
VITE_API_BASE_URL=https://YOUR_RENDER_BACKEND_URL/api
```

Example:

```text
VITE_API_BASE_URL=https://stegacrypt-backend.onrender.com/api
```

## Final CORS Tightening

After Netlify gives you the final site URL, update Render's `FRONTEND_ORIGINS` to include it explicitly:

```text
FRONTEND_ORIGINS=http://localhost:3000,http://localhost:5173,https://YOUR_SITE_NAME.netlify.app
```

Redeploy the backend after changing that variable.
