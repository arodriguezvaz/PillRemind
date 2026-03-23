# Estudo preliminar

## 1. Descrición do proxecto
Este proxecto ten como finalidade desenvolver unha aplicación de recordatorios
para a toma de medicación. A aplicación permitirá aos usuarios xestionar de
maneira sinxela e eficaz os seus tratamentos, evitando esquecementos e mellorando
o seguimento dos mesmos. Para iso, o proxecto desenvolverase en diferentes fases
que garantirán unha correcta planificación, implementación e avaliación do sistema.

### 1.1. Xustificación do proxecto
A idea deste proxecto xorde dunha problemática común nos fogares: esquecer tomar
a medicación ou non xestionar correctamente o inventario dos medicamentos. Isto é
especialmente frecuente en persoas que deben seguir tratamentos continuos ou
complexos.

É habitual que os usuarios dubiden sobre se xa tomaron unha dose, que esquezan
tomala ou que, de maneira inesperada, queden sen medicación dispoñible. Estas
situacións poden afectar negativamente á saúde e á eficacia do tratamento.

Con esta aplicación, os usuarios poderán realizar un seguimento completo do seu
tratamento, mellorando a adherencia ao mesmo e reducindo erros na toma da
medicación.

### 1.2. Funcionalidades do proxecto
- Creación, edición e eliminación de recordatorios, adaptados a diferentes tipos
  de tratamentos (puntuais, periódicos ou crónicos).
- Configuración de frecuencia e horarios personalizados para cada medicación.
- Diferentes tipos de medicación con unidades específicas (comprimidos, xarabes,
  cremas, inxeccións, etc.).
- Sistema de notificacións para lembrar a toma da medicación.
- Rexistro das tomas realizadas, permitindo consultar o historial do tratamento.
- Xestión do stock de medicamentos, notificando ao usuario cando a medicación
  estea a piques de esgotarse.
- Xestión de múltiples perfís, orientada a persoas que coidan ou administran a
  medicación de terceiros (familiares, persoas maiores, etc.).
- Interface sinxela e intuitiva, adaptada a todo tipo de usuarios.

### 1.3. Estudo de necesidades
Na actualidade existen aplicacións no mercado como MyTherapy ou Medisafe que
ofrecen funcionalidades similares. Non obstante, moitas destas aplicacións
presentan limitacións como interfaces complexas, exceso de funcionalidades pouco
intuitivas ou falta de adaptación a determinados perfís de usuario.

Este proxecto busca cubrir estas carencias ofrecendo unha solución máis sinxela,
accesible e centrada na experiencia do usuario, especialmente para persoas maiores
ou con pouca experiencia tecnolóxica.

### 1.4. Persoas destinatarias
A aplicación está dirixida principalmente a:

- Persoas maiores que necesitan apoio na xestión da súa medicación.
- Coidadores ou familiares responsables da medicación doutras persoas.

Non obstante, pode ser utilizada por calquera persoa que requira seguir un
tratamento de forma continuada, como xoves que toman suplementos ou persoas de
mediana idade que comezan a tomar medicación de forma habitual.

### 1.5. Modelo de negocio
O modelo de negocio da aplicación baséase nun enfoque freemium:

- Versión gratuíta con funcionalidades básicas (recordatorios e xestión simple
  de medicación).
- Versión premium con funcionalidades avanzadas (múltiples perfís e
  sincronización na nube).

Ademais, poderían explorarse outras fontes de ingresos como:

- Integración con servizos de saúde ou farmacias.
- Publicidade non intrusiva na versión gratuíta.

## 2. Requirimentos

Para o desenvolvemento da aplicación utilizarase Jetpack Compose. A decisión de
usar este framework débese a que está baseado en Kotlin, linguaxe que levo dous
anos estudando e que segue sendo o estándar actual para o desenvolvemento Android,
a diferenza de XML, que é a forma tradicional pero que Google xa non recomenda para
novos proxectos. Ademais, na empresa de prácticas estou a traballar con React, polo
que a xestión de estados de Compose é un concepto co que xa estou familiarizado.

Para o almacenamento de datos utilizarase:

- Room: para o gardado de datos en local (medicacións, recordatorios e historial
  de tomas).
- DataStore: para o gardado de preferencias do usuario.
- Firebase Authentication e Cloud Firestore: para as funcionalidades premium,
  permitindo o inicio de sesión e a sincronización de datos na nube entre
  dispositivos.