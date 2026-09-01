export const environment = {
  produccion: false,
  // Pega contra el API Gateway compartido de Buildr (Path=/api/frezco/**), no directo al
  // backend. El Gateway valida el JWT, inyecta los headers de identidad y reenvia a este
  // backend sacando el segmento "frezco" (RewritePath). Sin pasar por el Gateway, el
  // backend rechaza todo con 401 (GatewayAuthFilter no ve esos headers).
  apiUrl: '/api/frezco',
  // El login pega contra el Gateway tambien, pero en su raiz (Path=/auth/**), no bajo
  // /api/frezco.
  gatewayUrl: ''
};
