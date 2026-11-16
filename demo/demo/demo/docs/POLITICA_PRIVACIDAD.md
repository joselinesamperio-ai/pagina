# Aviso y Política de Privacidad — Catálogo

**Versión:** 1.2 · **Última actualización:** 16 de noviembre de 2026  
**Aplica a:** cuentas de usuario (`/api/login`), clientes (`/api/clientes`) y proveedores (`/api/proveedores`)

## 1. Responsable del tratamiento

El equipo desarrollador del proyecto **Catálogo** (en adelante, "el Responsable") decide sobre el tratamiento de los datos personales que se recaban en la aplicación.

| Dato de contacto | Valor |
| --- | --- |
| Responsable / equipo | *[completar]* |
| Domicilio | *[completar]* |
| Correo para solicitudes de privacidad | *[completar, por ejemplo privacidad@tu-proyecto.mx]* |

Esta política se rige por los principios de licitud, consentimiento, información, calidad, finalidad, lealtad, proporcionalidad y responsabilidad de la **Ley Federal de Protección de Datos Personales en Posesión de los Particulares (LFPDPPP)**.

## 2. Alcance

| Módulo | Ruta | ¿Contiene datos personales? |
| --- | --- | --- |
| Cuentas de usuario | `/api/login` | Sí |
| Clientes | `/api/clientes` | Sí (nombre, correo y teléfono) |
| Proveedores | `/api/proveedores` | Sí, cuando se trata de una persona física o de un contacto (nombre, correo, teléfono y dirección) |
| Categorías y productos | `/api/categorias`, `/api/productos` | No |

## 3. Datos personales que se recaban

### 3.1 Cuentas de usuario

| Dato | Obligatorio | Para qué se usa |
| --- | --- | --- |
| Nombre | Sí | Identificar a la persona titular de la cuenta |
| Correo electrónico | Sí | Usuario de la cuenta y destino de los avisos de seguridad |
| Contraseña | Sí | Autenticación. **Nunca se guarda en texto claro**: solo un hash PBKDF2-HMAC-SHA256 con sal aleatoria |
| Aceptación de la política, su versión y la fecha | Sí | Acreditar el consentimiento otorgado |
| Fecha de registro | Sí | Ciclo de vida de la cuenta |
| Preferencia de comunicaciones | No (**desactivada** por defecto) | Avisos opcionales sobre novedades del proyecto |

### 3.2 Proveedores

| Dato | Obligatorio | Para qué se usa |
| --- | --- | --- |
| Nombre | Sí | Identificar al proveedor o a su contacto |
| Correo electrónico | No | Contacto operativo |
| Teléfono | No | Contacto operativo |
| Dirección | No | Contacto operativo |

### 3.3 Clientes

| Dato | Obligatorio | Para qué se usa |
| --- | --- | --- |
| Nombre | Sí | Identificar a la persona cliente |
| Correo electrónico | Sí | Contactar y distinguir de forma única a cada cliente |
| Teléfono | No | Contacto operativo |

### 3.4 Datos en tu dispositivo

- El **token de sesión** se guarda en el almacenamiento de sesión del navegador y se borra al cerrar la pestaña o al cerrar sesión.
- La aplicación instalada (PWA) guarda en caché sus archivos y la última lista de **productos y categorías** para funcionar sin conexión. **Nunca** se guardan en caché las cuentas, los clientes ni los proveedores.
- No se usan cookies de seguimiento, analítica ni publicidad.

**No se recaban datos personales sensibles** (salud, origen étnico, creencias, orientación sexual, datos biométricos, etc.) ni datos de menores de edad.

## 4. Finalidades

**Finalidades necesarias** (sin ellas no se puede prestar el servicio):

1. Crear y administrar la cuenta y autenticar a la persona al iniciar sesión.
2. Enviar **avisos de seguridad por correo**: confirmación de que la cuenta fue creada y notificación de cada inicio de sesión, para que puedas detectar accesos que no reconozcas.
3. Mantener el registro de clientes y proveedores para la operación del catálogo.
4. Atender las solicitudes de derechos ARCO.

**Finalidad opcional** (puedes oponerte sin perder el servicio):

