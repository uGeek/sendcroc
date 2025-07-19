# sendcroc - Un Wrapper Inteligente y Potenciado para `croc`

![Bash Shell](https://img.shields.io/badge/shell-bash-blue?style=for-the-badge&logo=gnu-bash)
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

`sendcroc` (o `sc`) es un script en Bash que actúa como un *wrapper* o envoltorio para la increíble herramienta [croc](https://github.com/schollz/croc). Su objetivo es simplificar radicalmente el proceso de enviar y recibir archivos, directorios y texto al permitirte pre-configurar tus secretos y opciones de relay.

Olvídate de tener que recordar y teclear códigos complejos en ambos extremos. Con `sendcroc`, defines tu configuración una vez y las transferencias se vuelven tan simples como `sc mi_archivo.zip` en una máquina y `sc r` en la otra.

## ✨ Características

*   **Configuración Asistida**: Un asistente interactivo te guía en la primera ejecución para crear tu configuración.
*   **Comandos Intuitivos y Cortos**: Alias como `sc [archivo]` para enviar y `sc r` para recibir.
*   **Manejo Seguro de Secretos**: Utiliza la variable de entorno `CROC_SECRET` para evitar que tus códigos aparezcan en el historial o en la lista de procesos.
*   **Soporte para Múltiples Modos**:
    *   Transferencias con **Relay Privado** para máxima velocidad y privacidad.
    *   Transferencias con **Relay Público** para envíos rápidos.
    *   Transferencias exclusivas en **Red Local** para no salir a Internet.
*   **Automatización con Cron**: Comandos optimizados (`sccron`, `rccron`) y sus alias (`scron`, `rcron`) para facilitar la programación de transferencias.
*   **Configuraciones Múltiples**: Usa perfiles de configuración alternativos con la opción `--conf`.
*   **Previsualización Segura**: Con `--dry-run`, puedes ver el comando exacto que se ejecutaría sin enviar nada.
*   **Auto-actualización**: El comando `sc u` actualiza tanto `croc` como el propio script `sc`.
*   **Sin Dependencias Externas** (más allá de `croc`): Es un script de Bash puro.

## 🚀 Instalación y Actualización

### Paso 1: Instalar `croc` (Prerrequisito)

Si aún no tienes `croc`, instálalo con el siguiente comando:
```sh
curl https://getcroc.schollz.com | bash
```

(Nota: Esto podría requerir sudo si tu usuario no tiene permisos sobre /usr/local/bin).

Paso 2: Instalar sendcroc

Ejecuta este comando único en tu terminal para descargar el script y hacerlo ejecutable en todo tu sistema:

Generated sh
sudo curl -L https://raw.githubusercontent.com/uGeek/sendcroc/main/sc -o /usr/bin/sc && sudo chmod +x /usr/bin/sc
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
Sh
IGNORE_WHEN_COPYING_END
Actualización Simplificada

Para actualizar tanto croc como sendcroc a sus últimas versiones, simplemente ejecuta:

Generated sh
sc u
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
Sh
IGNORE_WHEN_COPYING_END
⚙️ Primera Ejecución y Configuración

Después de la instalación, ejecuta el script por primera vez. El asistente de configuración se iniciará automáticamente:

Generated sh
sc
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
Sh
IGNORE_WHEN_COPYING_END

Te pedirá un secreto personal, las opciones de tu relay (si tienes uno) y un secreto global opcional. Esto creará el archivo de configuración en ~/.config/sendcroc/sendcroc.conf.

Ejemplo de Archivo de Configuración

Tu archivo ~/.config/sendcroc/sendcroc.conf se verá así. Puedes editarlo en cualquier momento con sc edotfile.

Generated bash
# Archivo de configuración para sendcroc
CROC_SECRET='mi-frase-secreta-y-muy-larga-imposible-de-adivinar'

# Las opciones de Relay deben ser un array de Bash para funcionar correctamente.
# Cada opción y su valor son elementos separados.
# Ejemplo: RELAY=(--pass 'mi-clave-de-relay' --relay '192.168.1.10:9009')
RELAY=(--pass 'xxxx' --relay '140.140.140.140:9009')

# Secreto para transferencias públicas o rápidas sin usar tu relay privado.
CROC_SECRET_GLOBAL="codigo-rapido-y-facil"
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
Bash
IGNORE_WHEN_COPYING_END
💻 Uso

La sintaxis general es sc [COMANDO] [ARGUMENTO] [OPCIONES].

Comando	Alias	Descripción
Transferencia Privada (con Relay)		
sc [archivo/dir]		Envía un archivo o directorio con tu configuración privada.
sc r		Recibe un archivo o directorio con tu configuración privada.
sc t		Pide un texto por la terminal y lo envía con la config. privada.
Transferencia Pública (Relay Global)		
sc g [archivo/dir]		Envía usando la configuración global (pública).
sc rg		Recibe usando la configuración global.
Transferencia en Red Local		
sc l [archivo/dir]		Envía un archivo/dir forzando una conexión local.
sc lt		Envía un texto forzando una conexión local.
Automatización (Cron)		
sccron [ruta]	scron	Comando optimizado para enviar desde un cronjob.
rccron	rcron	Comando optimizado para recibir desde un cronjob.
lscron [ruta]		Envía desde cronjob forzando conexión local.
lrcron		Recibe desde cronjob forzando conexión local.
Gestión y Opciones		
dotfile		Muestra el contenido de tu archivo de configuración.
edotfile		Abre el archivo de configuración para editarlo.
u		Actualiza croc y el script sc.
--conf [ruta]		Usa un archivo de configuración alternativo.
--dry-run		Muestra el comando que se ejecutaría sin ejecutarlo.
Ejemplos Prácticos

1. Envío Interactivo:

Máquina A (Emisor): Envía tu directorio de Proyectos.

Generated sh
sc ~/Proyectos
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
Sh
IGNORE_WHEN_COPYING_END

Máquina B (Receptor): Ve al directorio donde quieres recibirlo y ejecuta:

Generated sh
sc r
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
Sh
IGNORE_WHEN_COPYING_END

2. Previsualizar un Comando con --dry-run:

Antes de enviar un archivo grande, puedes verificar que todos los parámetros son correctos.

Generated sh
sc /datos/backup_semanal.tar.gz --dry-run
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
Sh
IGNORE_WHEN_COPYING_END

Salida esperada:

Generated code
Enviando: /datos/backup_semanal.tar.gz
DRY-RUN: croc '--pass' 'xxxx' '--relay' '140.140.140.140:9009' 'send' '--code' 'mi-frase-secreta-y-muy-larga-imposible-de-adivinar' '/datos/backup_semanal.tar.gz'
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
IGNORE_WHEN_COPYING_END

3. Usar una Configuración Alternativa con --conf:

Si tienes un archivo para el trabajo y otro personal.

Generated sh
sc CV.pdf --conf ~/.config/sendcroc/trabajo.conf
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
Sh
IGNORE_WHEN_COPYING_END

4. Automatización con Cron (crontab -e):

Recuerda usar siempre la ruta absoluta al script sc y los alias de cron.

Emisor (a las 2:00 AM):

Generated crontab
0 2 * * * /usr/bin/sc scron /home/user/backups/
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
Crontab
IGNORE_WHEN_COPYING_END

Receptor (a las 2:01 AM):

Generated crontab
1 2 * * * cd /home/user/restores/ && /usr/bin/sc rcron
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
Crontab
IGNORE_WHEN_COPYING_END
📜 Licencia

Este proyecto está bajo la Licencia MIT.

🙏 Agradecimientos

A schollz por crear y mantener la fantástica herramienta croc.

A uGeek por la idea original y el primer script que sirvió de inspiración para esta versión mejorada.

