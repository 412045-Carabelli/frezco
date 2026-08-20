import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AnalisisComercial,
  CondicionVenta,
  CrearPedido,
  Cuenta,
  CuentaCorriente,
  Movimiento,
  MovimientoStock,
  Pedido,
  PrecioPreview,
  Producto,
  RankingProducto,
  RemitoCliente,
  RemitoProveedor,
  Resumen,
  Saldo,
  StockItem,
  TipoCuenta,
  TipoMovimiento
} from './modelos';

/** Un unico cliente para toda la API. Con nueve endpoints no hace falta un servicio por modulo. */
@Injectable({ providedIn: 'root' })
export class ApiService {

  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  // ---- Articulos ----

  productos(busqueda?: string, soloActivos = true): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.base}/productos`,
      { params: this.parametros({ busqueda, soloActivos }) });
  }

  guardarProducto(producto: Producto): Observable<Producto> {
    return producto.id
      ? this.http.put<Producto>(`${this.base}/productos/${producto.id}`, producto)
      : this.http.post<Producto>(`${this.base}/productos`, producto);
  }

  desactivarProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/productos/${id}`);
  }

  // ---- Cuentas ----

  cuentas(tipo?: TipoCuenta, busqueda?: string, soloActivas = true): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(`${this.base}/cuentas`,
      { params: this.parametros({ tipo, busqueda, soloActivas }) });
  }

  guardarCuenta(cuenta: Cuenta): Observable<Cuenta> {
    return cuenta.id
      ? this.http.put<Cuenta>(`${this.base}/cuentas/${cuenta.id}`, cuenta)
      : this.http.post<Cuenta>(`${this.base}/cuentas`, cuenta);
  }

  desactivarCuenta(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/cuentas/${id}`);
  }

  // ---- Pedidos ----

  pedidos(filtros: {
    desde?: string; hasta?: string; cuentaId?: number; incluirAnulados?: boolean;
  }): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.base}/pedidos`, { params: this.parametros(filtros) });
  }

  pedido(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.base}/pedidos/${id}`);
  }

  crearPedido(pedido: CrearPedido): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.base}/pedidos`, pedido);
  }

  anularPedido(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/pedidos/${id}/anular`, {});
  }

  precio(productoId: number, cuentaId: number | null, condicion: CondicionVenta,
         descuentoPct: number): Observable<PrecioPreview> {
    return this.http.get<PrecioPreview>(`${this.base}/pedidos/precio`,
      { params: this.parametros({ productoId, cuentaId, condicion, descuentoPct }) });
  }

  // ---- Movimientos ----

  movimientos(filtros: {
    desde?: string; hasta?: string; cuentaId?: number; tipo?: TipoMovimiento;
  }): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(`${this.base}/movimientos`, { params: this.parametros(filtros) });
  }

  crearMovimiento(movimiento: Movimiento): Observable<Movimiento> {
    return this.http.post<Movimiento>(`${this.base}/movimientos`, movimiento);
  }

  borrarMovimiento(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/movimientos/${id}`);
  }

  // ---- Stock ----

  stock(soloConStock = false): Observable<StockItem[]> {
    return this.http.get<StockItem[]>(`${this.base}/stock`, { params: this.parametros({ soloConStock }) });
  }

  movimientosDeStock(productoId: number, desde?: string, hasta?: string): Observable<MovimientoStock[]> {
    return this.http.get<MovimientoStock[]>(`${this.base}/stock/${productoId}/movimientos`,
      { params: this.parametros({ desde, hasta }) });
  }

  rankingStock(): Observable<RankingProducto[]> {
    return this.http.get<RankingProducto[]>(`${this.base}/stock/ranking`);
  }

  // ---- Cuentas corrientes ----

  cuentaCorriente(cuentaId: number, desde?: string, hasta?: string): Observable<CuentaCorriente> {
    return this.http.get<CuentaCorriente>(`${this.base}/cuentas-corrientes/${cuentaId}`,
      { params: this.parametros({ desde, hasta }) });
  }

  saldos(tipo?: TipoCuenta, soloConSaldo = true): Observable<Saldo[]> {
    return this.http.get<Saldo[]>(`${this.base}/cuentas-corrientes/saldos`,
      { params: this.parametros({ tipo, soloConSaldo }) });
  }

  // ---- Remitos y resumen ----

  remitoCliente(pedidoId: number): Observable<RemitoCliente> {
    return this.http.get<RemitoCliente>(`${this.base}/remitos/cliente/${pedidoId}`);
  }

  remitoProveedor(desde?: string, hasta?: string): Observable<RemitoProveedor> {
    return this.http.get<RemitoProveedor>(`${this.base}/remitos/proveedor`,
      { params: this.parametros({ desde, hasta }) });
  }

  resumen(desde?: string, hasta?: string): Observable<Resumen> {
    return this.http.get<Resumen>(`${this.base}/resumen`, { params: this.parametros({ desde, hasta }) });
  }

  analisisComercial(desde?: string, hasta?: string): Observable<AnalisisComercial> {
    return this.http.get<AnalisisComercial>(`${this.base}/analisis`, { params: this.parametros({ desde, hasta }) });
  }

  private parametros(valores: Record<string, unknown>): HttpParams {
    let params = new HttpParams();
    for (const [clave, valor] of Object.entries(valores)) {
      if (valor !== null && valor !== undefined && valor !== '') {
        params = params.set(clave, String(valor));
      }
    }
    return params;
  }
}
