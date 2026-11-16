# API REST de catálogo

API Spring Boot con persistencia H2 y CRUD para categorías y productos. El modelo separa las categorías de los productos para evitar repetir sus datos; cada producto requiere una categoría existente. H2 guarda los datos en `demo/data/catalogo` y Hibernate crea/actualiza las tablas al arrancar.

## Modelo relacional

- `categorias`: `id` (PK), `nombre` (obligatorio, único, máximo 80), `descripcion` (máximo 250).
- `productos`: `id` (PK), `nombre` (obligatorio, máximo 120), `precio` (decimal obligatorio, mayor que cero), `categoria_id` (FK obligatoria a `categorias.id`).
- Una categoría puede tener muchos productos. No se permite borrar una categoría mientras esté referenciada por productos.

## Ejecutar

Desde la carpeta `demo`:

```powershell
.\gradlew.bat bootRun
```

La API queda disponible en `http://localhost:8080`. H2 Console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/catalogo`, usuario `sa`, contraseña vacía).

## Endpoints

| Método | Ruta | Acción |
| --- | --- | --- |
| GET | `/api/categorias` | Listar categorías |
| GET | `/api/categorias/{id}` | Consultar categoría |
| POST | `/api/categorias` | Crear categoría |
| GET / POST | `/api/clientes` | Listar / crear clientes (**requiere** `Authorization: Bearer <token>`) |
| GET / PUT / DELETE | `/api/clientes/{id}` | Consultar / actualizar / eliminar cliente (**requiere** token) |
| GET / POST | `/api/proveedores` | Listar / crear proveedores (**requiere** `Authorization: Bearer <token>`) |
| GET / PUT / DELETE | `/api/proveedores/{id}` | Consultar / actualizar / eliminar proveedor (**requiere** token) |
| PUT | `/api/categorias/{id}` | Actualizar categoría |
| DELETE | `/api/categorias/{id}` | Eliminar categoría |
| GET | `/api/productos` | Listar productos con su categoría |
| GET | `/api/productos/{id}` | Consultar producto |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{id}` | Actualizar producto y categoría |
| DELETE | `/api/productos/{id}` | Eliminar producto |

Ejemplos de cuerpos JSON:

```json
{"nombre":"Computación","descripcion":"Equipo y accesorios"}
```

```json
{"nombre":"Laptop Gamer","precio":18500.00,"categoriaId":1}
```

Las validaciones devuelven `400`, los recursos o categorías inexistentes `404` y las violaciones de integridad (como nombre de categoría duplicado o borrar una categoría en uso) `409`. Las eliminaciones exitosas devuelven `204`.

## Verificación

Ejecuta `.\gradlew.bat test` para ejecutar las pruebas HTTP automatizadas con MockMvc. Cubren GET, POST, PUT, DELETE, validaciones, referencias a categorías inexistentes y la protección de categorías referenciadas.

## Login y privacidad (derechos ARCO)

Módulo de cuentas en `/api/login`, regido por [`docs/POLITICA_PRIVACIDAD.md`](docs/POLITICA_PRIVACIDAD.md). El flujo de atención de derechos ARCO está en [`docs/DIAGRAMA_ARCO.md`](docs/DIAGRAMA_ARCO.md).

| Método | Ruta | Acción / derecho |
| --- | --- | --- |
| GET | `/api/login/politica-privacidad` | Información: política vigente |
| POST | `/api/login/registro` | Crear cuenta (exige `aceptaPoliticaPrivacidad: true`) |
| POST | `/api/login` | Iniciar sesión → token Bearer (30 min) |
| POST | `/api/login/logout` | Cerrar sesión |
| GET | `/api/login/yo` | **Acceso** |
| PUT | `/api/login/yo` | **Rectificación** (nombre, correo) |
| PUT | `/api/login/yo/password` | **Rectificación** de contraseña |
| PATCH | `/api/login/yo/oposicion` | **Oposición** a comunicaciones opcionales |
| DELETE | `/api/login/yo` | **Cancelación** (confirma con contraseña) |

