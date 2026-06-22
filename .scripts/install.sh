#!/usr/bin/env sh
set -eu

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)

DEPLOY_ROOT=${DEPLOY_ROOT:-"$HOME/.generator"}
BIN_ROOT=${BIN_ROOT:-"$HOME/.local/bin"}

ARTIFACT_ID=$(xmllint --xpath "string(/*[local-name()='project']/*[local-name()='artifactId'])" "$PROJECT_ROOT/pom.xml")
GENERATOR_NAME=${ARTIFACT_ID#generator-}

LATEST_TAG=$(git -C "$PROJECT_ROOT" tag --sort=-v:refname | head -n 1)

if [ -z "$LATEST_TAG" ]; then
    echo "No Git tag found. Unable to install latest version." >&2
    exit 1
fi

CURRENT_BRANCH=$(git -C "$PROJECT_ROOT" symbolic-ref --quiet --short HEAD || true)
CURRENT_COMMIT=$(git -C "$PROJECT_ROOT" rev-parse HEAD)

cleanup() {
    if [ -n "$CURRENT_BRANCH" ]; then
        git -C "$PROJECT_ROOT" checkout "$CURRENT_BRANCH" >/dev/null 2>&1 || true
    else
        git -C "$PROJECT_ROOT" checkout "$CURRENT_COMMIT" >/dev/null 2>&1 || true
    fi
}

trap cleanup EXIT INT TERM

echo "Installing $ARTIFACT_ID from latest tag: $LATEST_TAG"

git -C "$PROJECT_ROOT" checkout "$LATEST_TAG" >/dev/null 2>&1

make -C "$PROJECT_ROOT" build
"$PROJECT_ROOT/.scripts/deploy.sh"

mkdir -p "$DEPLOY_ROOT"

cat > "$DEPLOY_ROOT/$GENERATOR_NAME" <<EOF
#!/usr/bin/env sh
exec java -jar "$DEPLOY_ROOT/$GENERATOR_NAME.jar" "\$@"
EOF

chmod +x "$DEPLOY_ROOT/$GENERATOR_NAME"

mkdir -p "$BIN_ROOT"

ln -sf \
    "$DEPLOY_ROOT/$GENERATOR_NAME" \
    "$BIN_ROOT/$GENERATOR_NAME"

echo "Installed jar:      $DEPLOY_ROOT/$GENERATOR_NAME.jar"
echo "Installed launcher: $DEPLOY_ROOT/$GENERATOR_NAME"
echo "Installed command:  $BIN_ROOT/$GENERATOR_NAME"

case ":$PATH:" in
    *":$BIN_ROOT:"*)
        echo "PATH already contains $BIN_ROOT"
        ;;
    *)
        echo
        echo "WARNING: $BIN_ROOT is not present in PATH."
        echo
        echo "Add the following line to your shell configuration:"
        echo
        echo "export PATH=\"$BIN_ROOT:\$PATH\""
        echo
        echo "Then reload your shell."
        ;;
esac

echo
echo "Run with:"
echo "  $GENERATOR_NAME --repo . --from v1.0.0 --to v1.1.0 --output CHANGELOG.md"
