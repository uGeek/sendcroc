#!/bin/bash

VERSION="0.2 17/02/2023"


if [ "$1" = "h" ] || [ "$1" = "" ] ; then
echo "
Comando                            Acción
--------------------------------------------------------------------
   sc   archivo/directorio         Enviar un archivo/s o directorio
   sc          t                   Enviar un texto
   rc                              Recibir archivo/s, directorio o texto 

   sc     install    debian        Instalar paquetes para debian
   sc     install    arch          Instalar paquetes para Arch
   sc     install    termux        Instalar paquetes para termux
   sc         qr                   Muestra los códigos qr para importar en tu móvil con termux


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


if [ "$1" = "install" ] && [ "$2" = "arch" ]
     then
sudo curl -L https://raw.githubusercontent.com/uGeek/sendcroc/main/sc \
          -o /usr/bin/sc && sudo chmod +x /usr/bin/sc     
sudo pacman -S croc qrencode
    exit
fi

if [ "$1" = "install" ] && [ "$2" = "debian" ]
     then
sudo curl -L https://raw.githubusercontent.com/uGeek/sendcroc/main/sc \
          -o /usr/bin/sc && sudo chmod +x /usr/bin/sc     
sudo apt install croc qrencode
    exit
fi


if [ "$1" = "qr" ] ; then
echo "Archivo de configuración. sendcroc.conf"
echo "------------------------------------------------"
qrencode -m 2 -t utf8 <<< "$(cat ~/.config/sendcroc/sendcroc.conf)" 
echo "------------------------------------------------"
cat ~/.config/sendcroc/sendcroc.conf
echo "------------------------------------------------"
echo ""
echo ""
echo "Añade a tu archivo ~/.bashrc o ~/.zshrc"
echo "------------------------------------------------"
qrencode -m 2 -t utf8 <<< "$(source ~/.config/sendcroc/sendcroc.conf ; echo "alias rc='croc --yes $RELAY $CODEPASS'")"
echo "------------------------------------------------"
source ~/.config/sendcroc/sendcroc.conf ; echo "alias rc='croc --yes $RELAY $CODEPASS'"
echo "------------------------------------------------"
   exit
fi

if [ "$1" = "t" ] ; then

echo -en "Escribe aquí el texto que quieres enviar: " ; read TEXTO
croc $RELAY send --code $CODEPASS  --text "$TEXTO"
		exit
		fi


croc $RELAY send --code $CODEPASS  "$1"
