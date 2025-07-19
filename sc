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
# VERSIÓN: 6.2.0 (Añade la opción --dry-run para previsualizar comandos)
# FECHA: 18/07/2025

# --- Variables Globales y de Configuración ---
VERSION="6.2.0 (18/07/2025)"
CONFIG_DIR="${HOME}/.config/sendcroc"
INSTALL_PATH="/usr/bin/sc"
DRY_RUN_ENABLED=0

# --- Funciones ---

show_help() {
    cat << EOF

sendcroc (sc) - v${VERSION}
Wrapper para 'croc' con la sintaxis correcta y verificada para máxima fiabilidad.

SINOPSIS
    sc [COMANDO] [ARGUMENTO] [--conf RUTA_CONFIG] [--dry-run]

COMANDOS PRINCIPALES
    sc [archivo/dir]            Envía un archivo/dir con tu relay privado.
    sc r                        Prepara la recepción con tu relay privado.
    # ... (resto de comandos)

OPCIONES AVANZADAS
    --conf [ruta]               Usa un archivo de configuración alternativo.
    --dry-run                   Muestra el comando que se ejecutaría sin ejecutarlo.

EJEMPLOS

    # Previsualizar el comando para enviar un archivo:
    sc mi_archivo.zip --dry-run

    # Resultado esperado:
    # DRY-RUN: croc '--ignore-stdin' 'send' '--code' 'secreto' 'mi_archivo.zip'

    # Enviar un directorio usando una configuración específica:
    sc mi_directorio/ --conf /home/user/configs/trabajo.conf

    ------------------------------ 
        sendcroc (sc) - v${VERSION}
        $(croc --version)                                                                                                                                           
    ------------------------------  

EOF
}

execute_croc() {
    if [ "$DRY_RUN_ENABLED" -eq 1 ]; then
        printf "DRY-RUN: croc"
        # Usamos printf con %q para citar correctamente cada argumento
        for arg in "$@"; do
            printf " %q" "$arg"
        done
        printf "\n"
        # Nota: Las variables de entorno como CROC_SECRET se definen para el comando
        # pero no se muestran explícitamente en esta salida.
    else
        # Usamos 'command' para evitar recursión si hubiera un alias llamado 'croc'
        command croc "$@"
    fi
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
        echo "RELAY=(${user_relay})"
        echo "CROC_SECRET_GLOBAL='${user_secret_global:-public-croc-code}'"
    } > "$CONFIG_FILE"
    
    echo -e "\n¡Configuración guardada en ${CONFIG_FILE}! El script ya está listo para usarse."
    exit 0
}

# --- Lógica Principal ---

# 1. Analizar argumentos para --conf y --dry-run
ARGS=()
CUSTOM_CONFIG_FILE=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --conf)
            if [ -z "$2" ]; then echo "Error: --conf requiere una ruta." >&2; exit 1; fi
            CUSTOM_CONFIG_FILE="$2"
            shift 2
            ;;
        --dry-run)
            DRY_RUN_ENABLED=1
            shift
            ;;
        *)
            ARGS+=("$1")
            shift
            ;;
    esac
done
set -- "${ARGS[@]}" # Restaurar argumentos posicionales filtrados

# 2. Determinar la ruta del archivo de configuración
CONFIG_FILE="${HOME}/.config/sendcroc/sendcroc.conf" # Por defecto
if [ -n "$CUSTOM_CONFIG_FILE" ]; then
    CONFIG_FILE="$CUSTOM_CONFIG_FILE"
    if [ ! -f "$CONFIG_FILE" ]; then
        echo "Error: El archivo de configuración no existe: $CONFIG_FILE" >&2
        exit 1
    fi
fi

# 3. Extraer comando principal
COMMAND="$1"

# Comandos que no requieren dependencias
case "$COMMAND" in
    "u")
        echo "--- Actualizando croc y el script sendcroc (sc) ---"
        # Esta acción no es un "dry-run", se ejecuta directamente.
        curl https://getcroc.schollz.com | bash
        sudo curl -L https://raw.githubusercontent.com/uGeek/sendcroc/main/sc -o ${INSTALL_PATH} && sudo chmod +x ${INSTALL_PATH}
        echo "--- ¡Actualización completada! ---"
        exit 0
        ;;
