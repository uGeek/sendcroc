#!/usr/bin/env bash

#===========#
#  sendcroc #
#===========#
#
# DESCRIPCIÓN: Wrapper definitivo para 'croc'. (Cron Support, Config Edit, Full Docs).
# VERSIÓN: 9.2.0 (Cron & Edit Features)
# AUTOR: Refactorizado por IA

# --- Configuración de seguridad ---
set -o errexit
set -o pipefail
set -o nounset

# --- Colores ---
if [ -t 1 ]; then
    RJO=$(printf '\033[31m')
    VRD=$(printf '\033[32m')
    AMA=$(printf '\033[33m')
    AZL=$(printf '\033[34m')
    CYN=$(printf '\033[36m')
    GRS=$(printf '\033[90m')
    NC=$(printf '\033[0m')
    BOLD=$(printf '\033[1m')
else
    RJO="" VRD="" AMA="" AZL="" CYN="" GRS="" NC="" BOLD=""
fi

# --- Variables Globales ---
VERSION="9.2.0"
CONFIG_DIR="${HOME}/.config/sendcroc"
CONFIG_FILE="${CONFIG_DIR}/sendcroc.conf"
HISTORY_FILE="${CONFIG_DIR}/history.log"

RELAY_OPTS=() 
RELAY=()

# Flags
DRY_RUN=0
NOTIFY=1
CLIPBOARD=1
FORCE_OVERWRITE=1
AUTO_YES=1
BURN_AFTER=0
LOCAL_MODE=0
GLOBAL_FORCE=0
ZIP_MODE=0
OUTPUT_DIR=""
CURRENT_ACTION=""
CURRENT_TARGET=""

# --- Funciones ---

log_info() { echo -e "${AZL}${BOLD}[INFO]${NC} $1"; }
log_server() { echo -e "${CYN}${BOLD}[NET]${NC} $1"; }
log_success() { echo -e "${VRD}${BOLD}[OK]${NC} $1"; }
log_warn() { echo -e "${AMA}${BOLD}[WARN]${NC} $1" >&2; }
log_error() { echo -e "${RJO}${BOLD}[ERROR]${NC} $1" >&2; }

append_history() {
    local status="$1"
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ ! -d "$CONFIG_DIR" ] && mkdir -p "$CONFIG_DIR"
    [ -n "$CURRENT_ACTION" ] && echo "[$timestamp] [$status] [$CURRENT_ACTION] $CURRENT_TARGET" >> "$HISTORY_FILE"
}

notify() {
    if [ "$NOTIFY" -eq 1 ] && command -v notify-send &> /dev/null; then
        if [ -n "${DISPLAY:-}" ] || [ -n "${DBUS_SESSION_BUS_ADDRESS:-}" ]; then
            notify-send -a "sendcroc" "Transferencia finalizada" "$1" 2>/dev/null || true
        fi
    fi
}

copy_to_clipboard() {
    local code="$1"
    if [ "$CLIPBOARD" -ne 1 ]; then return 0; fi
    if [ -z "${DISPLAY:-}" ] && [ -z "${WAYLAND_DISPLAY:-}" ]; then return 0; fi

    if command -v wl-copy &> /dev/null; then
        echo -n "$code" | wl-copy 2>/dev/null || true
        log_info "Código copiado (Wayland)."
    elif command -v xclip &> /dev/null; then
        echo -n "$code" | xclip -selection clipboard 2>/dev/null || true
        log_info "Código copiado (X11)."
    elif command -v pbcopy &> /dev/null; then
        echo -n "$code" | pbcopy 2>/dev/null || true
        log_info "Código copiado (macOS)."
    fi
}

show_help() {
    cat << EOF
${BOLD}sendcroc (sc) v${VERSION}${NC} - Gestor avanzado de transferencias.

${BOLD}USO:${NC} 
    sc [FLAGS] [g] <COMANDO | ARCHIVO>

${BOLD}JERARQUÍA DE CONEXIÓN:${NC}
    1. ${BOLD}--local${NC}: Fuerza descubrimiento por LAN (Broadcast).
    2. ${BOLD}g${NC}:       Fuerza uso de Relay GLOBAL (Público).
    3. ${BOLD}Defecto${NC}: Usa tu Relay PRIVADO (si está configurado).

${BOLD}OPCIONES DE TRANSFERENCIA:${NC}
    --zip, -z       Comprimir archivo/carpeta (.tar.gz).
    --out, -o <dir> Directorio de destino.
    --burn          Borrar original tras envío exitoso.
    --ask           Preguntar antes de recibir (Desactiva auto-yes).
    --resume        No sobrescribir archivos.

${BOLD}OPCIONES GENERALES:${NC}
    --conf <file>   Usar configuración alternativa.
    --dry-run       Simulación (no ejecuta).
    --no-notify     Sin notificaciones.
    --no-copy       No copiar clave al portapapeles.
    --examples      Ver TODOS los ejemplos disponibles.
    --help, -h      Ver esta ayuda.

${BOLD}COMANDOS:${NC}
    [archivo]       Enviar (clave fija).
    r               Recibir (clave fija).
    sccron [file]   Modo Emisor CRON (No interactivo, --ignore-stdin).
    rccron          Modo Receptor CRON (Sobrescribe, --ignore-stdin).
    edit (o conf)   Editar archivo de configuración con nano.
    loop            Modo Demonio (Bucle infinito).
    qr              Ver código QR.
    text "msg"      Enviar texto.
    rand [file]     Enviar con código aleatorio.
    l [file]        Atajo local.
    log             Ver historial.
    u               Actualizar 'croc'.
EOF
}

