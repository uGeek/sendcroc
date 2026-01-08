#!/usr/bin/env bash

#===========#
#  sendcroc #
#===========#
#
# DESCRIPCIÓN: Wrapper definitivo para 'croc'. Documentación y Ejemplos Maximizados.
# VERSIÓN: 9.0.0 (Ultimate Documentation Update)
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
VERSION="9.0.0"
CONFIG_DIR="${HOME}/.config/sendcroc"
CONFIG_FILE="${CONFIG_DIR}/sendcroc.conf"
HISTORY_FILE="${CONFIG_DIR}/history.log"

# Inicialización segura de variables de config
RELAY_OPTS=() 
RELAY=()

# Flags de estado
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

# --- Funciones de Utilidad ---

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

# --- AYUDA Y EJEMPLOS ---

show_help() {
    cat << EOF
${BOLD}sendcroc (sc) v${VERSION}${NC} - Gestor avanzado de transferencias seguras.

${BOLD}USO:${NC} 
    sc [FLAGS] [g] <COMANDO | ARCHIVO>

${BOLD}JERARQUÍA DE CONEXIÓN:${NC}
    1. ${BOLD}--local${NC}: Fuerza descubrimiento por LAN (Broadcast).
    2. ${BOLD}g${NC}:       Fuerza uso de Relay GLOBAL (Público).
    3. ${BOLD}Defecto${NC}: Usa tu Relay PRIVADO (si está en sendcroc.conf).

${BOLD}OPCIONES DE TRANSFERENCIA:${NC}
    --zip, -z       Comprimir archivo/carpeta (.tar.gz) antes de enviar.
    --out, -o <dir> Definir directorio de destino para recibir.
    --burn          Borrar archivo original tras envío exitoso (Autodestrucción).
    --ask           Preguntar confirmación antes de recibir (Desactiva auto-yes).
    --resume        Reanudar transferencia / No sobrescribir archivos.

${BOLD}OPCIONES GENERALES:${NC}
    --conf <file>   Usar archivo de configuración alternativo.
    --dry-run       Simulación: Muestra el comando 'croc' sin ejecutarlo.
    --no-notify     Desactivar notificaciones de escritorio.
    --no-copy       No copiar la clave al portapapeles.
    --examples      Ver biblioteca completa de ejemplos.
    --help, -h      Ver esta ayuda.

${BOLD}COMANDOS:${NC}
    [archivo]       Enviar (clave fija).
    r               Recibir (clave fija).
    loop            Modo Demonio: Bucle infinito de recepción.
    qr              Generar código QR de la clave.
    text "msg"      Enviar texto o portapapeles.
    rand [file]     Enviar con código aleatorio.
    l [file]        Atajo para enviar localmente.
    log             Ver historial reciente.
    u               Actualizar binario 'croc'.
EOF
}

show_examples() {
    cat << EOF
${BOLD}=== BIBLIOTECA DE EJEMPLOS SENDCROC ===${NC}

${BOLD}1. FUNDAMENTOS (Usando tu Servidor Privado)${NC}
   ${GRS}Usa la configuración por defecto de sendcroc.conf${NC}
   sc documento.pdf             ${CYN}# Enviar archivo${NC}
   sc Fotos_Vacaciones/         ${CYN}# Enviar carpeta (croc la comprime sola)${NC}
   sc r                         ${CYN}# Recibir en la carpeta actual${NC}

${BOLD}2. SELECCIÓN DE RED${NC}
   ${GRS}Controla por dónde viajan tus datos${NC}
   sc g archivo.pdf             ${CYN}# Forzar servidor PÚBLICO (Global)${NC}
   sc --local video.mp4         ${CYN}# Forzar Red LOCAL (Broadcast P2P)${NC}
   sc l video.mp4               ${CYN}# Atajo rápido para lo anterior${NC}
   sc --local r                 ${CYN}# Recibir buscando solo en LAN${NC}

${BOLD}3. GESTIÓN DE ARCHIVOS AVANZADA${NC}
   ${GRS}Compresión y limpieza${NC}
   sc --zip node_modules/       ${CYN}# Comprime en tar.gz -> Envía -> Borra el zip${NC}
   sc --burn secretos.txt       ${CYN}# Envía -> Si éxito -> Borra el original${NC}
   sc -o ~/Descargas r          ${CYN}# Recibir guardando en 'Descargas'${NC}

${BOLD}4. AUTOMATIZACIÓN Y SERVIDORES${NC}
   ${GRS}Ideal para dejar un PC recibiendo cosas${NC}
   sc loop                      ${CYN}# Recibe, termina y vuelve a esperar${NC}
   sc -o /tmp/inbox loop        ${CYN}# Servidor de recepción continua en /tmp${NC}
   sc --local -o ~/Nas loop     ${CYN}# Servidor de recepción SOLO local${NC}

${BOLD}5. TEXTO Y TUBERÍAS (PIPES)${NC}
   ${GRS}Comparte información sin crear archivos${NC}
   sc text "ContraseñaWifi"     ${CYN}# Envía texto plano${NC}
   sc text "\$(cat id_rsa.pub)" ${CYN}# Envía contenido de un archivo como texto${NC}
   echo "Hola Mundo" | sc       ${CYN}# Envía STDIN (salida de un comando)${NC}
   tar cvf - . | sc             ${CYN}# Empaqueta y envía por pipe${NC}

${BOLD}6. CÓDIGOS ALEATORIOS Y MÓVIL${NC}
   ${GRS}Si no quieres usar tu clave fija${NC}
   sc rand foto.jpg             ${CYN}# Genera código tipo '1234-word-word'${NC}
   sc qr                        ${CYN}# Muestra QR para escanear con móvil${NC}

${BOLD}7. SEGURIDAD Y DEPURACIÓN${NC}
   sc --ask r                   ${CYN}# Pregunta (y/n) antes de bajar nada${NC}
   sc --dry-run archivo.txt     ${CYN}# Muestra qué comando ejecutaría (sin hacerlo)${NC}
   sc log                       ${CYN}# Muestra el historial de transferencias${NC}
EOF
}

