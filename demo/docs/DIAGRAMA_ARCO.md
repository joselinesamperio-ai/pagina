# Diagrama de flujo — Derechos ARCO en el proyecto

Basado en el procedimiento para gestionar una solicitud ARCO (página 13 del PDF): ingreso, competencia, prevención, acreditación de la personalidad, turno, respuesta interna / Comité, resolución y respuesta final.

![Diagrama ARCO](diagrama-arco.png)

```mermaid
flowchart TD
    A([1. INGRESO DE SOLICITUD<br/>La persona titular quiere ejercer un derecho ARCO]) --> B{¿Puede usar la API?}
    B -- Sí --> C[Inicia sesión<br/>POST /api/login]
    B -- No, p. ej. olvidó su contraseña --> Z[Envía solicitud al correo del Responsable]
    Z --> D{2. COMPETENCIA<br/>¿El Responsable es competente?}
    D -- No --> D1[Se notifica en 3 días hábiles] --> FIN
    D -- Sí --> E{3. PREVENCIÓN<br/>¿Solicitud completa?}
    E -- No --> E1[Se previene en 5 días hábiles<br/>La persona responde en 10 días hábiles] --> E
    E -- Sí --> F
    C --> F[4. ACREDITACIÓN DE LA PERSONALIDAD<br/>Credenciales válidas + token Bearer vigente]
    F --> G{¿Identidad acreditada?}
    G -- No --> G1[401 No autorizado<br/>No se atiende la solicitud] --> FIN
    G -- Sí --> H[5. TURNO<br/>Se dirige al endpoint del derecho solicitado]
    H --> I{¿Qué derecho?}
    I -- Acceso --> J1[GET /api/login/yo<br/>Devuelve los datos almacenados]
    I -- Rectificación --> J2[PUT /api/login/yo<br/>PUT /api/login/yo/password<br/>Valida y actualiza datos]
    I -- Cancelación --> J3[DELETE /api/login/yo<br/>Confirma contraseña, elimina la cuenta<br/>y cierra sesiones]
    I -- Oposición --> J4[PATCH /api/login/yo/oposicion<br/>Desactiva comunicaciones opcionales]
    J1 --> K{6. RESPUESTA INTERNA<br/>¿Datos válidos y existentes?}
    J2 --> K
    J3 --> K
    J4 --> K
    K -- No: dato inválido, duplicado o inexistente --> L[7. RESOLUCIÓN<br/>400 / 404 / 409 con motivo por escrito<br/>Revisión por el Comité o equipo responsable]
    K -- Sí --> M[7. RESOLUCIÓN<br/>200 / 204 derecho atendido]
    L --> FIN([RESPUESTA FINAL<br/>Máximo 20 días hábiles, ampliable 10 más])
    M --> FIN
```