Las rutas `/yo` requieren `Authorization: Bearer <token>`. No existe un endpoint que liste usuarios.

```json
{"nombre":"Ana Pérez","correo":"ana@correo.com","password":"secreto123","aceptaPoliticaPrivacidad":true}
```

### Interfaz web

Al correr la aplicación, abre `http://localhost:8080/` (archivo `src/main/resources/static/index.html`): login, registro con aceptación de la política de privacidad (se muestra completa en una ventana) y panel "Mi cuenta" para ejercer los derechos ARCO. Usa los mismos endpoints de `/api/login`.

## Front + back en una sola app

El front (HTML/CSS/JS, sin frameworks) vive en `src/main/resources/static/` y Spring lo sirve junto con la API: **un solo proyecto, un solo puerto, sin CORS**.

| Archivo | Para qué |
| --- | --- |
| `index.html`, `app.css`, `app.js` | Login, registro, Productos, Categorías y Mi cuenta (ARCO) |
| `manifest.json` | Datos de la PWA (nombre, íconos, colores) |
| `sw.js` | Service worker: caché de la app y del catálogo para uso sin conexión |
| `icons/` | Íconos de la app (192, 512, maskable, Apple) |

## Levantarlo en local

```powershell
.\gradlew.bat bootRun        # Windows
./gradlew bootRun            # Mac / Linux
```

Abre `http://localhost:8080/`. Carga datos de prueba (con la app corriendo) con `cargar-datos.ps1`.

## Subirlo a un dominio web (Docker)

El `Dockerfile` compila y ejecuta la app en cualquier hosting que acepte contenedores (Render, Railway, Fly.io…).

Ejemplo en **Render**:

1. Sube la carpeta `demo` a un repositorio de GitHub.
2. En render.com: **New → Web Service** → elige el repositorio → **Runtime: Docker**.
3. Deja el puerto por defecto (la app lee la variable `PORT`).
4. Al terminar el despliegue, Render te da una URL `https://tu-app.onrender.com` con HTTPS.

Variables de entorno (opcionales): `H2_CONSOLE=false` (ya viene así en Docker), `DB_PATH` (ruta de la base).
La base H2 es un archivo: en planes gratuitos el disco es **temporal** y los datos se pierden al reiniciar; para conservarlos usa un disco persistente montado en `/data`.

Probar la imagen en tu máquina (requiere Docker):

```powershell
docker build -t catalogo .
docker run -p 8080:8080 catalogo
```

## Instalar como PWA

La app cumple los requisitos de una PWA: manifest, service worker y HTTPS (o `localhost`).

- **Android / Chrome:** botón **Instalar app** dentro de la app, o menú ⋮ → *Instalar app*.
- **Windows / Mac (Chrome o Edge):** ícono de instalar en la barra de direcciones.
- **iPhone / iPad (Safari):** **Compartir → Agregar a pantalla de inicio**.

Sin conexión se muestra la última copia del catálogo; los datos personales (`/api/login/*`) nunca se guardan en caché.


## Avisos de seguridad por correo (opcional)

Al crear la cuenta y en cada inicio de sesión se puede enviar un correo a la persona usuaria.
Usa la API HTTPS de [Brevo](https://www.brevo.com) (los hostings gratuitos suelen bloquear SMTP).
Queda **desactivado** si no se definen estas variables de entorno (en Render: *Environment*):

| Variable | Valor |
| --- | --- |
| `BREVO_API_KEY` | Clave de API de Brevo (*SMTP & API → API Keys*) |
| `MAIL_FROM` | Remitente verificado en Brevo (*Senders*) |
| `MAIL_FROM_NAME` | Nombre visible (opcional, por defecto `Catálogo`) |

El envío es en segundo plano: si falla, el registro y el login no se ven afectados.
