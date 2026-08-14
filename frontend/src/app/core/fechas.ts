/** El backend usa LocalDate: las fechas viajan como 'aaaa-mm-dd', sin zona horaria. */
export function aTexto(fecha: Date | null): string | undefined {
  if (!fecha) {
    return undefined;
  }
  const mes = `${fecha.getMonth() + 1}`.padStart(2, '0');
  const dia = `${fecha.getDate()}`.padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

export function aFecha(texto: string): Date {
  const [anio, mes, dia] = texto.split('-').map(Number);
  return new Date(anio, mes - 1, dia);
}

export function primerDiaDelMes(): Date {
  const hoy = new Date();
  return new Date(hoy.getFullYear(), hoy.getMonth(), 1);
}
