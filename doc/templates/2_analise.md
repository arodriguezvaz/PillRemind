# Análise: Requisitos do sistema

## Funcionalidades

### 1. Alta de medicamento
- **Actor:** Usuario (invitado ou rexistrado)  
- **Datos de entrada:** Nome do medicamento, dose, frecuencia, hora(s)  
- **Proceso:**  
  O usuario introduce os datos do medicamento. A aplicación valida os campos e garda a información na base de datos local.  
- **Datos de saída:** Medicamento rexistrado no sistema  

---

### 2. Modificación de medicamento
- **Actor:** Usuario (invitado ou rexistrado)  
- **Datos de entrada:** Novos datos do medicamento  
- **Proceso:**  
  O usuario selecciona un medicamento existente e modifica os seus datos. A aplicación valida os cambios e actualiza a información.  
- **Datos de saída:** Medicamento actualizado  

---

### 3. Baixa de medicamento
- **Actor:** Usuario (invitado ou rexistrado)  
- **Datos de entrada:** Identificador do medicamento  
- **Proceso:**  
  O usuario selecciona un medicamento e elimínao. A aplicación bórrano da base de datos local.  
- **Datos de saída:** Medicamento eliminado  

---

### 4. Rexistro de usuario premium
- **Actor:** Usuario  
- **Datos de entrada:** Correo electrónico, contrasinal, datos de pago  
- **Proceso:**  
  O usuario crea unha conta e realiza o pago da subscrición. A aplicación valida os datos e crea o usuario na base de datos en liña.  
- **Datos de saída:** Conta premium creada  

---

### 5. Migración de datos a conta premium
- **Actor:** Sistema  
- **Datos de entrada:** Datos locais do usuario  
- **Proceso:**  
  A aplicación recolle os datos almacenados no dispositivo e envíaos á base de datos na nube, asociándoos á conta do usuario.  
- **Datos de saída:** Datos sincronizados na nube  

---

### 6. Sincronización na nube
- **Actor:** Sistema  
- **Datos de entrada:** Datos locais e datos almacenados na nube  
- **Proceso:**  
  A aplicación compara os datos locais cos da nube e actualízaos para manter a consistencia entre dispositivos.  
- **Datos de saída:** Datos sincronizados  

---

### 7. Xestión de perfís (clientes)
- **Actor:** Usuario premium  
- **Datos de entrada:** Nome e datos do perfil (persoa)  
- **Proceso:**  
  O usuario premium pode crear, modificar ou eliminar perfís asociados (por exemplo, familiares).  
- **Datos de saída:** Perfís actualizados  

---

### 8. Notificación de medicación
- **Actor:** Sistema  
- **Datos de entrada:** Hora e datos do medicamento  
- **Proceso:**  
  O sistema comproba os recordatorios programados e envía unha notificación ao usuario no momento indicado.  
- **Datos de saída:** Notificación enviada  

---

### 9. Confirmación da toma de medicación
- **Actor:** Usuario  
- **Datos de entrada:** Acción de confirmación  
- **Proceso:**  
  O usuario indica que tomou a medicación e a aplicación garda o rexistro.  
- **Datos de saída:** Rexistro de toma almacenado  

---

## Tipos de usuarios

| Usuario | Descrición | Acceso / Funcionalidades |
|---------|------------|--------------------------|
| Anónimo / Invitado | Usuario que utiliza a aplicación sen rexistro. | Crear e xestionar recordatorios localmente no dispositivo; consulta de información básica. Sen sincronización nin gardado na nube. |
| Rexistrado (gratis) | Usuario con conta gratuíta. | Funcionalidades básicas: crear, editar e eliminar recordatorios; almacenamento seguro asociado á conta; migración opcional a premium. Sen sincronización na nube. |
| Premium | Usuario con conta de pago. | Todas as funcionalidades gratuítas, xestión de múltiples perfís, sincronización na nube, historial e estatísticas. Conta obrigatoria para asociar a subscrición e recuperar datos. |
| Bloqueado | Usuario ao que se lle restrinxiu o acceso. | Acceso limitado ou nulo segundo o motivo do bloqueo. |
| Administrador | Usuario con permisos avanzados para xestión e supervisión. | Xestión de usuarios, supervisión do sistema, configuración global e xestión de contido ou integracións externas. |

---

## Normativa