check_dependencies() {
    if ! command -v croc &> /dev/null; then log_error "'croc' no instalado."; exit 1; fi
}

load_config() {
    if [ ! -f "$CONFIG_FILE" ]; then setup_wizard; fi
    # shellcheck source=/dev/null
    source "$CONFIG_FILE"
    
    # Compatibilidad RELAY vs RELAY_OPTS
    if [ ${#RELAY[@]} -gt 0 ]; then
        RELAY_OPTS=("${RELAY[@]}")
    fi

    if [ -z "${CROC_SECRET:-}" ]; then log_error "CROC_SECRET no definido."; exit 1; fi
}

setup_wizard() {
    echo -e "${BOLD}--- Configuración Inicial ---${NC}"
    mkdir -p "$CONFIG_DIR"
    read -r -p "Code Phrase (Secret): " secret
    read -r -p "Relay Privado (Ej: 192.168.1.50:9009) [Enter para Global]: " relay_addr
    
    if [ -z "$relay_addr" ]; then 
        echo "RELAY_OPTS=()" > /tmp/sc_relay_tmp
    else 
        echo "RELAY_OPTS=(--pass 'pass_opcional' --relay '$relay_addr')" > /tmp/sc_relay_tmp
    fi

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

    # --- LÓGICA DE SELECCIÓN DE SERVIDOR ---
    
    if [ "$LOCAL_MODE" -eq 1 ]; then
        # MODO LOCAL
        log_server "Conexión: [ MODO LOCAL (P2P Broadcast) ]"
        final_opts+=("--local")
        
    elif [ "$GLOBAL_FORCE" -eq 1 ]; then
        # MODO GLOBAL
        log_server "Conexión: [ SERVIDOR PÚBLICO (Global) ]"
        
    else
        # MODO PRIVADO (Defecto)
        if [ ${#RELAY_OPTS[@]} -gt 0 ]; then
            final_opts+=("${RELAY_OPTS[@]}")
            
            # Intento de extracción de IP para feedback visual
            local server_ip="Desconocido"
            local found=0
            for ((i=0; i<${#RELAY_OPTS[@]}; i++)); do
                if [[ "${RELAY_OPTS[i]}" == "--relay" ]]; then
                    if [[ -n "${RELAY_OPTS[i+1]:-}" ]]; then
                        server_ip="${RELAY_OPTS[i+1]}"
                        found=1
                        break
                    fi
                fi
            done
            
            if [ "$found" -eq 1 ]; then
                log_server "Conexión: [ SERVIDOR PRIVADO: \"$server_ip\" ]"
            else
                log_server "Conexión: [ SERVIDOR PRIVADO (Configurado) ]"
            fi
        else
            log_server "Conexión: [ SERVIDOR PÚBLICO (No hay relay configurado) ]"
        fi
    fi

    # --- EJECUCIÓN ---

    if [ "$DRY_RUN" -eq 1 ]; then
        echo -e "${GRS}[DRY-RUN] croc ${final_opts[*]} ${cmd[*]}${NC}"
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

# Detectar 'g' al principio
if [[ "${1:-}" == "g" ]]; then
    GLOBAL_FORCE=1
    shift
fi

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

if [ ! -t 0 ]; then
    log_info "Enviando STDIN..."
    CURRENT_ACTION="SEND_PIPE"
    CURRENT_TARGET="STDIN"
    if [[ "$COMMAND" == "rand" ]]; then execute_croc send
    else execute_croc send --code "${CROC_SECRET}"; fi
    exit 0
fi

case "$COMMAND" in
    "") log_error "Falta comando. 'sc -h' o 'sc --examples'."; exit 1 ;;
    "u") curl https://getcroc.schollz.com | bash; exit 0 ;;
    "log") [ -f "$HISTORY_FILE" ] && tail -n 20 "$HISTORY_FILE" ;;
    
    "qr")
        log_info "QR para clave: ${CROC_SECRET}"
        command -v curl &> /dev/null && curl -s "qrenco.de/${CROC_SECRET}" || log_error "Falta 'curl'."
        ;;

    "rand")
        shift
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
        export CROC_SECRET="$CROC_SECRET"
        log_info "Preparando recepción..."
        execute_croc "${RECV_OPTS[@]}"
        ;;

    "loop")
        CURRENT_ACTION="LOOP"
        export CROC_SECRET="$CROC_SECRET"
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
        if execute_croc send --code "$CROC_SECRET" "$@"; then
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
        execute_croc send --code "$CROC_SECRET" --text "$text"
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
            if execute_croc send --code "$CROC_SECRET" "${FILES_TO_SEND[@]}"; then
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
