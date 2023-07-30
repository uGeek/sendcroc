#!/bin/bash

VERSION="0.3.4  30/07/2023"
if [ "$1" = "h" ] || [ "$1" = "" ] ; then
echo "
Comando                            Acción
--------------------------------------------------------------------
   sc   archivo/directorio         Enviar un archivo/s o directorio
   sc          g                   Enviar con el relay global
   sc          t                   Enviar un texto
   sc          tg                  Enviar un texto desde realy global
   rc                              Recibir archivo/s, directorio o texto
   rcg                             Recibir archivo/s, directorio o texto del servidor global
   sc          dotfile             Ver archivo que hay que copiar en .bashrc o .zshrc
   sc          edotfile            Editar dotfile

   sc          sccron              Enviar por cron
   sc          rccron              Recibir por cron


   Ejemplos:
   Enviar el contenido de un directorio local a otro remoto, con archivos nuevos, sin eliminar los antiguos.

   Emisor:   Enviar desde el directorio HOME, el directorio Documentos:
      sc Documentos/ 

   Receptor: Situate en el directorio HOME y ejecuta el comando:
      rc




   Si estás dentro de un directorio y quieres enviar todos los archivos o directorios, lo haríamos del siguiente modo:

   sc .
   sc g .




   Ejemplos:

   Enviar-Recibir con cron:
   Emisor   =  00 09 * * * ( sc sccron /home/ubuntu/Descargas/ )
   Receptor =  01 09 * * * ( cd /home/ubuntu/Descargas/ ; sc rccron )
sendcroc version $VERSION
"
    exit
		fi





mkdir -p ~/.config/sendcroc

if [ -f ~/.config/sendcroc/sendcroc.conf ];
then
    source ~/.config/sendcroc/sendcroc.conf
else
    echo -en "Introduce el código que quieres utilizar. Puedes aporrear el teclado sin miedo: " ; read -e CODE
    echo "CODEPASS='$CODE'" >  ~/.config/sendcroc/sendcroc.conf
    echo -en "Añade información de tu servidor relay, si no vas a utilizar los servidores de croc: " ; read -e RELAYPASS
    echo "RELAY='$RELAYPASS'" >>  ~/.config/sendcroc/sendcroc.conf
    echo "Copia este alias en .bashrc o .zshrc y reinicia tu terminal"
    echo "alias rc='croc --yes $RELAYPASS $CODE'"
    exit
    fi

source ~/.config/sendcroc/sendcroc.conf



if [ "$1" = "install" ] && [ "$2" = "termux" ]
then
    ### Termux
    pkg upgrade
    pkg install croc -y
    termux-setup-storage
    mkdir -p $HOME/.config/sendcroc/
    curl -L https://raw.githubusercontent.com/uGeek/sendcroc/main/sc      -o $HOME/.config/sendcroc/sc
    chmod +x $HOME/.config/sendcroc/sc
    clear
    exit
fi



if [ "$1" = "t" ] ; then

echo -en "Escribe aquí el texto que quieres enviar: " ; read TEXTO
croc $RELAY send --code $CODEPASS  --text "$TEXTO"
		exit
		fi


if [ "$1" = "tg" ] ; then

echo -en "Escribe aquí el texto que quieres enviar: " ; read TEXTO
croc  send --code $CODEPASSGLOBAL  --text "$TEXTO"
		exit
		fi



if [ "$1" = "g" ] ; then
    croc --yes send --code $CODEPASSGLOBAL "$2"
		exit
fi


if [ "$1" = "dotfile" ] ; then
echo "Archivo de configuración ~/.config/sendcroc/sendcroc.conf"
echo "---------------------------------------------------------"
cat ~/.config/sendcroc/sendcroc.conf
echo ""
echo "---------------------------------------------------------"
echo ""
echo ""
echo ""
echo "Copia este texto en tu archivo .bashrc o .zshrc"
echo "-----------------------------------------------"
echo "alias rc='croc --yes --overwrite $(echo $RELAY | cut -d" " -f3-4) $(echo $RELAY | cut -d" " -f1-2) $CODEPASS'"
echo "      --overwrite = Elimina esta opción si no quieres que te pregunte cada vez que tenga que sobreescribir"
echo "alias rcg='croc --yes  $CODEPASSGLOBAL'"
echo ""
echo ""
echo "Enviar con cron"
echo "---------------"
echo "croc --ignore-stdin $RELAY send --code $CODEPASS  [RUTA ARCHIVO O DIRECTORIO]"
    		exit
fi


if [ "$1" = "edotfile" ] ; then
nano 	~/.config/sendcroc/sendcroc.conf
    exit
fi

if [ "$1" = "sccron" ] ; then
    echo "    croc --ignore-stdin croc $RELAY send --code $CODEPASS  "$2""
    croc --ignore-stdin $RELAY send --code $CODEPASS  "$2"
    exit
fi


if [ "$1" = "rccron" ] ; then
		echo "Ejecutando el comando:"
		echo "----------------------"
echo "croc  --yes --ignore-stdin --overwrite $(echo $RELAY | cut -d" " -f3-4) $(echo $RELAY | cut -d" " -f1-2) $CODEPASS"
echo ""
croc  --yes --ignore-stdin --overwrite  $(echo $RELAY | cut -d" " -f3-4) $(echo $RELAY | cut -d" " -f1-2) $CODEPASS
    exit
fi

croc $RELAY send --code $CODEPASS  "$1"
