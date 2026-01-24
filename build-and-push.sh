#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

VERSION=${1:-"latest"}
REGISTRY=${2:-"myregistry.azurecr.io"}
IMAGE_NAME="quora-app"

FULL_IMAGE_NAME="${REGISTRY}/${IMAGE_NAME}"
IMAGE_TAG="${FULL_IMAGE_NAME}:${VERSION}"
LATEST_TAG="${FULL_IMAGE_NAME}:latest"

echo -e "${BLUE}Building Docker image...${NC}"
echo "Image Tag: $IMAGE_TAG"

# Build image
if docker build -t $IMAGE_TAG -t $LATEST_TAG .; then
    echo -e "${GREEN}✓ Docker image built successfully${NC}"
    echo -e "Image Tag: ${BLUE}${IMAGE_TAG}${NC}"
else
    echo -e "${RED}✗ Failed to build Docker image${NC}"
    exit 1
fi

# Check if pushing to registry
if [ "$REGISTRY" != "myregistry.azurecr.io" ]; then
    echo -e "${BLUE}Ready to push to registry${NC}"
    read -p "Push to registry? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker push $IMAGE_TAG
        docker push $LATEST_TAG
        echo -e "${GREEN}✓ Pushed to registry${NC}"
    fi
fi