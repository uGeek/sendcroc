#!/usr/bin/env bash

#===========#
#  sendcroc #
#===========#
#
# DESCRIPCIÓN:
# Un wrapper robusto para 'croc' que aísla los comandos globales en un
# subshell para evitar la contaminación del entorno por variables como CROC_RELAY.
#
# AUTOR: Basado en el script original de uGeek.
# VERSIÓN: 5.4.3 (Mejora la sección de ayuda con ejemplos de cron)
# FECHA: 18/07/2025

# --- Variables Globales y de Configuración ---
VERSION="5.4.3 (18/07/2025)"
CONFIG_DIR="${HOME}/.config/sendcroc"
CONFIG_FILE="${CONFIG_DIR}/sendcroc.conf"
INSTALL_PATH="/usr/bin/sc"

export $(cat ~/.config/sendcroc/sendcroc.conf | grep "CROC_SECRET=")
# --- Funciones ---

show_help() {
    cat << EOF

sendcroc (sc) - v${VERSION}
Wrapper para 'croc' con la sintaxis correcta y verificada para máxima fiabilidad.

SINOPSIS
    sc [COMANDO] [ARGUMENTO]

COMANDOS PRINCIPALES
    sc [archivo/dir]            Envía un archivo/dir con tu relay privado.
    sc rc                       Prepara la recepción con tu relay privado.
    
    sc g [archivo/dir]          Envía usando el relay global (público).
    sc rcg                      Recibe usando el relay global.
    
    sc t                        Envía un texto con tu relay privado.
    sc gt                       Envía un texto con el relay global.

CONFIGURACIÓN Y CRON
    dotfile                     Muestra el contenido del archivo de configuración.
    edotfile                    Abre y edita el archivo de configuración.
    sccron [ruta]               Comando para ENVIAR vía cron.
    rccron                      Comando para RECIBIR vía cron.

EJEMPLOS

    Uso interactivo:
    ----------------
    # Máquina A (Emisor):
    sc mi_directorio/

    # Máquina B (Receptor):
    sc rc


    Uso con Cron (crontab -e):
    --------------------------
    Recuerda usar siempre la ruta absoluta al script (${INSTALL_PATH}).

    # Emisor (a las 2:00 AM):
    0 2 * * * ${INSTALL_PATH} sccron /home/user/backups/

    # Receptor (a las 2:01 AM):
    1 2 * * * cd /home/user/restores/ && ${INSTALL_PATH} rccron

    Actualiza croc con:
    --------------------------
    curl https://getcroc.schollz.com | bash

    ------------------------------ 
        sendcroc (sc) - v${VERSION}
        $(croc --version)                                                                                                                                           
    ------------------------------  

EOF
}

check_dependencies() {
    if ! command -v croc &> /dev/null; then
        echo "Error: 'croc' no está instalado o no se encuentra en el PATH." >&2
        echo "Ejecuta: curl https://getcroc.schollz.com | bash" >&2
        exit 1
    fi
}

initial_setup() {
    echo "--- Configuración Inicial de sendcroc ---"
    
    read -p "Introduce tu secreto personal (CROC_SECRET): " user_secret
    read -p "Introduce tus opciones de relay (ej: --pass 'pw' --relay 'host:port'): " user_relay
    read -p "Introduce un secreto para transferencias globales (CROC_SECRET_GLOBAL): " user_secret_global
    
    {
        echo "# Archivo de configuración para sendcroc"
        echo "CROC_SECRET='${user_secret}'"
        echo "# Las opciones de Relay deben ser un array de Bash."
        echo "RELAY=(${user_relay})"
        echo "CROC_SECRET_GLOBAL='${user_secret_global:-public-croc-code}'"
    } > "$CONFIG_FILE"
    
    echo -e "\n¡Configuración guardada en ${CONFIG_FILE}! El script ya está listo para usarse."
    exit 0
}

# --- Lógica Principal ---

check_dependencies

mkdir -p "$CONFIG_DIR"
if [ ! -f "$CONFIG_FILE" ]; then
    initial_setup
fi
source "$CONFIG_FILE"

if ! [[ "$(declare -p RELAY 2>/dev/null)" =~ "declare -a" ]]; then
    if [ -n "$RELAY" ]; then
        RELAY_OPTS=($RELAY)
    fi
else
    RELAY_OPTS=("${RELAY[@]}")
fi

COMMAND="$1"

case "$COMMAND" in
    "" | "-h" | "h" | "--help")
        show_help
        ;;

    "rc")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        echo "Preparando para recibir..."
        CROC_SECRET="${CROC_SECRET}" croc --yes --overwrite "${RELAY_OPTS[@]}"
        ;;

    "rcg")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET_GLOBAL no está definido." >&2; exit 1; fi
        echo "Preparando para recibir (global)..."
        (
            unset CROC_RELAY CROC_RELAY6 CROC_PASS
            CROC_SECRET="${CROC_SECRET}" croc --yes --overwrite
        )
        ;;

    "t")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        read -p "Escribe el texto a enviar: " texto
        croc "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" --text "${texto}"
        ;;

    "gt")
        if [ -z "${CROC_SECRET_GLOBAL}" ]; then echo "Error: CROC_SECRET_GLOBAL no está definido." >&2; exit 1; fi
        read -p "Escribe el texto a enviar (global): " texto
        (
            unset CROC_RELAY CROC_RELAY6 CROC_PASS
            croc send --code "${CROC_SECRET_GLOBAL}" --text "${texto}"
        )
        ;;

    "g")
        if [ -z "${CROC_SECRET_GLOBAL}" ]; then echo "Error: CROC_SECRET_GLOBAL no está definido." >&2; exit 1; fi
        shift
        (
            unset CROC_RELAY CROC_RELAY6 CROC_PASS
            croc --yes send --code "${CROC_SECRET_GLOBAL}" "$@"
        )
        ;;

    "rccron")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        CROC_SECRET="${CROC_SECRET}" croc --yes --ignore-stdin --overwrite "${RELAY_OPTS[@]}"
        ;;

    "sccron")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        shift
        croc --ignore-stdin "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" "$@"
        ;;

    "dotfile")
        cat "${CONFIG_FILE}"
        ;;
        
    "edotfile")
        ${EDITOR:-nano} "$CONFIG_FILE"
        ;;
        
    *)
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        echo "Enviando: $@"
        croc "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" "$@"
        ;;
esac

exit 0
