#!/bin/bash
# E2E Test Runner for Sprint Mode
# Usage: ./run-tests.sh [options]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$(dirname "$SCRIPT_DIR")"
PROJECT_DIR="$(dirname "$FRONTEND_DIR")"

cd "$FRONTEND_DIR"

# Default values
HEADED=false
DEBUG=false
BROWSER="chromium"

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --headed)
      HEADED=true
      shift
      ;;
    --debug)
      DEBUG=true
      shift
      ;;
    --browser)
      BROWSER="$2"
      shift 2
      ;;
    --help)
      echo "Usage: $0 [--headed] [--debug] [--browser chromium|firefox|webkit]"
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

echo "=========================================="
echo "E2E Test Runner - Sprint Mode"
echo "=========================================="
echo "Frontend dir: $FRONTEND_DIR"
echo "Browser: $BROWSER"
echo "Headless: $([ "$HEADED" = false ] && echo 'yes' || echo 'no')"
echo "=========================================="

# Check if Playwright is installed
if ! npx playwright --version > /dev/null 2>&1; then
  echo "Installing Playwright..."
  npm install -D @playwright/test
  npx playwright install "$BROWSER"
fi

# Build the command
CMD="npx playwright test"

if [ "$HEADED" = true ]; then
  CMD="$CMD --headed"
fi

if [ "$DEBUG" = true ]; then
  CMD="$CMD --debug"
fi

echo "Running: $CMD"
eval "$CMD"

echo ""
echo "=========================================="
echo "Tests complete!"
echo "=========================================="
