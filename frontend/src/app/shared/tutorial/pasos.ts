/**
 * Guion del tutorial, una entrada por pantalla. Es texto, no codigo: para cambiar la ayuda
 * se edita esta lista y nada mas.
 *
 * `selector` es opcional. Si esta, el tutorial resalta ese elemento de la pantalla; si no,
 * el paso se muestra centrado como explicacion general.
 */
export interface PasoTutorial {
  titulo: string;
  texto: string;
  selector?: string;
}

export const PASOS: Record<string, PasoTutorial[]> = {

  '/resumen': [
    {
      titulo: 'Esta es tu pantalla de inicio',
      texto: 'Acá ves cómo venís en el mes: lo que vendiste, lo que ganaste, lo que te deben ' +
        'y lo que debés. Cambia solo, a medida que cargás pedidos y cobros.'
    },
    {
      titulo: 'Elegí el período',
      texto: 'Por defecto te muestra el mes en curso. Si querés mirar otra fecha, cambiá ' +
        '"Desde" y "Hasta" y tocá Actualizar.',
      selector: '[data-tutorial="periodo"]'
    },
    {
      titulo: 'El menú de la izquierda',
      texto: 'Desde ahí entrás a todo lo demás. En el celular se abre con el botón de las ' +
        'tres rayitas, arriba a la izquierda.',
      selector: '[data-tutorial="menu"]'
    }
  ],

  '/pedidos': [
    {
      titulo: 'Todos tus pedidos',
      texto: 'La lista de lo que fuiste cargando. Arriba filtrás por fecha o por cliente.'
    },
    {
      titulo: 'Cargar uno nuevo',
      texto: 'Este es el botón que más vas a usar. Te lleva a la pantalla de carga.',
      selector: '[data-tutorial="nuevo-pedido"]'
    },
    {
      titulo: 'Si te equivocaste',
      texto: 'Los pedidos no se editan. Se anulan con el botón rojo y se cargan de nuevo. ' +
        'El stock y los saldos se acomodan solos.',
      selector: '[data-tutorial="tabla-pedidos"]'
    }
  ],

  '/pedidos/nuevo': [
    {
      titulo: 'Cargar una venta',
      texto: 'Tres pasos: elegís el cliente, agregás los artículos y guardás. Nada más.'
    },
    {
      titulo: 'Primero el cliente',
      texto: 'Escribí las primeras letras del nombre y elegilo de la lista. El descuento ' +
        'que tenga cargado se completa solo, y lo podés cambiar.',
      selector: '[data-tutorial="cuenta"]'
    },
    {
      titulo: 'Después los artículos',
      texto: 'Tocá "Artículo", buscá el producto y poné cuántas unidades. El precio se ' +
        'completa solo según la condición que elegiste.',
      selector: '[data-tutorial="agregar-linea"]'
    },
    {
      titulo: 'Si dice "se pide al proveedor"',
      texto: 'Es porque no tenés stock de ese artículo. No es un error: el sistema lo suma ' +
        'al pedido del proveedor y vos seguís normalmente.'
    },
    {
      titulo: 'Guardar',
      texto: 'El total siempre está a la vista acá abajo. Al guardar vas directo al remito ' +
        'para mandárselo al cliente por WhatsApp.',
      selector: '[data-tutorial="pie-total"]'
    }
  ],

  '/stock': [
    {
      titulo: 'Lo que tenés en el freezer',
      texto: 'Se calcula solo con lo que entra y sale en los pedidos. No hay que cargar nada a mano.'
    },
    {
      titulo: 'Las filas en rojo',
      texto: 'Son los artículos sin stock. Tocá cualquier fila para ver todos los movimientos ' +
        'de ese producto.',
      selector: '[data-tutorial="tabla-stock"]'
    }
  ],

  '/cuentas-corrientes': [
    {
      titulo: 'Quién te debe y a quién le debés',
      texto: 'Arriba tenés la lista rápida: los clientes con saldo, de mayor a menor, y lo ' +
        'que le debés al proveedor.'
    },
    {
      titulo: 'El detalle de una cuenta',
      texto: 'Elegí una cuenta para ver todos sus movimientos: cada venta suma y cada cobro ' +
        'resta, con el saldo al costado.',
      selector: '[data-tutorial="selector-cuenta"]'
    },
    {
      titulo: 'Refuerzo y Consumo Propio',
      texto: 'Esas dos cuentas son informativas. Lo que muestran ya está sumado dentro del ' +
        'saldo del proveedor: no se debe dos veces.'
    }
  ],

  '/movimientos': [
    {
      titulo: 'Registrar plata',
      texto: 'Cobro es lo que te paga un cliente. Pago es lo que le pagás al proveedor.'
    },
    {
      titulo: 'Te muestra cuánto debe',
      texto: 'Al elegir la cuenta aparece el saldo actual debajo del importe, para que sepas ' +
        'cuánto te tienen que pagar antes de cargarlo.',
      selector: '[data-tutorial="formulario-movimiento"]'
    }
  ],

  '/remitos': [
    {
      titulo: 'Remitos de clientes y de proveedor',
      texto: 'Una pestaña para ver e imprimir el remito de cada venta, otra con lo que hay que ' +
        'pedirle al proveedor en un período.'
    },
    {
      titulo: 'Junta todo',
      texto: 'El remito de proveedor incluye lo que no tenías en stock de las ventas, los refuerzos ' +
        'y los consumos, sumado por artículo en todo el período. Con Imprimir lo mandás o lo guardás ' +
        'como PDF.',
      selector: '[data-tutorial="acciones-remito"]'
    }
  ],

  '/articulos': [
    {
      titulo: 'Tus productos y precios',
      texto: 'Acá cargás lo que vendés, con el costo y los tres precios.'
    },
    {
      titulo: 'El margen te lo calcula',
      texto: 'Al editar, debajo de cada precio te dice qué ganancia te queda contra el costo. ' +
        'Los precios los escribís vos, redondeados como quieras cobrarlos.',
      selector: '[data-tutorial="nuevo-articulo"]'
    }
  ],

  '/cuentas': [
    {
      titulo: 'Tus clientes',
      texto: 'Cargá acá a cada cliente. Si le hacés siempre el mismo descuento, ponelo una ' +
        'vez y se completa solo en cada pedido.'
    },
    {
      titulo: 'Las tres cuentas especiales',
      texto: 'Proveedor, Refuerzo Stock y Consumo Propio ya vienen creadas y no se borran. ' +
        'El sistema las necesita para llevar el stock.',
      selector: '[data-tutorial="tabla-cuentas"]'
    }
  ]
};

/** Devuelve los pasos de una ruta, ignorando parametros como /remitos/cliente/12. */
export function pasosDe(ruta: string): PasoTutorial[] {
  return PASOS[ruta] ?? [];
}