show_examples() {
    cat << EOF
${BOLD}=== BIBLIOTECA DE EJEMPLOS SENDCROC ===${NC}

${BOLD}1. AUTOMATIZACIÓN CON CRON (Programador de tareas)${NC}
   ${GRS}Estos comandos están diseñados para no pedir interacción humana (--ignore-stdin).${NC}
   
   ${CYN}# Enviar una copia de seguridad todos los días a las 09:00${NC}
   ${BOLD}00 09 * * * /usr/bin/sc sccron /home/ubuntu/backup.tar.gz${NC}

   ${CYN}# Recibir la copia en el otro servidor a las 09:05${NC}
   ${BOLD}05 09 * * * cd /backup/ ; /usr/bin/sc rccron${NC}

${BOLD}2. CONFIGURACIÓN RÁPIDA${NC}
   sc edit                  ${CYN}# Abre la configuración con nano${NC}

${BOLD}3. USO BÁSICO (Clave Fija)${NC}
   sc documento.pdf         ${CYN}# Enviar archivo${NC}
   sc Fotos/                ${CYN}# Enviar carpeta${NC}
   sc r                     ${CYN}# Recibir${NC}

${BOLD}4. RED LOCAL Y SERVIDORES${NC}
   sc --local video.mp4     ${CYN}# Enviar solo por LAN (Broadcast)${NC}
   sc g video.mp4           ${CYN}# Forzar servidor PÚBLICO${NC}

${BOLD}5. GESTIÓN DE ARCHIVOS${NC}
   sc --zip Proyecto/       ${CYN}# Comprime -> Envía -> Borra zip${NC}
   sc --burn secretos.txt   ${CYN}# Envía -> Borra original${NC}
   sc -o ~/Descargas r      ${CYN}# Recibir en carpeta específica${NC}

${BOLD}6. MODOS SERVIDOR / DEMONIO${NC}
   sc loop                  ${CYN}# Recibe en bucle infinito${NC}
   sc -o /tmp loop          ${CYN}# Servidor temporal${NC}

${BOLD}7. TEXTO Y PIPES${NC}
   sc text "123456"         ${CYN}# Envía texto plano${NC}
   ls -la | sc              ${CYN}# Envía salida de comando${NC}
   sc rand foto.jpg         ${CYN}# Código aleatorio${NC}
   sc qr                    ${CYN}# Ver QR${NC}
EOF
}

check_dependencies() {
    if ! command -v croc &> /dev/null; then log_error "'croc' no instalado."; exit 1; fi
}

