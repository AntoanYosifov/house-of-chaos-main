#!/bin/bash
  set -e

  echo "Fetching secrets from SSM..."

  DB_HOST=$(aws ssm get-parameter \
    --name "/hoc/prod/db-host" \
    --query "Parameter.Value" \
    --output text \
    --region eu-central-1)

  DB_USERNAME=$(aws ssm get-parameter \
    --name "/hoc/prod/db-username" \
    --with-decryption \
    --query "Parameter.Value" \
    --output text \
    --region eu-central-1)

  DB_PASSWORD=$(aws ssm get-parameter \
    --name "/hoc/prod/db-password" \
    --with-decryption \
    --query "Parameter.Value" \
    --output text \
    --region eu-central-1)

  JWT_SECRET=$(aws ssm get-parameter \
    --name "/hoc/prod/jwt-secret" \
    --with-decryption \
    --query "Parameter.Value" \
    --output text \
    --region eu-central-1)

  CLOUDINARY_URL=$(aws ssm get-parameter \
    --name "/hoc/prod/cloudinary-url" \
    --with-decryption \
    --query "Parameter.Value" \
    --output text \
    --region eu-central-1)

  ADMIN_EMAIL=$(aws ssm get-parameter \
    --name "/hoc/prod/admin-email" \
    --query "Parameter.Value" \
    --output text \
    --region eu-central-1)

  ADMIN_PASSWORD=$(aws ssm get-parameter \
    --name "/hoc/prod/admin-password" \
    --with-decryption \
    --query "Parameter.Value" \
    --output text \
    --region eu-central-1)

  GHCR_PAT=$(aws ssm get-parameter \
      --name "/hoc/prod/ghcr-pat" \
      --with-decryption \
      --query "Parameter.Value" \
      --output text \
      --region eu-central-1)

  echo "Logging in to GHCR..."
  echo "$GHCR_PAT" | docker login ghcr.io -u AntoanYosifov --password-stdin

  echo "Stopping existing container..."
  docker stop hoc-api 2>/dev/null || true
  docker rm hoc-api 2>/dev/null || true

  echo "Pulling latest image..."
  docker pull ghcr.io/antoanyosifov/house-of-chaos-main:latest

  echo "Starting container..."
  docker run -d \
    --name hoc-api \
    --restart unless-stopped \
    -p 8080:8080 \
    -e DB_HOST="$DB_HOST" \
    -e DB_USERNAME="$DB_USERNAME" \
    -e DB_PASSWORD="$DB_PASSWORD" \
    -e JWT_SECRET="$JWT_SECRET" \
    -e CLOUDINARY_URL="$CLOUDINARY_URL" \
    -e ADMIN_EMAIL="$ADMIN_EMAIL" \
    -e ADMIN_PASSWORD="$ADMIN_PASSWORD" \
    -e SPRING_PROFILES_ACTIVE=prod \
    ghcr.io/antoanyosifov/house-of-chaos-main:latest

  echo "Waiting for application to become healthy..."
  for i in $(seq 1 24); do
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
      echo "Health check passed."
      exit 0
    fi
    echo "Attempt $i/24 - not ready yet, waiting 5s..."
    sleep 5
  done

  echo "Health check failed after 2 minutes."
  exit 1