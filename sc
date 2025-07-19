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
# VERSIÓN: 6.0.0 (Soporte para --local, comando de actualización y alias)
# FECHA: 18/07/2025

# --- Variables Globales y de Configuración ---
VERSION="6.0.0 (18/07/2025)"
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
    sc r                        Prepara la recepción con tu relay privado.
    
    sc g [archivo/dir]          Envía usando el relay global (público).
    sc rg                       Recibe usando el relay global.
    
    sc t                        Envía un texto con tu relay privado.
    sc gt                       Envía un texto con el relay global.

TRANSFERENCIA EN RED LOCAL
    --local: Fuerza que la transferencia se realice únicamente a través de la red local.
    
    sc l [archivo/dir]          Envía un archivo/directorio localmente.
    sc lt                       Envía un texto localmente.

CONFIGURACIÓN Y CRON
    dotfile                     Muestra el contenido del archivo de configuración.
    edotfile                    Abre y edita el archivo de configuración.
    sccron | scron [ruta]       Comando para ENVIAR vía cron.
    rccron | rcron              Comando para RECIBIR vía cron.
    lscron [ruta]               Comando para ENVIAR vía cron (solo local).
    lrcron                      Comando para RECIBIR vía cron (solo local).

INSTALACIÓN Y ACTUALIZACIÓN
    sc u                        Actualiza 'croc' e instala/actualiza el script 'sc'.

EJEMPLOS

    Uso interactivo:
    ----------------
    # Máquina A (Emisor):
    sc mi_directorio/

    # Máquina B (Receptor):
    sc r


    Uso con Cron (crontab -e):
    --------------------------
    Recuerda usar siempre la ruta absoluta al script (${INSTALL_PATH}).

    # Emisor (a las 2:00 AM con relay privado):
    0 2 * * * ${INSTALL_PATH} scron /home/user/backups/

    # Receptor (a las 2:01 AM con relay privado):
    1 2 * * * cd /home/user/restores/ && ${INSTALL_PATH} rcron

    ------------------------------ 
        sendcroc (sc) - v${VERSION}
        $(croc --version)                                                                                                                                           
    ------------------------------  

EOF
}

check_dependencies() {
    if ! command -v croc &> /dev/null; then
        echo "Error: 'croc' no está instalado o no se encuentra en el PATH." >&2
        echo "Puedes instalarlo ejecutando: sc u" >&2
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

COMMAND="$1"

# Comandos que no requieren dependencias ni configuración
case "$COMMAND" in
    "u")
        echo "--- Actualizando croc y el script sendcroc (sc) ---"
        echo "[1/2] Actualizando croc..."
        curl https://getcroc.schollz.com | bash
        echo "[2/2] Descargando y actualizando 'sc' en ${INSTALL_PATH}..."
        sudo curl -L https://raw.githubusercontent.com/uGeek/sendcroc/main/sc -o ${INSTALL_PATH} && sudo chmod +x ${INSTALL_PATH}
        echo "--- ¡Actualización completada! ---"
        exit 0
        ;;
esac

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


case "$COMMAND" in
    "" | "-h" | "h" | "--help")
        show_help
        ;;

    "u") # Gestionado arriba para no requerir dependencias, pero lo dejamos por si acaso.
        exit 0
        ;;

    "r") # ANTES "rc"
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        echo "Preparando para recibir..."
        CROC_SECRET="${CROC_SECRET}" croc --yes --overwrite "${RELAY_OPTS[@]}"
        ;;

    "rg") # ANTES "rcg"
        if [ -z "${CROC_SECRET_GLOBAL}" ]; then echo "Error: CROC_SECRET_GLOBAL no está definido." >&2; exit 1; fi
        echo "Preparando para recibir (global)..."
        (
            unset CROC_RELAY CROC_RELAY6 CROC_PASS
            CROC_SECRET="${CROC_SECRET_GLOBAL}" croc --yes --overwrite
        )
        ;;

    "t")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        read -p "Escribe el texto a enviar: " texto
        croc "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" --text "${texto}"
        ;;
    
    "lt") # NUEVO: Enviar texto localmente
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        read -p "Escribe el texto a enviar (local): " texto
        croc --local "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" --text "${texto}"
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

    "rccron" | "rcron") # AÑADIDO ALIAS "rcron"
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        CROC_SECRET="${CROC_SECRET}" croc --yes --ignore-stdin --overwrite "${RELAY_OPTS[@]}"
        ;;
    
    "lrcron") # NUEVO: Recibir vía cron localmente
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        CROC_SECRET="${CROC_SECRET}" croc --local --yes --ignore-stdin --overwrite "${RELAY_OPTS[@]}"
        ;;

    "sccron" | "scron") # AÑADIDO ALIAS "scron"
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        shift
        croc --ignore-stdin "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" "$@"
        ;;
    
    "lscron") # NUEVO: Enviar vía cron localmente
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        shift
        croc --local --ignore-stdin "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" "$@"
        ;;

    "dotfile")
        cat "${CONFIG_FILE}"
        ;;
        
    "edotfile")
        ${EDITOR:-nano} "$CONFIG_FILE"
        ;;
    
    "l") # NUEVO: Enviar localmente
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        shift
        echo "Enviando localmente: $@"
        croc --local "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" "$@"
        ;;
        
    *)
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no está definido." >&2; exit 1; fi
        echo "Enviando: $@"
        croc "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" "$@"
        ;;
esac

exit 0
