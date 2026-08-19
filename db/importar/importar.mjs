#!/usr/bin/env node
/**
 * Convierte los CSV exportados de la planilla en un unico script SQL de carga.
 * Se corre una sola vez, al arrancar el sistema. No hay pantalla de importacion.
 *
 *   node importar.mjs ./datos > carga.sql
 *   sqlcmd -S host,1433 -U sa -P clave -d frezco -i carga.sql
 *
 * Formato esperado en la carpeta de datos (ver README.md):
 *   productos.csv   nombre,categoria,kg,lt,costo,minorista,mayorista,cantidad
 *   cuentas.csv     nombre,tipo,zona,descuento
 *   pedidos.csv     numero,fecha,cuenta,condicion,descuento,observacion
 *   lineas.csv      numero,producto,unidades,precio,costo,entrada,salida,proveedor
 *   movimientos.csv fecha,tipo,cuenta,importe,observacion
 *
 * Los pedidos historicos se importan con su reparto ya calculado desde la planilla:
 * no se recalcula, porque el reparto dependia del stock que habia en ese momento.
 */

import { readFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';

const carpeta = process.argv[2];

if (!carpeta) {
  console.error('Uso: node importar.mjs <carpeta-con-csv>');
  process.exit(1);
}

/** CSV minimo: separador coma, comillas dobles para valores con comas. */
function leerCsv(archivo) {
  const ruta = join(carpeta, archivo);
  if (!existsSync(ruta)) {
    console.error(`-- Falta ${archivo}, se omite`);
    return [];
  }

  const lineas = readFileSync(ruta, 'utf8').split(/\r?\n/).filter(linea => linea.trim());
  const cabecera = partir(lineas.shift());

  return lineas.map(linea => {
    const valores = partir(linea);
    return Object.fromEntries(cabecera.map((columna, i) => [columna.trim(), valores[i] ?? '']));
  });
}

function partir(linea) {
  const valores = [];
  let actual = '';
  let entreComillas = false;

  for (const caracter of linea) {
    if (caracter === '"') {
      entreComillas = !entreComillas;
    } else if (caracter === ',' && !entreComillas) {
      valores.push(actual);
      actual = '';
    } else {
      actual += caracter;
    }
  }
  valores.push(actual);
  return valores.map(valor => valor.trim());
}

const texto = valor => (valor === '' || valor == null ? 'NULL' : `N'${String(valor).replace(/'/g, "''")}'`);
const numero = valor => (valor === '' || valor == null ? '0' : String(valor).replace(',', '.'));
const numeroONulo = valor => (valor === '' || valor == null ? 'NULL' : String(valor).replace(',', '.'));

const salida = [];
const emitir = linea => salida.push(linea);

emitir('-- Generado por db/importar/importar.mjs. Correr una sola vez sobre la base vacia.');
emitir('SET NOCOUNT ON;');
emitir('BEGIN TRANSACTION;');
emitir('');

// ---- Productos ----
for (const fila of leerCsv('productos.csv')) {
  emitir(`INSERT INTO producto (nombre, categoria, kg, lt, costo, precio_minorista, precio_mayorista, precio_cantidad, activo)
VALUES (${texto(fila.nombre)}, ${texto(fila.categoria)}, ${numeroONulo(fila.kg)}, ${numeroONulo(fila.lt)}, ${numero(fila.costo)}, ${numero(fila.minorista)}, ${numero(fila.mayorista)}, ${numero(fila.cantidad)}, 1);`);
}

// ---- Cuentas ----
// Las tres cuentas especiales ya las creo la migracion V2: solo se actualiza el nombre.
for (const fila of leerCsv('cuentas.csv')) {
  const tipo = (fila.tipo || 'CLIENTE').toUpperCase();
  if (tipo === 'CLIENTE') {
    emitir(`INSERT INTO cuenta (nombre, tipo, zona, descuento_pct, activo)
VALUES (${texto(fila.nombre)}, 'CLIENTE', ${texto(fila.zona)}, ${numero(fila.descuento)}, 1);`);
  } else {
    emitir(`UPDATE cuenta SET nombre = ${texto(fila.nombre)}, zona = ${texto(fila.zona)} WHERE tipo = '${tipo}';`);
  }
}

// ---- Pedidos ----
for (const fila of leerCsv('pedidos.csv')) {
  emitir(`INSERT INTO pedido (numero, fecha, cuenta_id, condicion, descuento_pct, observacion, anulado)
SELECT ${texto(fila.numero)}, ${texto(fila.fecha)}, c.id, ${texto((fila.condicion || 'MINORISTA').toUpperCase())}, ${numero(fila.descuento)}, ${texto(fila.observacion)}, 0
FROM cuenta c WHERE c.nombre = ${texto(fila.cuenta)};`);
}

// ---- Lineas ----
for (const fila of leerCsv('lineas.csv')) {
  emitir(`INSERT INTO pedido_linea (pedido_id, producto_id, unidades, precio_unitario, costo_unitario, entrada_stock, salida_stock, unidades_proveedor)
SELECT ped.id, p.id, ${numero(fila.unidades)}, ${numero(fila.precio)}, ${numero(fila.costo)}, ${numero(fila.entrada)}, ${numero(fila.salida)}, ${numero(fila.proveedor)}
FROM pedido ped, producto p WHERE ped.numero = ${texto(fila.numero)} AND p.nombre = ${texto(fila.producto)};`);
}

// ---- Movimientos ----
for (const fila of leerCsv('movimientos.csv')) {
  emitir(`INSERT INTO movimiento (fecha, tipo, cuenta_id, importe, observacion)
SELECT ${texto(fila.fecha)}, ${texto((fila.tipo || 'COBRO').toUpperCase())}, c.id, ${numero(fila.importe)}, ${texto(fila.observacion)}
FROM cuenta c WHERE c.nombre = ${texto(fila.cuenta)};`);
}

emitir('');
emitir('COMMIT;');
emitir('');
emitir('-- Verificacion cruzada contra la planilla:');
emitir("SELECT 'ventas' AS control, SUM(l.unidades * l.precio_unitario) AS total");
emitir('FROM pedido_linea l JOIN pedido p ON p.id = l.pedido_id JOIN cuenta c ON c.id = p.cuenta_id');
emitir("WHERE p.anulado = 0 AND c.tipo = 'CLIENTE';");

console.log(salida.join('\n'));
