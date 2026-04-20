# Análisis: Requisitos del sistema

## Funcionalidades

### 1. Alta de medicamento
- **Actor:** Usuario (invitado o registrado)
- **Datos de entrada:** Nombre del medicamento, dosis, frecuencia, hora(s)
- **Proceso:**  
  El usuario introduce los datos del medicamento. La aplicación valida los campos y guarda la información en la base de datos local.
- **Datos de salida:** Medicamento registrado en el sistema

---

### 2. Modificación de medicamento
- **Actor:** Usuario (invitado o registrado)
- **Datos de entrada:** Nuevos datos del medicamento
- **Proceso:**  
  El usuario selecciona un medicamento existente y modifica sus datos. La aplicación valida los cambios y actualiza la información.
- **Datos de salida:** Medicamento actualizado

---

### 3. Baja de medicamento
- **Actor:** Usuario (invitado o registrado)
- **Datos de entrada:** Identificador del medicamento
- **Proceso:**  
  El usuario selecciona un medicamento y lo elimina. La aplicación lo borra de la base de datos local.
- **Datos de salida:** Medicamento eliminado

---

### 4. Registro de usuario premium
- **Actor:** Usuario
- **Datos de entrada:** Correo electrónico, contraseña, datos de pago
- **Proceso:**  
  El usuario crea una cuenta y realiza el pago de la suscripción. La aplicación valida los datos y crea el usuario en la base de datos en línea.
- **Datos de salida:** Cuenta premium creada

---

### 5. Migración de datos a cuenta premium
- **Actor:** Sistema
- **Datos de entrada:** Datos locales del usuario
- **Proceso:**  
  La aplicación recoge los datos almacenados en el dispositivo y los envía a la base de datos en la nube, asociándolos a la cuenta del usuario.
- **Datos de salida:** Datos sincronizados en la nube

---

### 6. Sincronización en la nube
- **Actor:** Sistema
- **Datos de entrada:** Datos locales y datos almacenados en la nube
- **Proceso:**  
  La aplicación compara los datos locales con los de la nube y los actualiza para mantener la consistencia entre dispositivos.
- **Datos de salida:** Datos sincronizados

---
### 7 Gestión de perfiles
#### 7.1. Creación de perfil
- **Actor:** Usuario premium
- **Datos de entrada:** Nombre y datos del perfil (persona)
- **Proceso:**  
  El usuario premium crea un nuevo perfil asociado (por ejemplo, un familiar). La aplicación valida los datos y guarda el perfil en la base de datos.
- **Datos de salida:** Perfil creado y vinculado a la cuenta



#### 7.2. Modificación de perfil
- **Actor:** Usuario premium
- **Datos de entrada:** Nuevos datos del perfil
- **Proceso:**  
  El usuario premium selecciona un perfil existente y modifica sus datos. La aplicación valida los cambios y actualiza la información.
- **Datos de salida:** Perfil actualizado



#### 7.3. Eliminación de perfil
- **Actor:** Usuario premium
- **Datos de entrada:** Identificador del perfil
- **Proceso:**  
  El usuario premium selecciona un perfil y lo elimina. La aplicación lo borra de la base de datos, incluyendo todos los datos asociados.
- **Datos de salida:** Perfil eliminado

---

### 8. Notificación de medicación
- **Actor:** Sistema
- **Datos de entrada:** Hora y datos del medicamento
- **Proceso:**  
  El sistema comprueba los recordatorios programados y envía una notificación al usuario en el momento indicado. Es omportantne tener en cuenta las diferentes casusiticas de toma de medicación
- **Datos de salida:** Notificación enviada

---

### 9. Confirmación de toma de medicación
- **Actor:** Usuario
- **Datos de entrada:** Acción de confirmación
- **Proceso:**  
  El usuario indica que ha tomado la medicación y la aplicación guarda el registro.
- **Datos de salida:** Registro de toma almacenado

---

### 10. Modificación de inventario
- **Actor:** Sistema/Usuario
- **Datos de entrada:** Confirmación de toma de medicación o notificación de compra de nueva caja
- **Proceso:**  
  El sistema actualiza automáticamente el inventario de medicamentos cuando el usuario confirma una toma o notifica la compra de nuevas dosis.
- **Datos de salida:** Inventario actualizado

---

## Tipos de usuarios

