#!/bin/bash

# Verificar si hay cambios pendientes.
if [ -z "$(git status --porcelain)" ]; then
    echo "No hay cambios para commit. Saliendo..."
    exit 0
fi

TAG_MESSAGE=$2
TICKET_ID=${1:-"users"}

# Añadir todos los cambios al área de staging y hacer un commit.
echo "Añadiendo cambios y haciendo commit..."
git add .
if [ -z "$TAG_MESSAGE" ]; then
	git commit --allow-empty-message -m '' --no-verify
    TAG_MESSAGE="deploy to test environment"
else
	git commit -m "feat: $TAG_MESSAGE" --no-verify
fi

# Empujar cambios al repositorio remoto.
echo "Empujando cambios al repositorio remoto..."
git push

git fetch -p

# Obtener el último número de tag y prepararse para incrementarlo si es necesario.
LAST_TAG=$(git tag -l "snapshot-$TICKET_ID-*" | sort -Vr | head -n 1)
if [[ $LAST_TAG =~ ([0-9]+)$ ]]; then
    TAG_NUMBER=$((${BASH_REMATCH[1]} + 1))
else
    TAG_NUMBER=1
fi

# Incrementar el número del tag automáticamente si el tag ya existe.
TAG_EXISTS=1
while [ $TAG_EXISTS -ne 0 ]; do
    TAG="snapshot-$TICKET_ID-$(printf "%02d" $TAG_NUMBER)"
    if git rev-parse "$TAG" >/dev/null 2>&1; then
        TAG_NUMBER=$((TAG_NUMBER + 1))
    else
        TAG_EXISTS=0
    fi
done


echo "Trabajando en el último commit..."
git checkout HEAD

echo "Creando el tag $TAG..."
git tag -a "$TAG" -m "$TAG_MESSAGE"

echo "Empujando el tag al repositorio remoto..."
git push origin "$TAG"
