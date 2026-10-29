# Manuais

> **Nota:** este documento describe unha versión anterior da aplicación, con
> Supabase, autenticación e sincronización na nube. A versión actual é local-only
> e garda todos os datos en Room no dispositivo. Consérvase como documentación
> histórica.

## Manual técnico do proxecto

Cando o usuario accede á aplicación, aparece na pestana de perfís, na que pode observar un botón de engadir perfil e un botón de axustes.

<img src="../img/profiles_screen.png" alt="Pantalla de perfís" width="300"/>

A aplicación é completamente funcional sen conexión, pero está limitada a un perfil. A partir de aquí, centrarémonos en explicar o funcionamento da aplicación cun usuario rexistrado, aínda que é importante deixar isto claro.

Se o usuario preme o botón de axustes, accede á pantalla de axustes onde pode:

- Iniciar sesión ou rexistrarse
- Activar o modo escuro ou claro
- Acceder aos enlaces legais

<img src="../img/settings_screen_1.png" alt="Pantalla de axustes" width="300"/>

Se se preme o interruptor de modo escuro, actívase o modo escuro.

<img src="../img/modo osucro.png" alt="Modo escuro activado" width="300"/>

Se se preme en iniciar sesión, pásase á pestana de inicio de sesión. Se non ten conta, debe premer en rexistrarse; doutro xeito, recibirá un erro na pantalla.

<img src="../img/inicio_sesion.png" alt="Pantalla de inicio de sesión" width="300"/>

Ao premer o texto de rexistro, accédese á pantalla de rexistro, dende a que se pode crear unha conta na plataforma.

<img src="../img/rexistro1.png" alt="Pantalla de rexistro" width="300"/>

Tras inserir un enderezo de correo electrónico válido e un contrasinal de máis de 6 caracteres, o usuario rexístrase, a sesión iníciase e retórnase á pestana de perfís.

<img src="../img/rexistro2.png" alt="Formulario de rexistro cuberto" width="300"/>

<img src="../img/profiles_screen.png" alt="Pantalla de perfís tras o rexistro" width="300"/>

Para crear un perfil, prémese o botón de máis e insírese a información do perfil. A imaxe selecciónase a través do selector de imaxes de Android. Se se desexa editar, simplemente se preme outra vez na imaxe e permítese cambiala.

<img src="../img/crear_perfil_1.png" alt="Formulario de creación de perfil" width="300"/>
<img src="../img/selector_imagenes_perfiles.jpg" alt="Selector de imaxes de Android" width="300"/>
<img src="../img/crear_perfil_3.png" alt="Perfil con imaxe seleccionada" width="300"/>

Ao premer Gardar, retórnase á pestana de perfís. Se se intenta crear outro perfil, aparecerá esta mensaxe de erro.

<img src="../img/error_perfiles_1.png" alt="Erro ao tentar crear un segundo perfil sen premium" width="300">

Para poder crear máis perfís, é necesario activar o modo premium (interruptor que simula a función), que só está dispoñible para usuarios rexistrados. Se é un usuario convidado, non pode activalo.

<img src="../img/activar_premium.png" alt="Interruptor para activar o modo premium" width="300">

Agora o usuario pode crear ata 3 perfís. Se intenta crear máis, tamén obtén un erro.

<img src="../img/error_perfiles_2.png" alt="Erro ao superar o límite de perfís premium" width="300">

Se se preme en xestionar nunha das tarxetas dos perfís, accédese á xestión da medicación do perfil seleccionado.

<img src="../img/home.png" alt="Pantalla de inicio con lista de medicamentos do perfil" width="300">

Ao premer o botón de máis, permítese engadir un medicamento novo e accédese á pantalla de creación de medicamentos. O botón de gardar non se activa ata que o usuario cobre todos os campos obrigatorios, como se pode apreciar na cuarta foto. Os campos obrigatorios son o nome, a dose, a unidade, a data de inicio (se é crónico) ou a data de fin, a periodicidade e, como mínimo, unha hora de toma.

<img src="../img/creacion_de_medicamentos.png" alt="Pantalla de creación dun novo medicamento" width="300">
<img src="../img/hour.png" alt="Selector de hora de toma" width="300">
<img src="../img/date.png" alt="Selector de data de inicio ou fin" width="300">
<img src="../img/faltan_campos.png" alt="Botón de gardar desactivado por campos obrigatorios sen cubrir" width="300">

Unha vez creado, xéranse as tomas do día dese medicamento. Nestas imaxes pódense apreciar tomas atrasadas (vermello), tomas que se poden tomar (azul) ou xa tomadas (gris).

<img src="../img/tomas.png" alt="Lista de tomas do día con estados: atrasada, pendente e tomada" width="300">
<img src="../img/tomas1.png" alt="Detalle das tomas do día con código de cores" width="300">

A información dos perfís pódese editar accedendo á pantalla de perfil, ou eliminar premendo en eliminar. Do mesmo xeito, accedendo á información concreta dun medicamento, se se preme o botón de eliminar, móstrase un diálogo de confirmación.

<img src="../img/infomedi.png" alt="Pantalla de información dun medicamento" width="300">
<img src="../img/infoperfil.png" alt="Pantalla de información e edición dun perfil" width="300">
<img src="../img/dialogo_1.png" alt="Diálogo de confirmación de eliminación" width="300">
<img src="../img/dialogo_2.png" alt="Diálogo de confirmación tras premer eliminar" width="300">

Dende a pestana de medicamentos pódese facer unha busca por nome.

<img src="../img/medicamentos.png" alt="Pestana de listado de medicamentos" width="300">
<img src="../img/busqueda1.png" alt="Resultado da busca dun medicamento por nome" width="300">



## Instalación

Para a instalación da aplicación empregarase o ficheiro APK correspondente. A súa execución realizarase a través da plataforma [Appetize](https://appetize.io/), xa que permite a execución rápida da aplicación sen necesidade de instalación nativa nun dispositivo físico.

## Melloras futuras

- Incorporar un sistema de xestión de inventario que permita controlar a cantidade dispoñible de cada medicamento e emitir avisos cando estea próximo a esgotarse.
- Engadir unha vista de histórico máis completa, incluíndo un calendario de tomas e estatísticas sobre a adherencia aos tratamentos.
- Mellorar a robustez da sincronización mediante a integración de WorkManager, permitindo sincronizar periodicamente a información coa nube e reducindo o risco de inconsistencias entre dispositivos. Ademais da implementación de Storage para permitir que as imaxes tamén se descarguen entre dispositivos.