A aplicación xestiona datos relacionados coa saúde dos usuarios, o que a sitúa baixo un marco normativo estrito tanto a nivel europeo como español. A continuación detállanse as principais normas aplicables.

### 1. Protección de datos persoais

#### RGPD — Regulamento (UE) 2016/679  
O **Regulamento Xeral de Protección de Datos**, en vigor desde o 25 de maio de 2018, é a norma europea de referencia para o tratamento de datos persoais. Os datos sobre medicación e saúde están clasificados como **datos de categoría especial** (art. 9 RGPD), o que esixe un nivel de protección reforzado. Entre as obrigas principais atópanse:

- Obter o **consentimento explícito** do usuario antes de tratar os seus datos de saúde  
- Aplicar o principio de **privacidade desde o deseño** (*privacy by design*) e **por defecto** (*privacy by default*)  
- Informar ao usuario de forma clara sobre a finalidade do tratamento, os destinatarios dos datos e os seus dereitos  
- Garantir os dereitos de acceso, rectificación, supresión, portabilidade e oposición  
- Notificar as brechas de seguridade á AEPD nun prazo máximo de 72 horas  

#### LOPDGDD — Lei Orgánica 3/2018  
A **Lei Orgánica de Protección de Datos Persoais e Garantía dos Dereitos Dixitais** adapta o RGPD ao ordenamento xurídico español. Complementa o RGPD con disposicións específicas sobre o tratamento de datos no ámbito nacional, o réxime sancionador e os dereitos dixitais da cidadanía.  

---

### 2. Comercio electrónico e subscricións

#### LSSI-CE — Lei 34/2002  
A **Lei de Servizos da Sociedade da Información e de Comercio Electrónico** regula as actividades comerciais realizadas a través de medios electrónicos. Ao ofrecer unha subscrición de pago (conta premium), a aplicación queda suxeita a esta lei, que esixe:

- Informar de forma clara sobre a identidade do prestador do servizo, prezos e condicións de contratación  
- Obter o consentimento previo do usuario antes de proceder á contratación  
- Facilitar ao usuario o acceso aos termos e condicións antes da compra  
- Proporcionar confirmación da transacción realizada  

Actualizada mediante o **Real Decreto Lexislativo 9/2024** para adaptarse ao Regulamento europeo de Servizos Dixitais (DSA).  

#### Directiva (UE) sobre pagos dixitais — PSD2  
Aplicable ás transaccións de pago realizadas para activar a subscrición premium. Esixe a implementación de **autenticación reforzada de clientes** (SCA) e prohibe cargos adicionais sobre pagos con tarxeta de crédito ou débito.  

---

### 3. Normativa sanitaria e de saúde dixital

#### Regulamento (UE) 2025/327 — Espazo Europeo de Datos de Saúde (EHDS)  
Aprobado o 11 de febreiro de 2025, establece o marco europeo para o uso e a interoperabilidade dos datos de saúde electrónicos. Aínda que está orientado principalmente a sistemas de historia clínica electrónica (HCE), senta as bases para as aplicacións de saúde dixital en canto a:

- Dereitos da cidadanía a acceder e xestionar os seus propios datos de saúde  
- Requisitos de interoperabilidade e seguridade para aplicacións que manexen datos sanitarios  
- Obriga de distinguir claramente entre datos introducidos polo propio usuario e os rexistrados por profesionais sanitarios  

#### Anteproxecto de Lei de Saúde Dixital (España, 2025)  
O Ministerio de Sanidade iniciou a elaboración desta lei para adaptar o ordenamento español ao Regulamento EHDS. Afectará ás **aplicacións móbiles de saúde**, establecendo requisitos de interoperabilidade, seguridade para os pacientes e acceso equitativo á innovación dixital. As aplicacións de recordatorio de medicación poderían quedar suxeitas ás súas disposicións unha vez aprobada.  

---

### 5. Protección do consumidor

#### Real Decreto Lexislativo 1/2007 — Lei Xeral para a Defensa dos Consumidores e Usuarios  
Aplicable aos usuarios que contraten a subscrición premium. Regula o dereito de desistimento en compras en liña (14 días naturais), a obriga de informar sobre prezos e condicións, e a resolución de reclamacións.  

---

### Resumo da normativa aplicable

| Normativa | Ámbito | Relevancia para a app |
|-----------|--------|------------------------|
| RGPD (UE) 2016/679 | Protección de datos | Alta — datos de saúde |