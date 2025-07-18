# sendcroc - Un Wrapper Inteligente para `croc`

![Bash Shell](https://img.shields.io/badge/shell-bash-blue?style=for-the-badge&logo=gnu-bash)![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

`sendcroc` (o `sc`) es un script en Bash que actúa como un *wrapper* o envoltorio para la increíble herramienta [croc](https://github.com/schollz/croc). Su objetivo es simplificar radicalmente el proceso de enviar y recibir archivos, directorios y texto al permitirte pre-configurar un código secreto y, opcionalmente, un servidor relay.

Olvídate de tener que recordar y teclear códigos complejos en ambos extremos. Con `sendcroc`, defines tu configuración una vez y las transferencias se vuelven tan simples como `sc mi_archivo.zip` en una máquina y `rc` en la otra.

## ✨ Características

*   **Configuración Sencilla**: Un asistente interactivo te guía en la primera ejecución para crear tu configuración.
*   **Códigos Secretos Persistentes**: Usa un código secreto predefinido para tus transferencias privadas, eliminando la necesidad de compartirlo cada vez.
*   **Soporte para Relay Personalizado y Global**: Configura tu propio relay para máxima velocidad y privacidad, o usa un relay público para transferencias ocasionales.
*   **Alias Simples**: Incluye alias (`rc`, `rcg`) para que recibir archivos sea un comando de dos letras.
*   **Transferencia de Texto**: Envía fragmentos de texto o notas rápidas directamente desde la terminal con `sc t`.
*   **Automatización con Cron**: Comandos específicos (`sccron`, `rccron`) para facilitar la programación de transferencias automáticas.
*   **Gestión Fácil**: Visualiza (`dotfile`) y edita (`edotfile`) tu configuración en cualquier momento.
*   **Robusto y Seguro**: Respeta las variables de entorno del usuario (como `$EDITOR`) y sigue las buenas prácticas de scripting en Bash.

## 📋 Prerrequisitos

Para usar `sendcroc`, solo necesitas dos cosas:

1.  **Bash**: Ya viene instalado en prácticamente todos los sistemas Linux, macOS y en Windows a través de WSL.
2.  **`croc`**: La herramienta que hace la magia.

Si no tienes `croc` instalado, puedes hacerlo fácilmente con este comando:
```sh
curl https://getcroc.schollz.com | bash
```

## 🚀 Instalación y Configuración

Sigue estos 4 sencillos pasos para tener `sendcroc` funcionando.

### Paso 1: Descargar el Script

Descarga el script `sc.sh` en un directorio local. Por ejemplo, en `~/.local/bin/` (un lugar común para scripts de usuario).

```sh
# Crea el directorio si no existe
mkdir -p ~/.local/bin

# Descarga el script y renómbralo a 'sc'
curl -L [URL_DEL_SCRIPT_sc.sh] -o ~/.local/bin/sc
```
> **Nota:** Reemplaza `[URL_DEL_SCRIPT_sc.sh]` por la URL real del script en tu repositorio.

### Paso 2: Dar Permisos de Ejecución

Haz que el script sea ejecutable:
```sh
chmod +x ~/.local/bin/sc
```

### Paso 3: Añadir al PATH (si es necesario)

Asegúrate de que el directorio `~/.local/bin` esté en tu `PATH` para que puedas llamar a `sc` desde cualquier lugar. Añade esta línea al final de tu `~/.bashrc` o `~/.zshrc`:
```sh
export PATH="$HOME/.local/bin:$PATH"
```
Recarga tu terminal o ejecuta `source ~/.bashrc` para aplicar los cambios.

### Paso 4: Configuración Inicial (¡El Paso Mágico!)

Ejecuta el script por primera vez. El asistente de configuración se iniciará automáticamente:
```sh
sc
```
Te pedirá un código secreto personal, la configuración de tu relay (si tienes uno) y un código global opcional.

Una vez finalizado, **te mostrará los alias `rc` y `rcg` que debes añadir a tu `~/.bashrc` o `~/.zshrc`**. Este es el último paso y es **crucial** para poder recibir archivos fácilmente.

```sh
# Ejemplo de lo que debes añadir a tu .bashrc/.zshrc
alias rc='croc --yes --overwrite mi-codigo-secreto'
alias rcg='croc --yes codigo-publico'
```

¡Y ya está! Estás listo para transferir archivos a la velocidad de la luz.

## Usage

La sintaxis general es `sc [COMANDO] [ARGUMENTO]`.

| Comando | Descripción |
| :--- | :--- |
| `h`, `--help` | Muestra la ayuda detallada. |
| `sc [archivo]` | **Envía un archivo**. `sc mi_foto.jpg` |
| `sc [directorio]` | **Envía un directorio**. `sc Documentos/` |
| `sc .` | **Envía todo el contenido** del directorio actual. |
| `sc g [archivo]` | Envía usando la **configuración global** (relay y código público). |
| `sc t` | Pide un **texto** por la terminal y lo envía. |
| `sc tg` | Envía un **texto** usando la configuración global. |
| `rc` | **Recibe** un archivo, directorio o texto (requiere el alias). |
| `rcg` | **Recibe** usando la configuración global (requiere el alias). |
| `dotfile` | Muestra la configuración actual y los alias recomendados. |
| `edotfile` | Abre el archivo de configuración para editarlo. |

### Ejemplos de Flujo de Trabajo

**Escenario**: Enviar el directorio `ProyectoX` desde tu portátil al servidor.

1.  **En el portátil (emisor)**:
    ```sh
    # Navega al directorio que contiene ProyectoX
    cd ~/Proyectos/
    
    # Envía el directorio
    sc ProyectoX
    ```

2.  **En el servidor (receptor)**:
    ```sh
    # Sitúate donde quieres recibir el directorio
    cd /var/www/
    
    # Ejecuta el alias de recepción
    rc
    ```
    `croc` se conectará usando el código pre-configurado y descargará el directorio.

## 🤖 Uso Avanzado: Automatización con Cron

`sendcroc` está preparado para automatizar transferencias.

### Enviar con Cron (`sccron`)
Para enviar el backup diario de una base de datos a las 2 AM:

```crontab
# m h  dom mon dow   command
0 2 * * * /ruta/completa/a/sc sccron /backups/db_backup.sql.gz
```

### Recibir con Cron (`rccron`)
Para que un servidor de respaldo se sincronice y reciba archivos a las 2:05 AM:

```crontab
# m h  dom mon dow   command
5 2 * * * cd /ruta/de/descarga/ && /ruta/completa/a/sc rccron
```
> **Importante**: En cron, siempre usa la ruta absoluta al script `sc`. Puedes encontrarla con `which sc`.

## 📜 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 🙏 Agradecimientos

*   A **[schollz](https://github.com/schollz)** por crear y mantener la fantástica herramienta `croc`.
*   A **[uGeek](https://ugeek.github.io/)** por la idea original y el primer script que sirvió de inspiración para esta versión mejorada.
