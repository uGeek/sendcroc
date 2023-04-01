# sendcroc




## Instalación

### debian


```
bash <(curl https://raw.githubusercontent.com/uGeek/sendcroc/main/sc) install debian
```


### Arch


```
bash <(curl https://raw.githubusercontent.com/uGeek/sendcroc/main/sc) install arch
```

### termux


```
bash <(curl https://raw.githubusercontent.com/uGeek/sendcroc/main/sc) install termux
```




## Instalación otras distros
### Instala croc


```
curl https://getcroc.schollz.com | bash
```

### Instala sendcroc en Debian, Arch, Ubuntu, Manjaro,...
```
sudo curl -L https://raw.githubusercontent.com/uGeek/sendcroc/main/sc \
          -o /usr/bin/sc && sudo chmod +x /usr/bin/sc
```


## Instalación en Termux (Android)

```
bash <(curl https://raw.githubusercontent.com/uGeek/sendcroc/main/sc) install termux
```





## Ejemplo de alias


```
alias rc='croc --yes --relay [IP] --pass xxxxxxxxxxxx yyyyyyyyyyyy'
```

- yyyyyyyyyyyy = Code Pass




## Archivo de Configuración


```
CODEPASS="yyyyyyyyyyyy"
CODEPASSGLOBAL="zzzzzzzzzzzz"
RELAY="--pass xxxxxxxxxxxx  --relay [IP]"
#RELAY=""   # utilizar servidor relay de croc
```



## Croc
- https://github.com/schollz/croc

