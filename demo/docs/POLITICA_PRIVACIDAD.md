# Política de Privacidad — API de Catálogo (módulo de Login)

**Versión:** 1.0  
**Última actualización:** 22 de octubre de 2026

## 1. Responsable del tratamiento

El equipo desarrollador del proyecto **API de Catálogo** (en adelante, "el Responsable") es quien decide sobre el tratamiento de los datos personales que se recaban en el módulo de inicio de sesión (`/api/login`).

> Datos de contacto del Responsable: *[completar con nombre del equipo y correo de contacto, por ejemplo privacidad@tu-proyecto.mx]*

Esta política se rige por los principios de licitud, consentimiento, información, calidad, finalidad, lealtad, proporcionalidad y responsabilidad de la **Ley Federal de Protección de Datos Personales en Posesión de los Particulares (LFPDPPP)**.

## 2. Datos personales que se recaban

| Dato | Obligatorio | Para qué se usa |
| --- | --- | --- |
| Nombre | Sí | Identificar a la persona titular de la cuenta |
| Correo electrónico | Sí | Identificador de la cuenta (usuario de login) |
| Contraseña | Sí | Autenticación. **Nunca se guarda en texto claro**: solo se almacena un hash PBKDF2-HMAC-SHA256 con sal aleatoria |
| Aceptación de la política y su versión | Sí | Acreditar el consentimiento otorgado |
| Fecha y hora de consentimiento y de registro | Sí | Acreditar el consentimiento y el ciclo de vida de la cuenta |
| Preferencia de comunicaciones | No (por defecto **desactivada**) | Enviar avisos opcionales del proyecto |

**No se recaban datos personales sensibles** (salud, origen étnico, creencias, orientación sexual, datos biométricos, etc.) ni datos de menores de edad.

## 3. Finalidades

**Finalidades necesarias** (sin ellas no se puede prestar el servicio):

1. Crear y administrar la cuenta de la persona usuaria.
2. Autenticar a la persona usuaria al iniciar sesión.
3. Atender las solicitudes de derechos ARCO.

**Finalidad opcional** (la persona puede oponerse sin perder el servicio):

4. Envío de comunicaciones sobre novedades del proyecto. Está **desactivada por defecto**; solo se activa si la persona lo solicita expresamente.

Los datos no se venden, no se ceden ni se transfieren a terceros.

## 4. Consentimiento

El registro **solo se completa si la persona acepta expresamente esta política** (`aceptaPoliticaPrivacidad = true`). El sistema guarda la versión aceptada y la fecha del consentimiento. Si esta política cambia de forma sustancial, se publicará una nueva versión (consultable en `GET /api/login/politica-privacidad`) y se solicitará nuevamente el consentimiento.

## 5. Medidas de seguridad

- Contraseñas con hash PBKDF2-HMAC-SHA256 (210 000 iteraciones y sal única por usuario); nunca se devuelven en ninguna respuesta de la API.
- Las respuestas solo incluyen los datos mínimos necesarios (sin hash, sin sal).
- Sesiones mediante token aleatorio de 256 bits con caducidad de 30 minutos; se invalida al cerrar sesión, al cambiar la contraseña y al cancelar la cuenta.
- Mensajes de error genéricos en el login ("credenciales inválidas") para no revelar si un correo existe.
- No existe ningún endpoint que liste datos de otras personas: cada titular solo accede a **su propia** información.
- No se escriben datos personales ni contraseñas en los registros (logs) de la aplicación.

## 6. Derechos ARCO

Toda persona titular puede ejercer los derechos de **Acceso, Rectificación, Cancelación y Oposición**. En este proyecto se ejercen directamente desde la API, una vez que la persona acredita su identidad iniciando sesión (equivalente a la *acreditación de la personalidad* del procedimiento de solicitudes ARCO).

| Derecho | Qué permite | Cómo se ejerce en el proyecto | Resultado |
| --- | --- | --- | --- |
| **Acceso** | Conocer qué datos personales tenemos y cómo los tratamos | `GET /api/login/yo` | Devuelve todos los datos almacenados (excepto el hash) y la versión de política aceptada |
| **Rectificación** | Corregir datos inexactos o incompletos | `PUT /api/login/yo` (nombre, correo) y `PUT /api/login/yo/password` | Los datos se actualizan de inmediato |
| **Cancelación** | Que se eliminen los datos cuando ya no son necesarios o se retire el consentimiento | `DELETE /api/login/yo` (se confirma con la contraseña) | La cuenta y sus datos se **eliminan definitivamente**, y se cierran todas las sesiones |
| **Oposición** | Oponerse al tratamiento para finalidades no necesarias | `PATCH /api/login/yo/oposicion` | Se desactivan las comunicaciones opcionales; la cuenta sigue funcionando |

### 6.1 Procedimiento y plazos

El ejercicio mediante la API es inmediato. Si la persona no puede usar la API (por ejemplo, olvidó su contraseña), puede enviar una solicitud al correo de contacto del Responsable, y se seguirá este procedimiento (basado en el procedimiento de gestión de solicitudes ARCO):

1. **Ingreso de solicitud:** la persona envía su solicitud indicando qué derecho desea ejercer.
2. **Competencia:** si el Responsable no es competente, lo notifica dentro de los **3 días hábiles** siguientes.
3. **Prevención:** si la solicitud está incompleta, se previene a la persona dentro de los **5 días hábiles** siguientes; ella cuenta con **10 días hábiles** para responder.
4. **Acreditación de la personalidad:** se verifica que quien solicita es la persona titular de los datos.
5. **Turno:** se turna al área responsable del dato, que tiene **3 días hábiles** para informar si la solicitud corresponde a otra área.
6. **Respuesta interna:** el área emite su respuesta en **5 días hábiles**. Si es negativa o los datos no existen, se somete a revisión del Comité de Transparencia.
7. **Resolución y respuesta final:** el Comité puede confirmar, modificar o revocar la determinación. La respuesta final se entrega en un máximo de **20 días hábiles** (ampliable por 10 días hábiles más con justificación).

El diagrama de flujo está en [`DIAGRAMA_ARCO.md`](DIAGRAMA_ARCO.md).

### 6.2 Limitaciones

El ejercicio de un derecho puede negarse cuando la persona no acredite su identidad, cuando los datos no existan en nuestros registros o cuando una ley exija conservarlos. En ese caso se informará el motivo por escrito.

## 7. Conservación

Los datos se conservan mientras la cuenta esté activa. Al ejercer la **Cancelación**, se eliminan definitivamente de la base de datos.

## 8. Transferencias

No se realizan transferencias de datos personales a terceros.

## 9. Cambios a esta política

Cualquier modificación se publicará con un nuevo número de versión y fecha de actualización. El endpoint `GET /api/login/politica-privacidad` siempre devuelve la versión vigente.

## 10. Cómo se aplica esta política en el código

| Principio / requisito | Implementación |
| --- | --- |
| Consentimiento | `RegistroRequest.aceptaPoliticaPrivacidad` debe ser `true`; se guarda `versionPolitica` y `fechaConsentimiento` en `Usuario` |
| Información | `GET /api/login/politica-privacidad` |
| Proporcionalidad / minimización | Solo se pide nombre, correo y contraseña; no hay endpoint que liste usuarios |
| Seguridad | `PasswordHasher` (PBKDF2), `TokenService` (sesiones con caducidad) |
| Acceso | `GET /api/login/yo` |
| Rectificación | `PUT /api/login/yo`, `PUT /api/login/yo/password` |
| Cancelación | `DELETE /api/login/yo` |
| Oposición | `PATCH /api/login/yo/oposicion` |
