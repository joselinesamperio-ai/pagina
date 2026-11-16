$base = "http://localhost:8080"
$utf8 = "application/json; charset=utf-8"

function Enviar($metodo, $url, $objeto) {
    $json = $objeto | ConvertTo-Json
    Invoke-RestMethod -Method $metodo -Uri $url -ContentType $utf8 `
        -Body ([Text.Encoding]::UTF8.GetBytes($json))
}

# --- 5 categorias (si ya existen, se reutilizan) ---
$computacion = "Computaci" + [char]0x00F3 + "n"
$nombres = @($computacion, "Celulares", "Hogar", "Deportes", "Juguetes")
$existentes = @(Invoke-RestMethod -Uri "$base/api/categorias")
$ids = @()
foreach ($n in $nombres) {
    $cat = $existentes | Where-Object { $_.nombre -eq $n }
    if (-not $cat) {
        $cat = Enviar "Post" "$base/api/categorias" @{ nombre = $n; descripcion = "Categoria de $n" }
    }
    $ids += $cat.id
}
Write-Host "Categorias listas: $($ids -join ', ')"
if ($ids.Count -ne 5 -or ($ids | Where-Object { -not $_ })) {
    Write-Host "No se pudieron preparar las 5 categorias. Se cancela." -ForegroundColor Red
    exit 1
}

# --- 5000 productos repartidos entre las 5 categorias ---
$total = 5000
for ($i = 1; $i -le $total; $i++) {
    $producto = @{
        nombre      = "Producto $i"
        precio      = [math]::Round((Get-Random -Minimum 10 -Maximum 20000) + (Get-Random -Minimum 0 -Maximum 100) / 100, 2)
        categoriaId = $ids[$i % 5]
    }
    Enviar "Post" "$base/api/productos" $producto | Out-Null
    if ($i % 250 -eq 0) { Write-Host "$i / $total productos" }
}
Write-Host "Listo."
