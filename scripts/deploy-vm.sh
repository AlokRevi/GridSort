<<'EOF'
#!/usr/bin/env bash

set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/apps/GridSort}"
BRANCH="${BRANCH:-main}"
BACKEND_SERVICE="${BACKEND_SERVICE:-monthly-dashboard}"
WEB_ROOT="${WEB_ROOT:-/var/www/gridsort}"

BACKEND_DIR="$APP_DIR/backend-java"
FRONTEND_DIR="$APP_DIR/frontend-angular"

log() {
  echo
  echo "==> $1"
}

log "Moving to app directory"
cd "$APP_DIR"

log "Syncing latest code from GitHub"
git fetch origin "$BRANCH"
git reset --hard "origin/$BRANCH"

log "Building backend"
cd "$BACKEND_DIR"

if [[ -x "./mvnw" ]]; then
  ./mvnw -DskipTests clean package
elif command -v mvn >/dev/null 2>&1; then
  mvn -DskipTests clean package
else
  echo "ERROR: Maven is not installed and ./mvnw was not found."
  exit 1
fi

JAR_FILE="$(find target -maxdepth 1 -type f -name "*.jar" ! -name "*sources*" ! -name "*javadoc*" ! -name "*original*" | head -n 1)"

if [[ -z "$JAR_FILE" ]]; then
  echo "ERROR: No backend JAR file found in target/"
  exit 1
fi

cp "$JAR_FILE" "$BACKEND_DIR/app.jar"

log "Backend JAR prepared at $BACKEND_DIR/app.jar"

log "Building frontend"
cd "$FRONTEND_DIR"

if [[ -f package-lock.json ]]; then
  npm ci
else
  npm install
fi

npm run build

FRONTEND_INDEX="$(find "$FRONTEND_DIR/dist" -type f -name "index.html" -print -quit)"

if [[ -z "$FRONTEND_INDEX" ]]; then
  echo "ERROR: Frontend build output not found."
  exit 1
fi

FRONTEND_OUTPUT="$(dirname "$FRONTEND_INDEX")"

log "Copying frontend build to $WEB_ROOT"
sudo mkdir -p "$WEB_ROOT"
sudo rsync -a --delete "$FRONTEND_OUTPUT/" "$WEB_ROOT/"

log "Restarting backend service if it exists"

if systemctl list-unit-files | grep -q "^${BACKEND_SERVICE}.service"; then
  sudo systemctl restart "${BACKEND_SERVICE}.service"
  sudo systemctl status "${BACKEND_SERVICE}.service" --no-pager -l || true
else
  echo "Skipped: ${BACKEND_SERVICE}.service does not exist yet."
  echo "Later we will create /etc/systemd/system/${BACKEND_SERVICE}.service"
fi

log "Reloading Nginx if installed"

if command -v nginx >/dev/null 2>&1; then
  sudo nginx -t
  sudo systemctl reload nginx
else
  echo "Skipped: Nginx is not installed."
fi

log "Deployment complete"
EOF

