#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCANNER_VERSION="8.1.0.6389"
SCANNER_HOME="${ROOT_DIR}/.tools/sonar-scanner-${SCANNER_VERSION}"

if [[ -z "${SONAR_TOKEN:-}" ]]; then
  echo "SONAR_TOKEN is required. Set it in the shell before running this script."
  exit 1
fi

if command -v sonar-scanner >/dev/null 2>&1; then
  SCANNER="$(command -v sonar-scanner)"
else
  mkdir -p "${ROOT_DIR}/.tools"
  if [[ ! -x "${SCANNER_HOME}/bin/sonar-scanner" ]]; then
    ARCHIVE="${ROOT_DIR}/.tools/sonar-scanner-${SCANNER_VERSION}.zip"
    curl --fail --location --silent --show-error \
      "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SCANNER_VERSION}-linux-x64.zip" \
      --output "${ARCHIVE}"
    unzip -q -o "${ARCHIVE}" -d "${ROOT_DIR}/.tools"
    mv "${ROOT_DIR}/.tools/sonar-scanner-${SCANNER_VERSION}-linux-x64" "${SCANNER_HOME}"
  fi
  SCANNER="${SCANNER_HOME}/bin/sonar-scanner"
fi

cd "${ROOT_DIR}"
./mvnw -f services/pom.xml verify
npm --workspace web run test -- --coverage
npm --workspace web run build
"${SCANNER}"
