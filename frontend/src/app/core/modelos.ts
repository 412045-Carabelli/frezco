export type TipoCuenta = 'CLIENTE' | 'PROVEEDOR' | 'REFUERZO' | 'CONSUMO';
export type CondicionVenta = 'MINORISTA' | 'MAYORISTA' | 'CANTIDAD';
export type TipoMovimiento = 'COBRO' | 'PAGO';

export interface Producto {
  id: number | null;
  nombre: string;
  categoria: string | null;
  kg: number | null;
  lt: number | null;
  costo: number;
  precioMinorista: number;
  precioMayorista: number;
  precioCantidad: number;
  activo: boolean;
}

export interface Cuenta {
  id: number | null;
  nombre: string;
  tipo: TipoCuenta;
  zona: string | null;
  descuentoPct: number;
  activo: boolean;
}

export interface PedidoLinea {
  productoId: number;
  productoNombre: string;
  unidades: number;
  precioUnitario: number;
  costoUnitario: number;
  importe: number;
  entradaStock: number;
  salidaStock: number;
  unidadesProveedor: number;
}

export interface Pedido {
  id: number;
  numero: string;
  fecha: string;
  cuenta: { id: number; nombre: string; tipo: TipoCuenta };
  condicion: CondicionVenta;
  descuentoPct: number;
  observacion: string | null;
  anulado: boolean;
  total: number;
  totalCosto: number;
  margen: number;
  lineas: PedidoLinea[];
}

export interface CrearPedido {
  fecha: string;
  cuentaId: number;
  condicion: CondicionVenta;
  descuentoPct: number;
  observacion: string | null;
  lineas: { productoId: number; unidades: number }[];
}

export interface PrecioPreview {
  precioUnitario: number;
  stockDisponible: number;
}

export interface Movimiento {
  id: number | null;
  fecha: string;
  tipo: TipoMovimiento;
  cuentaId: number;
  cuentaNombre?: string;
  importe: number;
  observacion: string | null;
}

export interface StockItem {
  productoId: number;
  nombre: string;
  categoria: string | null;
  entradas: number;
  salidas: number;
  stockActual: number;
  ultimaEntrada: string | null;
  ultimaSalida: string | null;
}

export interface MovimientoStock {
  fecha: string;
  numero: string;
  cuenta: string;
  entrada: number;
  salida: number;
  saldo: number;
}

export interface CuentaCorriente {
  cuenta: { id: number; nombre: string; tipo: TipoCuenta };
  saldoAnterior: number;
  totalDebe: number;
  totalHaber: number;
  saldoFinal: number;
  esDeuda: boolean;
  lineas: {
    fecha: string;
    origen: 'PEDIDO' | 'MOVIMIENTO';
    detalle: string;
    debe: number;
    haber: number;
    saldo: number;
  }[];
}

export interface Saldo {
  cuentaId: number;
  nombre: string;
  saldo: number;
}

export interface RemitoCliente {
  pedidoId: number;
  numero: string;
  fecha: string;
  cliente: string;
  observacion: string | null;
  total: number;
  lineas: { producto: string; unidades: number; precioUnitario: number; importe: number }[];
}

export interface RemitoProveedor {
  desde: string;
  hasta: string;
  proveedor: string;
  totalPeriodo: number;
  dias: {
    fecha: string;
    subtotal: number;
    lineas: { producto: string; unidades: number; costoUnitario: number; importe: number }[];
  }[];
}

export interface Resumen {
  desde: string;
  hasta: string;
  ventas: number;
  costo: number;
  margen: number;
  margenPct: number;
  cantidadPedidos: number;
  ticketPromedio: number;
  saldoACobrar: number;
  saldoAPagar: number;
  productosSinStock: string[];
}
