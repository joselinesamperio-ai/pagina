/* Service worker del Catálogo.
 * - Shell de la app: se guarda al instalar y se sirve desde caché (funciona sin conexión).
 * - GET /api/productos y /api/categorias: red primero, y si no hay red, la última copia guardada.
 * - /api/login/*, /api/proveedores y /api/clientes (datos personales): NUNCA se guardan en caché (privacidad).
 * - Cualquier POST/PUT/PATCH/DELETE va directo a la red.
 */
const VERSION = 'catalogo-v8';
const SHELL = ['/', '/index.html', '/app.css', '/app.js', '/manifest.json',
               '/icons/icon-192.png', '/icons/icon-512.png', '/icons/logo.svg', '/icons/apple-touch-icon.png',
               '/manual.html', '/manual/manual.css'];
const DATOS = ['/api/productos', '/api/categorias'];

self.addEventListener('install', (event) => {
  event.waitUntil(caches.open(VERSION).then((c) => c.addAll(SHELL)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== VERSION).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const req = event.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;

  // Datos personales y sesión: solo red (nunca en caché)
  if (url.pathname.startsWith('/api/login') || url.pathname.startsWith('/api/proveedores') || url.pathname.startsWith('/api/clientes')) return;

  // Catálogo: red primero, caché de respaldo
  if (DATOS.includes(url.pathname)) {
    event.respondWith(
      fetch(req).then((res) => {
        if (res.ok) { const copia = res.clone(); caches.open(VERSION).then((c) => c.put(req, copia)); }
        return res;
      }).catch(() => caches.match(req).then((r) => r || new Response('[]', { headers: { 'Content-Type': 'application/json' } })))
    );
    return;
  }
  if (url.pathname.startsWith('/api/')) return;

  // Navegación: red primero, y al index guardado si no hay conexión
  if (req.mode === 'navigate') {
    event.respondWith(fetch(req).catch(() => caches.match(req).then((c) => c || caches.match('/index.html'))));
    return;
  }

  // Archivos estáticos: caché primero y se actualiza en segundo plano
  event.respondWith(
    caches.match(req).then((cacheado) => {
      const red = fetch(req).then((res) => {
        if (res.ok) { const copia = res.clone(); caches.open(VERSION).then((c) => c.put(req, copia)); }
        return res;
      }).catch(() => cacheado);
      return cacheado || red;
    })
  );
});
