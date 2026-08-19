#!/bin/bash
# Backup diario de la base de FrezCo. Retencion: 7 dias.
# La restauracion esta documentada en docs/08-guia-uso.md.
set -euo pipefail

# cron arranca sin las variables del contenedor: las deja el CMD del Dockerfile.
if [ -f /etc/backup.env ]; then
    set -a
    . /etc/backup.env
    set +a
fi

FECHA=$(date +%Y%m%d_%H%M%S)
DESTINO="/backups"
DB="${DB_NAME:-frezco}"
PREFIJO="[FREZCO-BACKUP $FECHA]"

echo "$PREFIJO === Iniciando backup de $DB ==="

sqlcmd \
    -S "${DB_HOST:-sqlserver},${DB_PORT:-1433}" \
    -U "${DB_USER:-sa}" \
    -P "${DB_PASSWORD}" \
    -C \
    -Q "BACKUP DATABASE [$DB] TO DISK = N'${DESTINO}/${DB}_${FECHA}.bak' WITH NOFORMAT, INIT, NAME = '${DB}-diario', SKIP, NOREWIND, NOUNLOAD, STATS = 10"

echo "$PREFIJO Comprimiendo..."
gzip -f "${DESTINO}/${DB}_${FECHA}.bak"

echo "$PREFIJO Limpiando backups de mas de 7 dias..."
find "$DESTINO" -name "${DB}_*.bak.gz" -mtime +7 -delete

echo "$PREFIJO === Backup completo ==="
ls -lh "$DESTINO"
