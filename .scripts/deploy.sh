#!/usr/bin/env sh
set -eu

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
DEPLOY_ROOT=${DEPLOY_ROOT:-"$HOME/.generator"}

ARTIFACT_ID=$(xmllint --xpath "string(/*[local-name()='project']/*[local-name()='artifactId'])" "$PROJECT_ROOT/pom.xml")
GENERATOR_NAME=${ARTIFACT_ID#generator-}
JAR_FILE=$(find "$PROJECT_ROOT/target" -maxdepth 1 -type f -name "$ARTIFACT_ID-*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "No jar found for artifact '$ARTIFACT_ID' in target/." >&2
    exit 1
fi

mkdir -p "$DEPLOY_ROOT"
cp "$JAR_FILE" "$DEPLOY_ROOT/$GENERATOR_NAME.jar"

echo "Deployed $DEPLOY_ROOT/$GENERATOR_NAME.jar"