load_config() {
    if [ ! -f "$CONFIG_FILE" ]; then setup_wizard; fi
    # shellcheck source=/dev/null
    source "$CONFIG_FILE"
    
    if [ ${#RELAY[@]} -gt 0 ]; then RELAY_OPTS=("${RELAY[@]}"); fi
    if [ -z "${CROC_SECRET:-}" ]; then log_error "CROC_SECRET no definido."; exit 1; fi
}

setup_wizard() {
    echo -e "${BOLD}--- Configuración ---${NC}"
    mkdir -p "$CONFIG_DIR"
    read -r -p "Code Phrase (Secret): " secret
    read -r -p "Relay Privado (Ej: 192.168.1.50:9009) [Enter para Global]: " relay_addr
    
    if [ -z "$relay_addr" ]; then echo "RELAY_OPTS=()" > /tmp/sc_relay_tmp
    else echo "RELAY_OPTS=(--pass 'pass' --relay '$relay_addr')" > /tmp/sc_relay_tmp; fi

    cat > "$CONFIG_FILE" << EOF
CROC_SECRET='$secret'
$(cat /tmp/sc_relay_tmp)
EOF
    rm /tmp/sc_relay_tmp
    chmod 600 "$CONFIG_FILE"
}

execute_croc() {
    local cmd=("$@")
    local final_opts=()

    if [ "$LOCAL_MODE" -eq 1 ]; then
        log_server "Conexión: [ MODO LOCAL (P2P Broadcast) ]"
        final_opts+=("--local")
    elif [ "$GLOBAL_FORCE" -eq 1 ]; then
        log_server "Conexión: [ SERVIDOR PÚBLICO (Global) ]"
    else
        if [ ${#RELAY_OPTS[@]} -gt 0 ]; then
            final_opts+=("${RELAY_OPTS[@]}")
            local server_ip="Desconocido"
            local found=0
            for ((i=0; i<${#RELAY_OPTS[@]}; i++)); do
                if [[ "${RELAY_OPTS[i]}" == "--relay" ]] && [[ -n "${RELAY_OPTS[i+1]:-}" ]]; then
                    server_ip="${RELAY_OPTS[i+1]}"
                    found=1
                    break
                fi
            done
            if [ "$found" -eq 1 ]; then
                log_server "Conexión: [ SERVIDOR PRIVADO: \"$server_ip\" ]"
            else
                log_server "Conexión: [ SERVIDOR PRIVADO (Configurado) ]"
            fi
        else
            log_server "Conexión: [ SERVIDOR PÚBLICO (Global Default) ]"
        fi
    fi

    if [ "$DRY_RUN" -eq 1 ]; then
        echo -e "${GRS}[DRY-RUN] export CROC_SECRET='****'; croc ${final_opts[*]} ${cmd[*]}${NC}"
        return 0
    else
        if command croc "${final_opts[@]}" "${cmd[@]}"; then
            notify "Éxito"
            append_history "OK"
            return 0
        else
            local err=$?
            notify "Error ($err)"
            append_history "ERROR"
            return $err
        fi
    fi
}

# --- Parseo de Argumentos ---

ARGS=()
while [[ $# -gt 0 ]]; do
    case "$1" in
        --conf) CONFIG_FILE="$2"; shift 2 ;;
        --out|-o) OUTPUT_DIR="$2"; shift 2 ;;
        --burn) BURN_AFTER=1; shift ;;
        --local) LOCAL_MODE=1; shift ;;
        --zip|-z) ZIP_MODE=1; shift ;;
        --ask) AUTO_YES=0; shift ;;
        --dry-run) DRY_RUN=1; shift ;;
        --no-notify) NOTIFY=0; shift ;;
        --no-copy) CLIPBOARD=0; shift ;;
        --resume) FORCE_OVERWRITE=0; shift ;;
        --examples) show_examples; exit 0 ;;
        --help|-h) show_help; exit 0 ;;
        *) ARGS+=("$1"); shift ;;
    esac
done
set -- "${ARGS[@]+"${ARGS[@]}"}"

if [[ "${1:-}" != "u" ]]; then check_dependencies; load_config; fi
if [[ "${1:-}" == "g" ]]; then GLOBAL_FORCE=1; shift; fi

COMMAND="${1:-}"
RECV_OPTS=()
if [ -n "$OUTPUT_DIR" ]; then
    mkdir -p "$OUTPUT_DIR"
    RECV_OPTS+=("--out" "$OUTPUT_DIR")
fi
if [ "$AUTO_YES" -eq 1 ]; then
    RECV_OPTS+=("--yes")
    if [ "$FORCE_OVERWRITE" -eq 1 ]; then RECV_OPTS+=("--overwrite"); fi
fi

# --- Ejecución ---

export CROC_SECRET="$CROC_SECRET"

if [ ! -t 0 ]; then
    log_info "Enviando STDIN..."
    CURRENT_ACTION="SEND_PIPE"
    CURRENT_TARGET="STDIN"
    if [[ "$COMMAND" == "rand" ]]; then
        unset CROC_SECRET
        execute_croc send
    else
        execute_croc send
    fi
    exit 0
fi