| Usuario | Descripción | Acceso / Funcionalidades |
|---------|-------------|--------------------------|
| Anónimo / Invitado | Usuario que utiliza la aplicación sin registro. | Funcionalidades básicas: crear, editar y eliminar recordatorios. Sin sincronización en la nube.|
| Registrado (gratis) | Usuario que registró su correo | Funcionalidades básicas: crear, editar y eliminar recordatorios. Sin sincronización en la nube. Es la puerta de entrada al modelopreimum, se le podría ofreer un descueto por los primeros meses |
| Premium | Usuario con cuenta de pago. | Todas las funcionalidades gratuitas, gestión de múltiples perfiles, sincronización en la nube, historial y estadísticas. Cuenta obligatoria para asociar suscripción y recuperar datos. |

---

## Normativa

La aplicación gestiona datos relacionados con la salud de los usuarios, lo que la sitúa bajo un marco normativo estricto tanto a nivel europeo como español. A continuación se detallan las principales normas aplicables.

### 1. Protección de datos personales

#### RGPD — Reglamento (UE) 2016/679
El **Reglamento General de Protección de Datos**, en vigor desde el 25 de mayo de 2018, es la norma europea de referencia para el tratamiento de datos personales. Los datos sobre medicación y salud están clasificados como **datos de categoría especial** (art. 9 RGPD), lo que exige un nivel de protección reforzado. Entre las obligaciones principales se encuentran:

- Obtener el **consentimiento explícito** del usuario antes de tratar sus datos de salud.
- Aplicar el principio de **privacidad desde el diseño** (*privacy by design*) y **por defecto** (*privacy by default*).
- Informar al usuario de forma clara sobre la finalidad del tratamiento, los destinatarios de los datos y sus derechos.
- Garantizar los derechos de acceso, rectificación, supresión, portabilidad y oposición.
- Notificar las brechas de seguridad a la AEPD en un plazo máximo de 72 horas.

#### LOPDGDD — Ley Orgánica 3/2018
La **Ley Orgánica de Protección de Datos Personales y Garantía de los Derechos Digitales** adapta el RGPD al ordenamiento jurídico español. Complementa el RGPD con disposiciones específicas sobre el tratamiento de datos en el ámbito nacional, el régimen sancionador y los derechos digitales de los ciudadanos.

---

### 2. Comercio electrónico y suscripciones

#### LSSI-CE — Ley 34/2002
La **Ley de Servicios de la Sociedad de la Información y de Comercio Electrónico** regula las actividades comerciales realizadas a través de medios electrónicos. Al ofrecer una suscripción de pago (cuenta premium), la aplicación queda sujeta a esta ley, que exige:

- Informar de forma clara sobre la identidad del prestador del servicio, precios y condiciones de contratación.
- Obtener el consentimiento previo del usuario antes de proceder a la contratación.
- Facilitar al usuario el acceso a los términos y condiciones antes de la compra.
- Proporcionar confirmación de la transacción realizada.

Actualizada mediante el **Real Decreto Legislativo 9/2024** para adaptarse al Reglamento europeo de Servicios Digitales (DSA).

#### Directiva (UE) sobre pagos digitales — PSD2
Aplicable a las transacciones de pago realizadas para activar la suscripción premium. Exige la implementación de **autenticación reforzada de clientes** (SCA) y prohíbe cargos adicionales sobre pagos con tarjeta de crédito o débito.

---

### 3. Normativa sanitaria y de salud digital

#### Reglamento (UE) 2025/327 — Espacio Europeo de Datos de Salud (EHDS)
Aprobado el 11 de febrero de 2025, establece el marco europeo para el uso y la interoperabilidad de los datos de salud electrónicos. Aunque está orientado principalmente a sistemas de historia clínica electrónica (HCE), sienta las bases para las aplicaciones de salud digital en cuanto a:

- Derechos de los ciudadanos a acceder y gestionar sus propios datos de salud.
- Requisitos de interoperabilidad y seguridad para aplicaciones que manejen datos sanitarios.
- Obligación de distinguir claramente entre datos introducidos por el propio usuario y los registrados por profesionales sanitarios.

#### Anteproyecto de Ley de Salud Digital (España, 2025)
El Ministerio de Sanidad ha iniciado la elaboración de esta ley para adaptar el ordenamiento español al Reglamento EHDS. Afectará a las **aplicaciones móviles de salud**, estableciendo requisitos de interoperabilidad, seguridad para los pacientes y acceso equitativo a la innovación digital. Las aplicaciones de recordatorio de medicación podrían quedar sujetas a sus disposiciones una vez aprobada.

---

### 5. Protección del consumidor

#### Real Decreto Legislativo 1/2007 — Ley General para la Defensa de los Consumidores y Usuarios
Aplicable a los usuarios que contraten la suscripción premium. Regula el derecho de desistimiento en compras online (14 días naturales), la obligación de informar sobre precios y condiciones, y la resolución de reclamaciones.

