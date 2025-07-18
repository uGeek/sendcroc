#!/usr/bin/env bash

#===========#
#  sendcroc #
#===========#
#
# DESCRIPCIÓN:
# Un wrapper robusto para 'croc' que incluye un instalador y usa la variable
# de entorno CROC_SECRET para un manejo seguro de los códigos.
#
# AUTOR: Basado en el script original de uGeek
# VERSIÓN: 2.3.0 
# FECHA: 18/07/2025

# --- Variables Globales y de Configuración ---
VERSION="2.3.0 (18/07/2025)"
CONFIG_DIR="${HOME}/.config/sendcroc"
CONFIG_FILE="${CONFIG_DIR}/sendcroc.conf"
INSTALL_PATH="/usr/local/bin/sc"

# --- Funciones ---

show_help() {
    cat << EOF

sendcroc (sc) - v${VERSION}
Wrapper autónomo para 'croc' que facilita la transferencia de archivos de forma segura.

SINOPSIS
    sc [COMANDO] [ARGUMENTO]

COMANDOS PRINCIPALES
    sc [archivo/dir]            Envía el archivo o directorio especificado.
    sc rc                       Prepara la recepción de un archivo.
    
    sc g [archivo/dir]          Envía usando la configuración global (pública).
    sc rcg                      Recibe usando la configuración global.
    
    sc t                        Inicia un prompt para enviar un texto simple.

CONFIGURACIÓN Y CRON
    dotfile                     Muestra el contenido del archivo de configuración.
    edotfile                    Abre y edita el archivo de configuración.
    sccron [ruta]               Comando para ENVIAR vía cron.
    rccron                      Comando para RECIBIR vía cron.

EJEMPLOS

    Uso interactivo:
    ----------------
    # Máquina A (Emisor): Enviar el directorio 'Proyecto'
    sc Proyecto/

    # Máquina B (Receptor): Recibir el directorio en la carpeta actual
    sc rc


    Uso con cron ('crontab -e'):
    -----------------------------
    Recuerda usar la ruta absoluta al script 'sc' (ej: ${INSTALL_PATH}).

    # Emisor: Enviar el contenido de Descargas todos los días a las 9:00 AM
    00 09 * * * ${INSTALL_PATH} sccron /home/ubuntu/Descargas/

    # Receptor: Recibir 1 minuto después en el mismo directorio
    01 09 * * * cd /home/ubuntu/Descargas/ && ${INSTALL_PATH} rccron

EOF
}

# --- Lógica de Instalación ---
run_install() {
    echo "--- Instalador de sendcroc ---"
    
    # Comprobar si se ejecuta como root para la instalación
    if [ "$(id -u)" -ne 0 ]; then
        echo "Error: La instalación debe ejecutarse como root o con sudo." >&2
        echo "Por favor, ejecuta el comando de instalación anteponiendo 'sudo':" >&2
        echo "sudo bash <(curl -sL https://raw.githubusercontent.com/uGeek/sendcroc/main/sc) install $1" >&2
        exit 1
    fi
    
    local distro="$1"
    
    echo "1. Instalando dependencias para '${distro}'..."
    case "$distro" in
        debian)
            apt-get update && apt-get install -y curl
            ;;
        arch)
            pacman -Sy --noconfirm curl
            ;;
        termux)
            pkg update && pkg install -y croc curl
            ;;
        *)
            echo "Distribución no reconocida. Asegúrate de tener 'curl' instalado manualmente."
            ;;
    esac
    
    if ! command -v croc &> /dev/null && [ "$distro" != "termux" ]; then
        echo "2. Instalando 'croc'..."
        curl -sL https://getcroc.schollz.com | bash
    fi
    
    echo "3. Instalando el script 'sendcroc' en ${INSTALL_PATH}..."
    # Copia el script que se está ejecutando a la ubicación final
    cp -f "$0" "${INSTALL_PATH}"
    chmod +x "${INSTALL_PATH}"
    
    echo ""
    echo "¡Instalación completada!"
    echo "Ahora puedes ejecutar 'sc' desde cualquier lugar."
    echo "En la primera ejecución, se te guiará para crear un archivo de configuración."
    exit 0
}


# --- Lógica Principal del Script ---

# Si el primer argumento es 'install', se desvía a la rutina de instalación
if [ "$1" = "install" ]; then
    run_install "$2"
fi


check_dependencies() {
    if ! command -v croc &> /dev/null; then
        echo "Error: 'croc' no está instalado o no se encuentra en el PATH." >&2
        echo "Visita https://github.com/schollz/croc para ver cómo instalarlo." >&2
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

check_dependencies

mkdir -p "$CONFIG_DIR"
if [ ! -f "$CONFIG_FILE" ]; then
    initial_setup
fi
# Cargar la configuración solo si existe
if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
fi

# Lógica de compatibilidad para la variable RELAY
if ! [[ "$(declare -p RELAY 2>/dev/null)" =~ "declare -a" ]]; then
    if [ -n "$RELAY" ]; then # Solo si RELAY no está vacío
        echo "AVISO: Tu archivo sendcroc.conf usa un formato obsoleto para RELAY. Convirtiendo en memoria..." >&2
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
        echo "Preparando para recibir..."
        CROC_SECRET="${CROC_SECRET}" croc --yes --overwrite "${RELAY_OPTS[@]}"
        ;;

    "rcg")
        echo "Preparando para recibir (global)..."
        CROC_SECRET="${CROC_SECRET_GLOBAL}" croc --yes
        ;;
        
    "t")
        read -p "Escribe el texto a enviar y presiona Enter: " texto
        CROC_SECRET="${CROC_SECRET}" croc "${RELAY_OPTS[@]}" send --text "${texto}"
        ;;
        
    "tg")
        read -p "Escribe el texto a enviar (vía global): " texto
        CROC_SECRET="${CROC_SECRET_GLOBAL}" croc send --text "${texto}"
        ;;
        
    "g")
        shift
        CROC_SECRET="${CROC_SECRET_GLOBAL}" croc --yes send "$@"
        ;;
        
    "dotfile")
        cat "${CONFIG_FILE}"
        ;;
        
    "edotfile")
        ${EDITOR:-nano} "$CONFIG_FILE"
        ;;
        
    "sccron")
        shift
        CROC_SECRET="${CROC_SECRET}" croc --ignore-stdin "${RELAY_OPTS[@]}" send "$@"
        ;;
        
    "rccron")
        CROC_SECRET="${CROC_SECRET}" croc --yes --ignore-stdin --overwrite "${RELAY_OPTS[@]}"
        ;;
        
    *)
        echo "Enviando: $@"
        CROC_SECRET="${CROC_SECRET}" croc "${RELAY_OPTS[@]}" send "$@"
        ;;
esac

exit 0
