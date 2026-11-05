# Proyecto fin de ciclo

 **PILLRemind**

## Descarga

[![Última versión](https://img.shields.io/github/v/release/arodriguezvaz/PillRemind?label=versi%C3%B3n&color=2ea44f)](https://github.com/arodriguezvaz/PillRemind/releases/latest)

A aplicación distribúese como APK desde as [releases de GitHub](https://github.com/arodriguezvaz/PillRemind/releases/latest).
Non está publicada en Google Play.

- Require Android 8.0 (API 26) ou superior.
- Descarga o ficheiro `PillRemind-x.y.z.apk` e ábreo. Se o sistema o pide, permite a
  instalación desde fontes descoñecidas para o navegador ou o xestor de ficheiros.
- As actualizacións instálanse sobre a versión anterior e conservan os datos locais,
  sempre que o APK estea asinado coa mesma clave.
- A aplicación non ten permiso de internet: os datos só saen do dispositivo nas copias
  de seguridade que exportes manualmente desde Axustes.

## Descrición


A aplicación desenvolvida é un sistema de recordatorio de medicación cuxo obxectivo principal é axudar ás persoas a 
xestionar de maneira sinxela e segura os seus tratamentos (medicamentos, suplementación, etc.). Está pensada tanto 
para usuarios que controlan a súa propia medicación como para aqueles que deben administrar ou supervisar a doutras persoas, 
como familiares ou persoas dependentes.

A aplicación permite rexistrar medicamentos indicando datos como o nome, a dose, a frecuencia e os horarios de toma. 
A partir desta información, o sistema xera recordatorios e notificacións no momento adecuado para evitar esquecementos. 
Ademais, o usuario pode confirmar a toma da medicación, o que permite levar un control do cumprimento do tratamento.

Outro dos aspectos importantes é a xestión de perfís, que permite organizar a medicación de diferentes persoas dentro 
dun mesmo dispositivo. A aplicación funciona integramente de forma local: non require conta nin conexión a internet,
e os datos gárdanse só no dispositivo. Tamén conta con funcionalidades de modificación e eliminación de medicamentos,
así como recordatorios e notificacións para cada toma.

Cada perfil pode gardar información médica opcional: data de nacemento, sexo, grupo sanguíneo, alerxias, notas e
número de tarxeta sanitaria. Estes datos móstranse desde «Ver perfil» e inclúense na copia de seguridade local. A opción
«Ver estadísticas» abre un resumo de adherencia con períodos de 7, 30 e 90 días, gráfico semanal, desglose por
medicamento e historial de tomas.

## Almacenamento local

Todos os datos (perfís, medicamentos, horarios e tomas) gárdanse nunha base de datos Room no propio dispositivo.
A aplicación non envía información a servidores externos nin sincroniza datos entre dispositivos.

Desde Axustes pódese exportar e importar unha copia de seguridade local que inclúe perfís, medicamentos,
horarios, historial e imaxes. A copia pódese protexer cun contrasinal; a importación substitúe todos os
datos actuais.

## Sobre a autoría

O autor deste proxecto é Andrés Rodríguez Vázquez, estudante de 2º DAM no IES San Clemente. 
Está interesado no ámbito tecnolóxico e científico. Os seus puntos fortes son a detección 
de problemas da vida real, o traballo en equipo e o pensamento lóxico.

As tecnoloxías que máis domina son Kotlin e Java. Ademais, durante as prácticas en empresa 
traballou con React, o que lle permitiu familiarizarse co desenvolvemento de interfaces de usuario reactivas, 
como Jetpack Compose. Debido a isto, escolleu realizar unha aplicación en Kotlin e Jetpack Compose, 
xa que son tecnoloxías modernas e en uso, con gran respaldo por parte de Google e da comunidade de Kotlin Multiplatform.

O motivo da realización deste proxecto foi persoal: decatouse de que con frecuencia se 
esquece de tomar a medicación e non quería que outras empresas tivesen información sobre a súa medicación. 
Por iso, decidiu crear unha aplicación que lle resultase útil a nivel persoal e familiar.

## Publicación

O APK de release compílase, asínase e publícase automaticamente con GitHub Actions
(`.github/workflows/release.yml`). Para lanzar unha versión:

```bash
git tag v1.0.0
git push origin v1.0.0
```

O workflow toma a versión da etiqueta, usa o número de execución como `versionCode` e
adjunta `PillRemind-1.0.0.apk` á release. Tamén se pode executar a man desde a pestana
Actions: nese caso só se sobe o APK como artefacto, sen crear release.

A firma necesita catro secretos no repositorio (Settings → Secrets and variables → Actions):
`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` e `KEY_PASSWORD`. En local pódese
crear `PillRemind/keystore.properties` coas claves `storeFile`, `storePassword`, `keyAlias`
e `keyPassword`; tanto ese ficheiro como o keystore están en `.gitignore`. Se o repositorio
limita o token a só lectura, hai que activar «Read and write permissions» en
Settings → Actions → General.

## Licencia

- [Ficheiro License](LICENSE)

## Índice

1. [Estudo preliminar](doc/templates/1_estudo_preliminar.md)
2. [Análise](doc/templates/2_analise.md)
   - [Planificación](doc/templates/21_planificacion.md)
   - [Orzamento](doc/templates/22_orzamento.md)
3. [Deseño](doc/templates/3_deseño.md)
4. [Codificación e probas](doc/templates/4_codificacion_probas.md)
5. [Manuais do proxecto](doc/templates/5_manuais.md)
   - [Referencias](doc/templates/51_referencias.md)