case "$COMMAND" in
    "") log_error "Falta comando. 'sc -h' o 'sc --examples'."; exit 1 ;;
    "u") curl https://getcroc.schollz.com | bash; exit 0 ;;
    "log") [ -f "$HISTORY_FILE" ] && tail -n 20 "$HISTORY_FILE" ;;
    
    # --- EDICIÓN DE CONFIGURACIÓN ---
    "edit"|"conf")
        log_info "Editando configuración con nano..."
        if command -v nano &> /dev/null; then
            nano "$CONFIG_FILE"
        else
            vi "$CONFIG_FILE"
        fi
        exit 0
        ;;

    # --- MODO CRON EMISOR (sccron) ---
    "sccron")
        shift
        if [ -z "${1:-}" ]; then log_error "Cron Send: Falta archivo"; exit 1; fi
        CURRENT_ACTION="SEND_CRON"
        CURRENT_TARGET="$*"
        # Forzamos ignore-stdin para que no se cuelgue en cron
        log_info "Modo Cron Send (No interactivo)..."
        execute_croc --ignore-stdin send "$@"
        ;;

    # --- MODO CRON RECEPTOR (rccron) ---
    "rccron")
        CURRENT_ACTION="RECV_CRON"
        CURRENT_TARGET="Incoming"
        log_info "Modo Cron Recv (No interactivo, Sobrescribir)..."
        # Forzamos yes, overwrite y ignore-stdin
        execute_croc --yes --overwrite --ignore-stdin "${RECV_OPTS[@]}"
        ;;

    "qr")
        log_info "QR para clave: ${CROC_SECRET}"
        command -v curl &> /dev/null && curl -s "qrenco.de/${CROC_SECRET}" || log_error "Falta 'curl'."
        ;;

    "rand")
        shift
        unset CROC_SECRET
        CURRENT_ACTION="SEND_RANDOM"
        CURRENT_TARGET="$*"
        IS_TEMP_ZIP=0
        if [ "$ZIP_MODE" -eq 1 ]; then
            log_info "Comprimiendo..."
            tarname="archive_$(date +%s).tar.gz"
            tar -czf "$tarname" "$@"
            set -- "$tarname"
            IS_TEMP_ZIP=1
        fi
        log_info "Enviando (Random)..."
        if execute_croc send "$@"; then
            if [ "$IS_TEMP_ZIP" -eq 1 ] && [ "$DRY_RUN" -eq 0 ]; then rm "$tarname"; fi
        fi
        ;;

    "r")
        CURRENT_ACTION="RECV"
        CURRENT_TARGET="Incoming"
        log_info "Preparando recepción..."
        execute_croc "${RECV_OPTS[@]}"
        ;;

    "loop")
        CURRENT_ACTION="LOOP"
        log_info "Modo BUCLE. Ctrl+C para salir."
        while true; do
            echo -e "\n${CYN}--- Esperando ---${NC}"
            execute_croc "${RECV_OPTS[@]}" || true
            sleep 1
        done
        ;;

    "l")
        shift
        LOCAL_MODE=1
        if [ -z "${1:-}" ]; then log_error "Falta archivo"; exit 1; fi
        COMMAND="$1"
        CURRENT_ACTION="SEND_LOCAL"
        CURRENT_TARGET="$*"
        IS_TEMP_ZIP=0
        if [ "$ZIP_MODE" -eq 1 ]; then
            log_info "Comprimiendo..."
            tarname="${COMMAND}.tar.gz"
            tar -czf "$tarname" "$@"
            set -- "$tarname"
            IS_TEMP_ZIP=1
        fi
        log_info "Enviando LOCAL..."
        copy_to_clipboard "$CROC_SECRET"
        if execute_croc send "$@"; then
            if [ "$IS_TEMP_ZIP" -eq 1 ] && [ "$DRY_RUN" -eq 0 ]; then rm "$tarname"; fi
            if [ "$BURN_AFTER" -eq 1 ] && [ "$DRY_RUN" -eq 0 ] && [ "$IS_TEMP_ZIP" -eq 0 ]; then rm -rf "$COMMAND"; fi
        fi
        ;;

    "text"|"t")
        shift
        CURRENT_ACTION="SEND_TEXT"
        text="$*"
        [ -z "$text" ] && read -r -p "Texto: " text
        copy_to_clipboard "$CROC_SECRET"
        log_info "Enviando texto..."
        execute_croc send --text "$text"
        ;;

    *)
        if [ -e "$COMMAND" ]; then
            CURRENT_ACTION="SEND_FIXED"
            CURRENT_TARGET="$COMMAND"
            IS_TEMP_ZIP=0
            FILES_TO_SEND=("$@")
            if [ "$ZIP_MODE" -eq 1 ]; then
                log_info "Comprimiendo $COMMAND..."
                tarname="${COMMAND}.tar.gz"
                tar -czf "$tarname" "${FILES_TO_SEND[@]}"
                FILES_TO_SEND=("$tarname")
                IS_TEMP_ZIP=1
            fi
            log_info "Enviando: ${FILES_TO_SEND[*]}"
            copy_to_clipboard "$CROC_SECRET"
            if execute_croc send "${FILES_TO_SEND[@]}"; then
                if [ "$IS_TEMP_ZIP" -eq 1 ] && [ "$DRY_RUN" -eq 0 ]; then rm "$tarname"; fi
                if [ "$BURN_AFTER" -eq 1 ] && [ "$DRY_RUN" -eq 0 ]; then
                    log_warn "Autodestrucción (--burn)..."
                    rm -rf "$COMMAND"
                    if [ $# -gt 0 ]; then rm -rf "$@"; fi
                fi
            fi
        else
            log_error "Archivo desconocido: $COMMAND"
            exit 1
        fi
        ;;
esac