5. Enviar comunicaciones sobre novedades del proyecto. Está **desactivada por defecto** y solo se activa si la solicitas al registrarte.

Los datos no se venden ni se usan para publicidad.

## 5. Consentimiento

- **Cuentas:** el registro solo se completa si aceptas expresamente esta política. El sistema guarda la versión aceptada y la fecha del consentimiento.
- **Clientes y proveedores:** el consentimiento se obtiene cuando la persona entrega sus datos al Responsable, poniéndole este aviso a disposición.
- Puedes **revocar tu consentimiento** en cualquier momento eliminando tu cuenta (Cancelación). Si la política cambia de forma sustancial, se publicará una nueva versión (`GET /api/login/politica-privacidad`) y se solicitará nuevamente tu consentimiento.

## 6. Medidas de seguridad

- **Contraseñas:** hash PBKDF2-HMAC-SHA256 con 210 000 iteraciones y sal única por usuario; nunca se devuelven en una respuesta.
- **Sesiones:** token aleatorio de 256 bits con caducidad de 30 minutos, que se invalida al cerrar sesión, cambiar la contraseña o cancelar la cuenta.
- **Clientes y proveedores protegidos:** todas las rutas de `/api/clientes` y `/api/proveedores` exigen una sesión iniciada.
- **Mensajes de error genéricos** en el login ("Credenciales inválidas") para no revelar si un correo existe.
- **Sin listados de cuentas:** ninguna función muestra datos de otras cuentas; cada titular solo accede a su propia información.
- **Conexión cifrada:** en el sitio publicado, la comunicación viaja por HTTPS.
- **Sin datos personales ni contraseñas en los registros (logs)** de la aplicación.
- **Secretos fuera del código:** la clave del servicio de correo se configura con variables de entorno.

## 7. Derechos ARCO

Toda persona titular puede ejercer los derechos de **Acceso, Rectificación, Cancelación y Oposición**. La forma depende del módulo donde estén sus datos.

### 7.1 Titulares de una cuenta

Se ejercen desde la sección **Mi cuenta** (o la API), una vez que la persona acredita su identidad iniciando sesión; esto equivale a la *acreditación de la personalidad* del procedimiento de solicitudes ARCO.

| Derecho | Qué permite | Cómo se ejerce | Resultado |
| --- | --- | --- | --- |
| **Acceso** | Conocer qué datos tenemos y cómo los tratamos | `GET /api/login/yo` | Se muestran todos los datos almacenados (sin el hash) y la versión de política aceptada |
| **Rectificación** | Corregir datos inexactos o incompletos | `PUT /api/login/yo` y `PUT /api/login/yo/password` | Se actualizan de inmediato; al cambiar la contraseña se cierran las demás sesiones |
| **Cancelación** | Eliminar los datos o retirar el consentimiento | `DELETE /api/login/yo` (se confirma con la contraseña) | La cuenta y sus datos se **eliminan definitivamente** y se cierran todas las sesiones |
| **Oposición** | Oponerse a finalidades no necesarias | `PATCH /api/login/yo/oposicion` | Se desactivan las comunicaciones opcionales; la cuenta sigue funcionando |

Las rutas `/api/login/yo` exigen el encabezado `Authorization: Bearer <token>`.

### 7.2 Clientes y proveedores

Las personas registradas como clientes o proveedores no inician sesión, por lo que envían su solicitud al **correo de contacto del Responsable** (apartado 7.3). Verificada su identidad, el Responsable la atiende con la aplicación (pestañas *Clientes* y *Proveedores*) o con la API:

| Derecho | Clientes | Proveedores |
| --- | --- | --- |
| **Acceso** | `GET /api/clientes/{id}` | `GET /api/proveedores/{id}` |
| **Rectificación** | `PUT /api/clientes/{id}` | `PUT /api/proveedores/{id}` |
| **Cancelación** | `DELETE /api/clientes/{id}` | `DELETE /api/proveedores/{id}` |
| **Oposición** | Se registra la solicitud y se deja de usar el dato para la finalidad a la que se opone | Igual que en clientes |

### 7.3 Procedimiento y plazos por correo

