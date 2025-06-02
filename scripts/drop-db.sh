#!/usr/bin/env bash
HOST="127.0.0.1"
PORT="3306"
PASWD="abc123"
DB="jp-file-transfer"

mariadb -h "$HOST" -P "$PORT" -u jp -p"$PASWD" --skip-ssl -e "DROP DATABASE IF EXISTS \`$DB\`; CREATE DATABASE \`$DB\`;"
notify-send "Dropped db" "Dropped db $DB succesfully"
