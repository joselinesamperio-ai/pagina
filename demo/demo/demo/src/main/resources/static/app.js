(() => {
  'use strict';
  const $ = (id) => document.getElementById(id);
  const API_LOGIN = '/api/login';
  const PAGINA = 20;
  const moneda = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' });
  const fechaFmt = (iso) => new Date(iso).toLocaleString('es-MX');

  let token = null;
  try { token = sessionStorage.getItem('token'); } catch (e) { /* sin almacenamiento */ }
  const estado = { categorias: [], productos: [], proveedores: [], clientes: [], pagina: 1, texto: '', cat: '' };

  /* ======================= utilidades ======================= */
  function msg(el, texto, tipo) { el.textContent = texto; el.className = 'msg show ' + tipo; }
  function limpiar(el) { el.className = 'msg'; el.textContent = ''; }
  let toastT;
  function toast(texto) {
    const t = $('toast'); t.textContent = texto; t.hidden = false;
    clearTimeout(toastT); toastT = setTimeout(() => { t.hidden = true; }, 2800);
  }
  function guardarToken(t) {
    token = t;
    try { t ? sessionStorage.setItem('token', t) : sessionStorage.removeItem('token'); } catch (e) { /* ignorar */ }
  }

  async function api(metodo, ruta, cuerpo) {
    const headers = {};
    if (cuerpo !== undefined) headers['Content-Type'] = 'application/json';
    if (token) headers['Authorization'] = 'Bearer ' + token;
    let res;
    try {
      res = await fetch(ruta, { method: metodo, headers, body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined });
    } catch (e) {
      const err = new Error('No hay conexión con el servidor'); err.offline = true; throw err;
    }
    if (res.status === 204) return null;
    let data = null;
    try { data = await res.json(); } catch (e) { /* sin cuerpo */ }
    if (!res.ok) {
      const err = new Error((data && data.error) || 'Error ' + res.status);
      err.status = res.status; throw err;
    }
    return data;
  }

  /* ======================= conexión / PWA ======================= */
  function actualizarConexion() { $('offline').hidden = navigator.onLine; }
  window.addEventListener('online', actualizarConexion);
  window.addEventListener('offline', actualizarConexion);
  actualizarConexion();

  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => navigator.serviceWorker.register('/sw.js').catch(() => { /* sin SW */ }));
  }

  let promptInstalar = null;
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault(); promptInstalar = e;
    $('btn-instalar').hidden = false; $('btn-instalar-auth').hidden = false;
  });
  window.addEventListener('appinstalled', () => {
    promptInstalar = null; $('btn-instalar').hidden = true; $('btn-instalar-auth').hidden = true;
    toast('App instalada');
  });
  async function instalar() {
    if (!promptInstalar) return;
    promptInstalar.prompt();
    await promptInstalar.userChoice;
    promptInstalar = null; $('btn-instalar').hidden = true; $('btn-instalar-auth').hidden = true;
  }
  $('btn-instalar').onclick = instalar;
  $('btn-instalar-auth').onclick = instalar;
  const esiOS = /iphone|ipad|ipod/i.test(navigator.userAgent);
  const instalada = window.matchMedia('(display-mode: standalone)').matches || navigator.standalone;
  if (esiOS && !instalada) $('hint-ios').hidden = false;

  /* ======================= política (diálogo) ======================= */
  const dlgPol = $('dlg-politica');
  document.querySelectorAll('[data-abrir-politica]').forEach((b) => b.onclick = () => dlgPol.showModal());
  $('cerrar-politica').onclick = () => dlgPol.close();
  dlgPol.addEventListener('click', (e) => { if (e.target === dlgPol) dlgPol.close(); });
  document.querySelectorAll('dialog [data-cerrar]').forEach((b) => b.onclick = () => b.closest('dialog').close());

  /* ======================= acceso ======================= */
  function pestana(cual) {
    const login = cual === 'login';
    $('tab-login').setAttribute('aria-selected', login);
    $('tab-registro').setAttribute('aria-selected', !login);
    $('form-login').hidden = !login; $('form-registro').hidden = login;
    limpiar($('msg-auth'));
  }
  $('tab-login').onclick = () => pestana('login');
  $('tab-registro').onclick = () => pestana('registro');
  $('r-politica').onchange = () => { $('btn-registro').disabled = !$('r-politica').checked; };

  $('form-registro').onsubmit = async (e) => {
    e.preventDefault();
    if (!$('r-politica').checked) return msg($('msg-auth'), 'Debes aceptar la política de privacidad.', 'err');
    const btn = $('btn-registro'), textoBtn = btn.textContent;
    btn.disabled = true; btn.textContent = 'Creando cuenta… (puede tardar unos segundos)';
    msg($('msg-auth'), 'Estamos protegiendo tu contraseña; no cierres ni vuelvas a pulsar.', 'ok');
    try {
      await api('POST', API_LOGIN + '/registro', {
        nombre: $('r-nombre').value, correo: $('r-correo').value, password: $('r-pass').value,
        aceptaPoliticaPrivacidad: $('r-politica').checked, aceptaComunicaciones: $('r-com').checked
      });
      $('l-correo').value = $('r-correo').value;
      $('form-registro').reset();
      pestana('login');
      msg($('msg-auth'), 'Cuenta creada. Ya puedes iniciar sesión.', 'ok');
    } catch (err) { msg($('msg-auth'), err.message, 'err'); }
    finally { btn.textContent = textoBtn; btn.disabled = !$('r-politica').checked; }
  };

  $('form-login').onsubmit = async (e) => {
    e.preventDefault();
    const btn = e.submitter || $('form-login').querySelector('button[type=submit]'), textoBtn = btn.textContent;
    btn.disabled = true; btn.textContent = 'Entrando…';
    try {
      const r = await api('POST', API_LOGIN, { correo: $('l-correo').value, password: $('l-pass').value });
      guardarToken(r.token);
      $('form-login').reset();
      entrarApp(r.usuario);
    } catch (err) { msg($('msg-auth'), err.message, 'err'); }
    finally { btn.textContent = textoBtn; btn.disabled = false; }
  };

  function salir(texto) {
    guardarToken(null);
    estado.productos = []; estado.categorias = []; estado.proveedores = []; estado.clientes = [];
    $('vista-app').hidden = true; $('vista-auth').hidden = false;
    pestana('login');
    if (texto) msg($('msg-auth'), texto, 'ok');
    window.scrollTo(0, 0);
  }

  /* ======================= navegación ======================= */
  function irA(sec) {
    ['productos', 'categorias', 'proveedores', 'clientes', 'cuenta'].forEach((s) => { $('sec-' + s).hidden = s !== sec; });
    document.querySelectorAll('.nav button').forEach((b) => {
      if (b.dataset.sec === sec) b.setAttribute('aria-current', 'page'); else b.removeAttribute('aria-current');
    });
    window.scrollTo(0, 0);
  }
  document.querySelectorAll('.nav button').forEach((b) => b.onclick = () => irA(b.dataset.sec));

  async function entrarApp(usuario) {
    $('vista-auth').hidden = true; $('vista-app').hidden = false;
    if (usuario) pintarCuenta(usuario); else cuentaSinConexion();
    irA('productos');
    await cargarCatalogo();
  }

  /* ======================= catálogo ======================= */
  async function cargarCatalogo() {
    try {
      // Proveedores y clientes contienen datos personales: requieren sesión y no se guardan en la caché sin conexión
      const personales = (ruta) => api('GET', ruta).catch((err) => {
        if (err.status === 401) { salir('Tu sesión caducó. Inicia sesión de nuevo.'); }
        return [];
      });
      const provs = personales('/api/proveedores'), clis = personales('/api/clientes');
      const [cats, prods] = await Promise.all([api('GET', '/api/categorias'), api('GET', '/api/productos')]);
      estado.categorias = cats; estado.productos = prods; estado.proveedores = await provs; estado.clientes = await clis;
      pintarFiltroCategorias(); pintarCategorias(); pintarProductos(); pintarProveedores(); pintarClientes();
    } catch (err) { toast(err.message); }
  }

  function pintarFiltroCategorias() {
    const sel = $('filtro-cat'); const actual = sel.value;
    sel.replaceChildren(new Option('Todas las categorías', ''));
    estado.categorias.forEach((c) => sel.add(new Option(c.nombre, String(c.id))));
    sel.value = estado.categorias.some((c) => String(c.id) === actual) ? actual : '';
    estado.cat = sel.value;
  }

  function el(tag, clase, texto) {
    const n = document.createElement(tag);
    if (clase) n.className = clase;
    if (texto !== undefined) n.textContent = texto;
    return n;
  }
  function boton(texto, clase, fn) {
    const b = el('button', 'btn small ' + clase, texto); b.type = 'button'; b.onclick = fn; return b;
  }

  function filtrados() {
    const t = estado.texto.trim().toLowerCase();
    return estado.productos.filter((p) =>
      (!estado.cat || String(p.categoriaId) === estado.cat) && (!t || p.nombre.toLowerCase().includes(t)));
  }

  function pintarProductos() {
    const todos = filtrados();
    const paginas = Math.max(1, Math.ceil(todos.length / PAGINA));
    if (estado.pagina > paginas) estado.pagina = paginas;
    const ini = (estado.pagina - 1) * PAGINA;
    const visibles = todos.slice(ini, ini + PAGINA);

    $('p-resumen').textContent = todos.length
      ? 'Mostrando ' + (ini + 1) + '–' + (ini + visibles.length) + ' de ' + todos.length + ' productos'
      : '';
    const ul = $('lista-productos'); ul.replaceChildren();
    if (!visibles.length) ul.append(el('li', 'empty', 'No hay productos para mostrar.'));
    visibles.forEach((p) => {
      const li = el('li', 'item');
      const izq = el('div');
      izq.append(el('div', 't', p.nombre));
      izq.append(el('span', 'chip', p.categoriaNombre));
      li.append(izq, el('div', 'price', moneda.format(p.precio)));
      const acts = el('div', 'acts');
      acts.append(boton('Editar', 'ghost', () => abrirProducto(p)),
                  boton('Eliminar', 'ghost', () => eliminarProducto(p)));
      li.append(acts); ul.append(li);
    });
    $('p-pagina').textContent = 'Página ' + estado.pagina + ' de ' + paginas;
    $('p-prev').disabled = estado.pagina <= 1;
    $('p-next').disabled = estado.pagina >= paginas;
  }

  $('buscar').oninput = (e) => { estado.texto = e.target.value; estado.pagina = 1; pintarProductos(); };
  $('filtro-cat').onchange = (e) => { estado.cat = e.target.value; estado.pagina = 1; pintarProductos(); };
  $('p-prev').onclick = () => { estado.pagina--; pintarProductos(); };
  $('p-next').onclick = () => { estado.pagina++; pintarProductos(); };

  /* ---- CRUD productos ---- */
  let editandoProducto = null;
  function abrirProducto(p) {
    if (!estado.categorias.length) return toast('Primero crea una categoría.');
    editandoProducto = p || null;
    $('dp-titulo').textContent = p ? 'Editar producto' : 'Nuevo producto';
    $('f-nombre').value = p ? p.nombre : '';
    $('f-precio').value = p ? p.precio : '';
    const sel = $('f-cat'); sel.replaceChildren();
    estado.categorias.forEach((c) => sel.add(new Option(c.nombre, String(c.id))));
    sel.value = p ? String(p.categoriaId) : String(estado.categorias[0].id);
    limpiar($('msg-producto'));
    $('dlg-producto').showModal();
  }
  $('nuevo-producto').onclick = () => abrirProducto(null);

  $('form-producto').onsubmit = async (e) => {
    e.preventDefault();
    const cuerpo = { nombre: $('f-nombre').value.trim(), precio: Number($('f-precio').value), categoriaId: Number($('f-cat').value) };
    try {
      if (editandoProducto) {
        const r = await api('PUT', '/api/productos/' + editandoProducto.id, cuerpo);
        estado.productos = estado.productos.map((x) => x.id === r.id ? r : x);
      } else {
        const r = await api('POST', '/api/productos', cuerpo);
        estado.productos.push(r);
      }
      $('dlg-producto').close(); pintarProductos();
      toast(editandoProducto ? 'Producto actualizado' : 'Producto creado');
    } catch (err) { msg($('msg-producto'), err.message, 'err'); }
  };

  async function eliminarProducto(p) {
    if (!confirm('¿Eliminar "' + p.nombre + '"?')) return;
    try {
      await api('DELETE', '/api/productos/' + p.id);
      estado.productos = estado.productos.filter((x) => x.id !== p.id);
      pintarProductos(); toast('Producto eliminado');
    } catch (err) { toast(err.message); }
  }

  /* ---- CRUD categorías ---- */
  function pintarCategorias() {
    const ul = $('lista-categorias'); ul.replaceChildren();
    if (!estado.categorias.length) ul.append(el('li', 'empty', 'Aún no hay categorías. Crea la primera.'));
    estado.categorias.forEach((c) => {
      const li = el('li', 'item');
      const izq = el('div');
      izq.append(el('div', 't', c.nombre));
      if (c.descripcion) izq.append(el('div', 's', c.descripcion));
      const n = estado.productos.filter((p) => p.categoriaId === c.id).length;
      li.append(izq, el('div', 'price', n + (n === 1 ? ' producto' : ' productos')));
      const acts = el('div', 'acts');
      acts.append(boton('Editar', 'ghost', () => abrirCategoria(c)),
                  boton('Eliminar', 'ghost', () => eliminarCategoria(c)));
      li.append(acts); ul.append(li);
    });
  }

  let editandoCategoria = null;
  function abrirCategoria(c) {
    editandoCategoria = c || null;
    $('dc-titulo').textContent = c ? 'Editar categoría' : 'Nueva categoría';
    $('g-nombre').value = c ? c.nombre : '';
    $('g-desc').value = c && c.descripcion ? c.descripcion : '';
    limpiar($('msg-categoria'));
    $('dlg-categoria').showModal();
  }
  $('nueva-categoria').onclick = () => abrirCategoria(null);

  $('form-categoria').onsubmit = async (e) => {
    e.preventDefault();
    const cuerpo = { nombre: $('g-nombre').value.trim(), descripcion: $('g-desc').value.trim() };
    try {
      if (editandoCategoria) {
        const r = await api('PUT', '/api/categorias/' + editandoCategoria.id, cuerpo);
        estado.categorias = estado.categorias.map((x) => x.id === r.id ? r : x);
        // el nombre de categoría viaja en cada producto: se actualiza en la lista local
        estado.productos = estado.productos.map((p) => p.categoriaId === r.id ? { ...p, categoriaNombre: r.nombre } : p);
      } else {
        estado.categorias.push(await api('POST', '/api/categorias', cuerpo));
      }
      $('dlg-categoria').close();
      pintarFiltroCategorias(); pintarCategorias(); pintarProductos();
      toast(editandoCategoria ? 'Categoría actualizada' : 'Categoría creada');
    } catch (err) { msg($('msg-categoria'), err.message, 'err'); }
  };

  async function eliminarCategoria(c) {
    if (!confirm('¿Eliminar la categoría "' + c.nombre + '"?')) return;
    try {
      await api('DELETE', '/api/categorias/' + c.id);
      estado.categorias = estado.categorias.filter((x) => x.id !== c.id);
      pintarFiltroCategorias(); pintarCategorias(); pintarProductos();
      toast('Categoría eliminada');
    } catch (err) { toast(err.message); }
  }

  /* ---- CRUD proveedores ---- */
  function pintarProveedores() {
    const ul = $('lista-proveedores'); ul.replaceChildren();
    if (!estado.proveedores.length) ul.append(el('li', 'empty', 'Aún no hay proveedores. Crea el primero.'));
    estado.proveedores.forEach((v) => {
      const li = el('li', 'item');
      const izq = el('div');
      izq.append(el('div', 't', v.nombre));
      const datos = [v.correo, v.telefono, v.direccion].filter(Boolean).join(' · ');
      if (datos) izq.append(el('div', 's', datos));
      li.append(izq);
      const acts = el('div', 'acts');
      acts.append(boton('Editar', 'ghost', () => abrirProveedor(v)),
                  boton('Eliminar', 'ghost', () => eliminarProveedor(v)));
      li.append(acts); ul.append(li);
    });
  }

  let editandoProveedor = null;
  function abrirProveedor(v) {
    editandoProveedor = v || null;
    $('dv-titulo').textContent = v ? 'Editar proveedor' : 'Nuevo proveedor';
    $('v-nombre').value = v ? v.nombre : '';
    $('v-correo').value = v && v.correo ? v.correo : '';
    $('v-tel').value = v && v.telefono ? v.telefono : '';
    $('v-dir').value = v && v.direccion ? v.direccion : '';
    limpiar($('msg-proveedor'));
    $('dlg-proveedor').showModal();
  }
  $('nuevo-proveedor').onclick = () => abrirProveedor(null);

  $('form-proveedor').onsubmit = async (e) => {
    e.preventDefault();
    const cuerpo = { nombre: $('v-nombre').value.trim(), correo: $('v-correo').value.trim(),
                     telefono: $('v-tel').value.trim(), direccion: $('v-dir').value.trim() };
    try {
      if (editandoProveedor) {
        const r = await api('PUT', '/api/proveedores/' + editandoProveedor.id, cuerpo);
        estado.proveedores = estado.proveedores.map((x) => x.id === r.id ? r : x);
      } else {
        estado.proveedores.push(await api('POST', '/api/proveedores', cuerpo));
      }
      $('dlg-proveedor').close();
      pintarProveedores();
      toast(editandoProveedor ? 'Proveedor actualizado' : 'Proveedor creado');
    } catch (err) { msg($('msg-proveedor'), err.message, 'err'); }
  };

  async function eliminarProveedor(v) {
    if (!confirm('¿Eliminar al proveedor "' + v.nombre + '"?')) return;
    try {
      await api('DELETE', '/api/proveedores/' + v.id);
      estado.proveedores = estado.proveedores.filter((x) => x.id !== v.id);
      pintarProveedores();
      toast('Proveedor eliminado');
    } catch (err) { toast(err.message); }
  }

  /* ---- CRUD clientes ---- */
  function pintarClientes() {
    const ul = $('lista-clientes'); ul.replaceChildren();
    if (!estado.clientes.length) ul.append(el('li', 'empty', 'Aún no hay clientes. Crea el primero.'));
    estado.clientes.forEach((k) => {
      const li = el('li', 'item');
      const izq = el('div');
      izq.append(el('div', 't', k.nombre));
      izq.append(el('div', 's', [k.correo, k.telefono].filter(Boolean).join(' · ')));
      li.append(izq);
      const acts = el('div', 'acts');
      acts.append(boton('Editar', 'ghost', () => abrirCliente(k)),
                  boton('Eliminar', 'ghost', () => eliminarCliente(k)));
      li.append(acts); ul.append(li);
    });
  }

  let editandoCliente = null;
  function abrirCliente(k) {
    editandoCliente = k || null;
    $('dk-titulo').textContent = k ? 'Editar cliente' : 'Nuevo cliente';
    $('k-nombre').value = k ? k.nombre : '';
    $('k-correo').value = k ? k.correo : '';
    $('k-tel').value = k && k.telefono ? k.telefono : '';
    limpiar($('msg-cliente'));
    $('dlg-cliente').showModal();
  }
  $('nuevo-cliente').onclick = () => abrirCliente(null);

  $('form-cliente').onsubmit = async (e) => {
    e.preventDefault();
    const cuerpo = { nombre: $('k-nombre').value.trim(), correo: $('k-correo').value.trim(), telefono: $('k-tel').value.trim() };
    try {
      if (editandoCliente) {
        const r = await api('PUT', '/api/clientes/' + editandoCliente.id, cuerpo);
        estado.clientes = estado.clientes.map((x) => x.id === r.id ? r : x);
      } else {
        estado.clientes.push(await api('POST', '/api/clientes', cuerpo));
      }
      $('dlg-cliente').close();
      pintarClientes();
      toast(editandoCliente ? 'Cliente actualizado' : 'Cliente creado');
    } catch (err) { msg($('msg-cliente'), err.message, 'err'); }
  };

  async function eliminarCliente(k) {
    if (!confirm('¿Eliminar al cliente "' + k.nombre + '"?')) return;
    try {
      await api('DELETE', '/api/clientes/' + k.id);
      estado.clientes = estado.clientes.filter((x) => x.id !== k.id);
      pintarClientes();
      toast('Cliente eliminado');
    } catch (err) { toast(err.message); }
  }

  /* ======================= mi cuenta / ARCO ======================= */
  function fila(dl, etiqueta, valor) {
    const dt = el('dt', '', etiqueta); const dd = el('dd', '', valor); dl.append(dt, dd);
  }
  function pintarCuenta(u) {
    const dl = $('datos'); dl.replaceChildren();
    fila(dl, 'Nombre', u.nombre);
    fila(dl, 'Correo', u.correo);
    fila(dl, 'Política aceptada', 'v' + u.versionPolitica + ' (' + fechaFmt(u.fechaConsentimiento) + ')');
    fila(dl, 'Registro', fechaFmt(u.fechaRegistro));
    fila(dl, 'Comunicaciones', u.aceptaComunicaciones ? 'Activadas' : 'Desactivadas');
    $('x-nombre').value = u.nombre; $('x-correo').value = u.correo;
  }

  function cuentaSinConexion() {
    const dl = $('datos'); dl.replaceChildren();
    fila(dl, 'Estado', 'Sin conexión: tus datos personales solo se consultan en línea.');
  }

  async function accion(fn, okTexto) {
    const m = $('msg-cuenta');
    try { await fn(); if (okTexto) msg(m, okTexto, 'ok'); }
    catch (err) {
      if (err.status === 401 && /sesión/i.test(err.message)) return salir('Tu sesión caducó. Inicia sesión de nuevo.');
      msg(m, err.message, 'err');
    }
  }

  $('btn-acceso').onclick = () => accion(async () => pintarCuenta(await api('GET', API_LOGIN + '/yo')),
    'Estos son todos los datos personales que tenemos de ti.');
  $('form-rectificar').onsubmit = (e) => { e.preventDefault(); accion(async () => {
    pintarCuenta(await api('PUT', API_LOGIN + '/yo', { nombre: $('x-nombre').value, correo: $('x-correo').value }));
  }, 'Datos rectificados.'); };
  $('form-password').onsubmit = (e) => { e.preventDefault(); accion(async () => {
    await api('PUT', API_LOGIN + '/yo/password', { passwordActual: $('p-actual').value, passwordNueva: $('p-nueva').value });
    $('form-password').reset();
    salir('Contraseña cambiada. Inicia sesión de nuevo.');
  }); };
  $('btn-oposicion').onclick = () => accion(async () => pintarCuenta(await api('PATCH', API_LOGIN + '/yo/oposicion')),
    'Te opusiste a las comunicaciones opcionales.');
  $('form-cancelar').onsubmit = (e) => {
    e.preventDefault();
    if (!confirm('Esta acción elimina tu cuenta definitivamente. ¿Continuar?')) return;
    accion(async () => {
      await api('DELETE', API_LOGIN + '/yo', { password: $('c-pass').value });
      $('form-cancelar').reset();
      salir('Tu cuenta y tus datos fueron eliminados.');
    });
  };
  $('btn-logout').onclick = async () => {
    try { await api('POST', API_LOGIN + '/logout'); } catch (e) { /* la sesión ya no existe */ }
    salir('Sesión cerrada.');
  };

  /* ======================= restaurar sesión ======================= */
  if (token) {
    api('GET', API_LOGIN + '/yo').then(entrarApp).catch((err) => {
      if (err.offline) entrarApp(null); else guardarToken(null);
    });
  }
})();
