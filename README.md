# sendcroc – Un Wrapper Inteligente y Potenciado para `croc`

![Bash Shell](https://img.shields.io/badge/shell-bash-blue?style=for-the-badge&logo=gnu-bash)  
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

`sendcroc` (o `sc`) es un script en Bash que actúa como un _wrapper_ para la increíble herramienta [croc](https://github.com/schollz/croc). Su objetivo es simplificar radicalmente el proceso de enviar y recibir archivos, directorios y texto al permitirte preconfigurar tus secretos y opciones de relay.

Olvídate de tener que recordar y teclear códigos complejos en ambos extremos. Con `sendcroc`, defines tu configuración una vez y las transferencias se vuelven tan simples como:

```bash
# En máquina A
sc mi_archivo.zip

# En máquina B
sc r
```

---

## ✨ Características

- **Configuración Asistida**: Un asistente interactivo te guía en la primera ejecución para crear tu configuración.
- **Comandos Intuitivos y Cortos**: Alias como `sc [archivo]` para enviar y `sc r` para recibir.
- **Manejo Seguro de Secretos**: Utiliza la variable de entorno `CROC_SECRET` para evitar que tus códigos aparezcan en el historial o en la lista de procesos.
- **Soporte para Múltiples Modos**  
  - Transferencias con **Relay Privado** para máxima velocidad y privacidad.  
  - Transferencias con **Relay Público** para envíos rápidos.  
  - Transferencias exclusivas en **Red Local** para no salir a Internet.
- **Automatización con Cron**: Comandos optimizados (`sccron`, `rccron`) y sus alias (`scron`, `rcron`) para facilitar la programación de transferencias.
- **Configuraciones Múltiples**: Usa perfiles de configuración alternativos con la opción `--conf`.
- **Previsualización Segura**: Con `--dry-run`, puedes ver el comando exacto que se ejecutaría sin enviar nada.
- **Auto-actualización**: El comando `sc u` actualiza tanto `croc` como el propio script `sc`.
- **Sin Dependencias Externas** (más allá de `croc`): Es un script de Bash puro.

---

## 🚀 Instalación y Actualización

### 1. Instalar `croc` (Prerrequisito)

```bash
curl https://getcroc.schollz.com | bash
```

> **Nota:** Puede requerir `sudo` si tu usuario no tiene permisos sobre `/usr/local/bin`.

### 2. Instalar `sendcroc`

```bash
sudo curl -L https://raw.githubusercontent.com/uGeek/sendcroc/main/sc -o /usr/bin/sc
sudo chmod +x /usr/bin/sc
```

### Actualización Simplificada

Para actualizar tanto `croc` como `sendcroc` a sus últimas versiones, ejecuta:

```bash
sc u
```

---

## ⚙️ Primera Ejecución y Configuración

Después de la instalación, ejecuta el script por primera vez:

```bash
sc
```

El asistente interactivo te pedirá:

1. Un secreto personal.
2. Opciones de tu relay (si tienes uno).
3. Un secreto global opcional.

Esto creará el archivo de configuración en `~/.config/sendcroc/sendcroc.conf`.

### Ejemplo de `~/.config/sendcroc/sendcroc.conf`

```bash
# Archivo de configuración para sendcroc
CROC_SECRET='mi-frase-secreta-y-muy-larga-imposible-de-adivinar'

# Las opciones de Relay deben ser un array de Bash.
# Cada opción y su valor son elementos separados.
# Ejemplo:
# RELAY=(--pass 'mi-clave-de-relay' --relay '192.168.1.10:9009')
RELAY=(--pass 'xxxx' --relay '140.140.140.140:9009')

# Secreto para transferencias públicas o rápidas sin usar tu relay privado.
CROC_SECRET_GLOBAL="codigo-rapido-y-facil"
```

---

## 💻 Uso

La sintaxis general es:

```bash
sc [COMANDO] [ARGUMENTO] [OPCIONES]
```

| Comando                         | Alias   | Descripción                                                                            |
|---------------------------------|--------|----------------------------------------------------------------------------------------|
| sc [archivo/dir]                | —      | Envía un archivo o directorio con tu configuración privada.                            |
| sc r                            | —      | Recibe un archivo o directorio con tu configuración privada.                           |
| sc t                            | —      | Pide un texto por la terminal y lo envía con la configuración privada.                 |
| sc g [archivo/dir]              | —      | Envía usando la configuración global (pública).                                        |
| sc rg                           | —      | Recibe usando la configuración global.                                                 |
| sc l [archivo/dir]              | —      | Envía un archivo/directorio forzando una conexión local.                               |
| sc lt                           | —      | Envía un texto forzando una conexión local.                                            |
| sccron [ruta]                   | scron  | Comando optimizado para enviar desde un cronjob.                                       |
| rccron                          | rcron  | Comando optimizado para recibir desde un cronjob.                                      |
| lscron [ruta]                   | lscron | Envía desde cronjob forzando conexión local.                                           |
| lrcron                          | lrcron | Recibe desde cronjob forzando conexión local.                                          |
| sc dotfile                      | —      | Muestra el contenido de tu archivo de configuración.                                   |
| sc edotfile                     | —      | Abre el archivo de configuración para editarlo.                                        |
| sc u                            | —      | Actualiza `croc` y el script `sc`.                                                     |
| sc --conf [ruta]                | —      | Usa un archivo de configuración alternativo.                                           |
| sc --dry-run                    | —      | Muestra el comando que se ejecutaría sin ejecutarlo.                                   |

---

## 🛠️ Ejemplos Prácticos

1. **Envío Interactivo**  
   Máquina A (Emisor):  

   ```bash
   sc ~/Proyectos
   ```

   Máquina B (Receptor):  

   ```bash
   sc r
   ```

2. **Previsualizar un Comando con `--dry-run`**  

   ```bash
   sc /datos/backup_semanal.tar.gz --dry-run
   ```

   Salida esperada:

   ```
   Enviando: /datos/backup_semanal.tar.gz
   DRY-RUN: croc '--pass' 'xxxx' '--relay' '140.140.140.140:9009' 'send' '--code' 'mi-frase-secreta-y-muy-larga-imposible-de-adivinar' '/datos/backup_semanal.tar.gz'
   ```

3. **Usar una Configuración Alternativa con `--conf`**  

   ```bash
   sc CV.pdf --conf ~/.config/sendcroc/trabajo.conf
   ```

4. **Automatización con Cron**  
   Edita tu crontab con `crontab -e` y añade:

   - **Emisor (2:00 AM):**

     ```cron
     0 2 * * * /usr/bin/sc scron /home/user/backups/
     ```

   - **Receptor (2:01 AM):**

     ```cron
     1 2 * * * cd /home/user/restores/ && /usr/bin/sc rcron
     ```

---

## 📜 Licencia

Este proyecto está bajo la **Licencia MIT**.

---

## 🙏 Agradecimientos

- A **schollz** por crear y mantener la fantástica herramienta **croc**.  
- A **uGeek** por la idea original y el primer script que sirvió de inspiración para esta versión mejorada.