El ejercicio desde la aplicación es inmediato. Cuando la persona no puede usarla (por ejemplo, olvidó su contraseña o es proveedor), se sigue este procedimiento basado en la gestión de solicitudes ARCO:

1. **Ingreso de solicitud:** la persona indica qué derecho desea ejercer.
2. **Competencia:** si el Responsable no es competente, lo notifica en **3 días hábiles**.
3. **Prevención:** si la solicitud está incompleta, se previene en **5 días hábiles**; la persona cuenta con **10 días hábiles** para responder.
4. **Acreditación de la personalidad:** se verifica que quien solicita es la persona titular.
5. **Turno:** se turna al área responsable, que tiene **3 días hábiles** para informar si corresponde a otra área.
6. **Respuesta interna:** el área responde en **5 días hábiles**. Si es negativa o los datos no existen, se somete a revisión del Comité de Transparencia.
7. **Resolución y respuesta final:** el Comité puede confirmar, modificar o revocar la determinación. La respuesta final se entrega en un máximo de **20 días hábiles**, ampliables **10** más con justificación.

El diagrama de flujo está en [`DIAGRAMA_ARCO.md`](DIAGRAMA_ARCO.md).

### 7.4 Limitaciones

Un derecho puede negarse cuando la persona no acredite su identidad, cuando los datos no existan en los registros o cuando una ley exija conservarlos. En ese caso se informará el motivo por escrito.

## 8. Conservación

Los datos se conservan mientras la cuenta, el cliente o el proveedor estén activos. Con la **Cancelación** se eliminan definitivamente de la base de datos.

## 9. Transferencias y encargados del tratamiento

No se venden ni se ceden datos personales. Para operar el servicio intervienen dos proveedores tecnológicos que tratan datos por cuenta del Responsable:

| Proveedor | Para qué | Datos que recibe |
| --- | --- | --- |
| Servicio de alojamiento en la nube (Render, servidores en Estados Unidos) | Ejecutar la aplicación y guardar la base de datos | Todos los datos de la aplicación |
| Servicio de envío de correo (Brevo), solo si está activado | Enviar los avisos de seguridad | Nombre y correo de la persona usuaria, y el texto del aviso |

Al aceptar esta política consientes este tratamiento, que es necesario para prestar el servicio.

## 10. Medios para limitar el uso o la divulgación

Puedes oponerte a las comunicaciones opcionales con el derecho de **Oposición** (botón *Oponerme* en Mi cuenta). Los avisos de seguridad son parte del servicio; si no deseas recibirlos, puedes cancelar tu cuenta.

## 11. Cambios a esta política

Cualquier modificación se publicará con un nuevo número de versión y fecha. El endpoint `GET /api/login/politica-privacidad` siempre devuelve la versión vigente.

## 12. Autoridad

Si consideras que tu derecho a la protección de datos personales fue vulnerado, puedes acudir ante la autoridad competente en materia de protección de datos personales conforme a la LFPDPPP vigente.

## 13. Cómo se aplica esta política en el proyecto

| Principio / requisito | Implementación |
| --- | --- |
| Consentimiento | `RegistroRequest.aceptaPoliticaPrivacidad` debe ser `true`; se guardan `versionPolitica` y `fechaConsentimiento` en `Usuario` |
| Información | `GET /api/login/politica-privacidad`, este documento y el aviso dentro de la aplicación |
| Proporcionalidad / minimización | Solo se piden los datos indispensables; no hay endpoint que liste cuentas |
| Seguridad | `PasswordHasher` (PBKDF2), `TokenService` (sesiones con caducidad y revocación; `ClienteController` y `ProveedorController` exigen sesión) |
| Avisos de seguridad | `NotificadorCorreo`: correo al crear la cuenta y en cada inicio de sesión (si está configurado) |
| Acceso | `GET /api/login/yo` |
| Rectificación | `PUT /api/login/yo`, `PUT /api/login/yo/password` |
| Cancelación | `DELETE /api/login/yo` |
| Oposición | `PATCH /api/login/yo/oposicion` |
| ARCO de clientes y proveedores | `ClienteController` y `ProveedorController`, atendidos por el Responsable tras una solicitud |
