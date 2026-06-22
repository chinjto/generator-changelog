#!/usr/bin/env sh
set -eu

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)

DEPLOY_ROOT=${DEPLOY_ROOT:-"$HOME/.generator"}
BIN_ROOT=${BIN_ROOT:-"$HOME/.local/bin"}

ARTIFACT_ID=$(xmllint --xpath "string(/*[local-name()='project']/*[local-name()='artifactId'])" "$PROJECT_ROOT/pom.xml")
GENERATOR_NAME=${ARTIFACT_ID#generator-}

if [ -z "${JAVA_HOME:-}" ]; then
    echo "JAVA_HOME is not configured." >&2
    echo "Run install through 'make install' so .env is loaded by Make." >&2
    exit 1
fi

if [ ! -x "$JAVA_HOME/bin/java" ]; then
    echo "JAVA_HOME does not point to a valid JDK/JRE: $JAVA_HOME" >&2
    echo "Expected executable: $JAVA_HOME/bin/java" >&2
    exit 1
fi

JAR_FILE=$(find "$PROJECT_ROOT/target" -maxdepth 1 -type f -name "$ARTIFACT_ID-*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "No jar found for artifact '$ARTIFACT_ID' in target/." >&2
    echo "Run 'make build' before install, or configure 'install: build' in the Makefile." >&2
    exit 1
fi

mkdir -p "$DEPLOY_ROOT"
mkdir -p "$BIN_ROOT"

cp "$JAR_FILE" "$DEPLOY_ROOT/$GENERATOR_NAME.jar"

cat > "$DEPLOY_ROOT/$GENERATOR_NAME" <<EOF
#!/usr/bin/env sh
set -eu

JAVA_HOME="$JAVA_HOME"

exec "\$JAVA_HOME/bin/java" -jar "$DEPLOY_ROOT/$GENERATOR_NAME.jar" "\$@"
EOF

chmod +x "$DEPLOY_ROOT/$GENERATOR_NAME"

ln -sf \
    "$DEPLOY_ROOT/$GENERATOR_NAME" \
    "$BIN_ROOT/$GENERATOR_NAME"

echo "Installed jar:      $DEPLOY_ROOT/$GENERATOR_NAME.jar"
echo "Installed launcher: $DEPLOY_ROOT/$GENERATOR_NAME"
echo "Installed command:  $BIN_ROOT/$GENERATOR_NAME"
echo "Using Java:         $JAVA_HOME/bin/java"

case ":$PATH:" in
    *":$BIN_ROOT:"*)
        echo "PATH already contains $BIN_ROOT"
        ;;
    *)
        echo
        echo "WARNING: $BIN_ROOT is not present in PATH."
        echo "Add this line to your shell configuration:"
        echo
        echo "export PATH=\"$BIN_ROOT:\$PATH\""
        ;;
esac

echo
echo "Run with:"
echo "  $GENERATOR_NAME --repo . --from v1.0.0 --to v1.1.0 --output CHANGELOG.md"
