#!/bin/bash


VERSION="0.2 01/04/2023"


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


   Si estás dentro de un directorio y quieres enviar todos los archivos o directorios, lo haríamos del siguiente modo:

   sc .
   sc g .

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
    croc --yes send --code $CODEPASSGLOBAL "$2"/* 
		exit
		fi


croc $RELAY send --code $CODEPASS  "$1"/*