esac

# 4. Lógica principal con dependencias y configuración
check_dependencies

mkdir -p "$CONFIG_DIR"
if [ ! -f "$CONFIG_FILE" ]; then
    initial_setup
fi
source "$CONFIG_FILE"

if ! [[ "$(declare -p RELAY 2>/dev/null)" =~ "declare -a" ]]; then
    if [ -n "$RELAY" ]; then RELAY_OPTS=($RELAY); fi
else
    RELAY_OPTS=("${RELAY[@]}")
fi

case "$COMMAND" in
    "" | "-h" | "h" | "--help")
        show_help
        ;;

    "u")
        exit 0
        ;;

    "r")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no definido en $CONFIG_FILE." >&2; exit 1; fi
        echo "Preparando para recibir..."
        CROC_SECRET="${CROC_SECRET}" execute_croc --yes --overwrite "${RELAY_OPTS[@]}"
        ;;

    "rg")
        if [ -z "${CROC_SECRET_GLOBAL}" ]; then echo "Error: CROC_SECRET_GLOBAL no definido en $CONFIG_FILE." >&2; exit 1; fi
        echo "Preparando para recibir (global)..."
        (
            unset CROC_RELAY CROC_RELAY6 CROC_PASS
            CROC_SECRET="${CROC_SECRET_GLOBAL}" execute_croc --yes --overwrite
        )
        ;;

    "t")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no definido." >&2; exit 1; fi
        read -p "Escribe el texto a enviar: " texto
        execute_croc "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" --text "${texto}"
        ;;
    
    "lt")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no definido." >&2; exit 1; fi
        read -p "Escribe el texto a enviar (local): " texto
        execute_croc --local "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" --text "${texto}"
        ;;

    "gt")
        if [ -z "${CROC_SECRET_GLOBAL}" ]; then echo "Error: CROC_SECRET_GLOBAL no definido." >&2; exit 1; fi
        read -p "Escribe el texto a enviar (global): " texto
        (
            unset CROC_RELAY CROC_RELAY6 CROC_PASS
            execute_croc send --code "${CROC_SECRET_GLOBAL}" --text "${texto}"
        )
        ;;

    "g")
        if [ -z "${CROC_SECRET_GLOBAL}" ]; then echo "Error: CROC_SECRET_GLOBAL no definido." >&2; exit 1; fi
        shift
        (
            unset CROC_RELAY CROC_RELAY6 CROC_PASS
            execute_croc --yes send --code "${CROC_SECRET_GLOBAL}" "$@"
        )
        ;;

    "rccron" | "rcron")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no definido." >&2; exit 1; fi
        CROC_SECRET="${CROC_SECRET}" execute_croc --yes --ignore-stdin --overwrite "${RELAY_OPTS[@]}"
        ;;
    
    "lrcron")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no definido." >&2; exit 1; fi
        CROC_SECRET="${CROC_SECRET}" execute_croc --local --yes --ignore-stdin --overwrite "${RELAY_OPTS[@]}"
        ;;

    "sccron" | "scron")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no definido." >&2; exit 1; fi
        shift
        execute_croc --ignore-stdin "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" "$@"
        ;;
    
    "lscron")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no definido." >&2; exit 1; fi
        shift
        execute_croc --local --ignore-stdin "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" "$@"
        ;;

    "dotfile")
        cat "${CONFIG_FILE}"
        ;;
        
    "edotfile")
        ${EDITOR:-nano} "$CONFIG_FILE"
        ;;
    
    "l")
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no definido." >&2; exit 1; fi
        shift
        echo "Enviando localmente: $@"
        execute_croc --local "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" "$@"
        ;;
        
    *)
        if [ -z "${CROC_SECRET}" ]; then echo "Error: CROC_SECRET no definido." >&2; exit 1; fi
        echo "Enviando: $@"
        execute_croc "${RELAY_OPTS[@]}" send --code "${CROC_SECRET}" "$@"
        ;;
esac

exit 0
