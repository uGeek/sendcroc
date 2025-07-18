# sendcroc - Un Wrapper Inteligente para `croc`

![Bash Shell](https://img.shields.io/badge/shell-bash-blue?style=for-the-badge&logo=gnu-bash)
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

`sendcroc` (o `sc`) es un script en Bash que actúa como un *wrapper* o envoltorio para la increíble herramienta [croc](https://github.com/schollz/croc). Su objetivo es simplificar radicalmente el proceso de enviar y recibir archivos, directorios y texto al permitirte pre-configurar tus secretos y opciones de relay en un archivo de configuración simple.

Olvídate de tener que recordar y teclear códigos complejos en ambos extremos. Con `sendcroc`, defines tu configuración una vez y las transferencias se vuelven tan simples como `sc mi_archivo.zip` en una máquina y `sc rc` en la otra.

## ✨ Características

*   **Instalación Universal**: Un único comando para instalar el script en cualquier sistema Linux.
*   **Configuración Automática**: Un asistente interactivo te guía en la primera ejecución para crear tu configuración.
*   **Manejo Seguro de Secretos**: Utiliza la variable de entorno `CROC_SECRET` para evitar que tus códigos aparezcan en el historial o en la lista de procesos.
*   **Soporte para Relay Personalizado**: Configura tu propio relay para máxima velocidad y privacidad.
*   **Comandos Intuitivos**: Comandos simples y directos como `sc mi_archivo` para enviar y `sc rc` para recibir.
*   **Automatización con Cron**: Comandos específicos (`sccron`, `rccron`) para facilitar la programación de transferencias automáticas.
*   **Sin Dependencias Externas (más allá de `croc`)**: Es un script de Bash puro.

## 🚀 Instalación

La instalación se realiza en dos sencillos pasos.

### Paso 1: Instalar `croc` (Prerrequisito)

Si aún no tienes `croc`, instálalo con el siguiente comando:
```sh
curl https://getcroc.schollz.com | bash
```
*(Nota: Esto podría requerir `sudo` si tu usuario no tiene permisos sobre `/usr/local/bin`)*.

### Paso 2: Instalar `sendcroc`

Ejecuta este comando único en tu terminal para descargar el script y hacerlo ejecutable en todo tu sistema:
```sh
sudo curl -L https://raw.githubusercontent.com/uGeek/sendcroc/main/sc -o /usr/bin/sc && sudo chmod +x /usr/bin/sc
```

¡Y eso es todo! Ya puedes usar el comando `sc`.

## ⚙️ Primera Ejecución y Configuración

Después de la instalación, ejecuta el script por primera vez. El asistente de configuración se iniciará automáticamente:
```sh
sc
```
Te pedirá un secreto personal, las opciones de tu relay (si tienes uno) y un secreto global opcional. Esto creará el archivo de configuración en `~/.config/sendcroc/sendcroc.conf`.

### Ejemplo de Archivo de Configuración

Tu archivo `~/.config/sendcroc/sendcroc.conf` se verá así. Puedes editarlo en cualquier momento con `sc edotfile`.

```bash
# Archivo de configuración para sendcroc
CROC_SECRET='mi-frase-secreta-y-muy-larga-imposible-de-adivinar'

# Las opciones de Relay deben ser un array de Bash para funcionar correctamente.
# Cada opción y su valor son elementos separados.
# Ejemplo: RELAY=(--pass 'mi-clave-de-relay' --relay '192.168.1.10:9009')
RELAY=(--pass 'xxxx' --relay '140.140.140.140:9009')

# Secreto para transferencias públicas o rápidas sin usar tu relay privado.
CROC_SECRET_GLOBAL="codigo-rapido-y-facil"
```

## Usage

La sintaxis general es `sc [COMANDO] [ARGUMENTO]`.

| Comando | Descripción |
| :--- | :--- |
| `sc [archivo/dir]` | **Envía** un archivo o directorio con tu configuración privada. |
| `sc rc` | **Recibe** un archivo o directorio con tu configuración privada. |
| `sc g [archivo/dir]` | **Envía** usando la configuración global (pública). |
| `sc rcg` | **Recibe** usando la configuración global. |
| `sc t` | Pide un **texto** por la terminal y lo envía. |
| `dotfile` | Muestra el contenido de tu archivo de configuración. |
| `edotfile` | Abre el archivo de configuración para editarlo. |
| `sccron [ruta]`| Comando optimizado para **enviar** desde un cronjob. |
| `rccron` | Comando optimizado para **recibir** desde un cronjob. |

### Ejemplos Prácticos

**Uso Interactivo:**
> **Máquina A (Emisor):** Envía tu directorio de Proyectos.
> ```sh
> sc ~/Proyectos
> ```
> **Máquina B (Receptor):** Ve al directorio donde quieres recibirlo y ejecuta:
> ```sh
> sc rc
> ```

**Uso con Cron (`crontab -e`):**
> Recuerda usar siempre la **ruta absoluta** al script `sc` en tus cronjobs.
>
> **Emisor (a las 2:00 AM):**
> ```crontab
> 0 2 * * * /usr/bin/sc sccron /home/user/backups/
> ```
> **Receptor (a las 2:01 AM):**
> ```crontab
> 1 2 * * * cd /home/user/restores/ && /usr/bin/sc rccron
> ```

## 📜 Licencia

Este proyecto está bajo la Licencia MIT.

## 🙏 Agradecimientos

*   A **[schollz](https://github.com/schollz)** por crear y mantener la fantástica herramienta `croc`.
*   A **[uGeek](https://ugeek.github.io/)** por la idea original y el primer script que sirvió de inspiración para esta versión mejorada.
