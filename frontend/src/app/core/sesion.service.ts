import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

/** Shape real de auth-service (com.auth.dto.AuthResponse): campos en snake_case. */
interface AuthResponse {
  access_token: string;
  refresh_token: string;
}

interface ClaimsToken {
  username: string;
  rol: string;
  organizacionId: number;
  aplicaciones: string[];
}

/** Frezco es tenant unico en la base de auth compartida — coincide con GatewayAuthFilter. */
const ORGANIZACION_FREZCO = 1;

const CLAVE_ACCESS_TOKEN = 'frezco.accessToken';
const CLAVE_REFRESH_TOKEN = 'frezco.refreshToken';

@Injectable({ providedIn: 'root' })
export class SesionService {

  private readonly http = inject(HttpClient);
  private readonly base = `${environment.gatewayUrl}/auth`;

  private readonly token = signal<string | null>(localStorage.getItem(CLAVE_ACCESS_TOKEN));

  readonly autenticado = computed(() => this.token() !== null);
  readonly usuario = computed(() => {
    const token = this.token();
    return token ? this.decodificarClaims(token).username : '';
  });

  accessToken(): string | null {
    return this.token();
  }

  login(usuario: string, clave: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/login`, { email: usuario, password: clave })
      .pipe(tap(respuesta => this.guardarTokens(respuesta)));
  }

  logout(): Observable<void> {
    const refreshToken = localStorage.getItem(CLAVE_REFRESH_TOKEN);
    return this.http.post<void>(`${this.base}/logout`, { refreshToken })
      .pipe(tap(() => this.limpiarTokens()));
  }

  /**
   * El login es del ecosistema Buildr entero, no de Frezco: cualquier usuario con
   * contraseña valida consigue token. Esto replica en el cliente lo que el Gateway ya
   * exige en el servidor (ver JwtAuthFilter) para no mostrar la app antes de que la
   * primera llamada a la API la rechace igual.
   */
  tieneAccesoFrezco(): boolean {
    const token = this.token();
    if (!token) return false;
    const claims = this.decodificarClaims(token);
    return claims.organizacionId === ORGANIZACION_FREZCO
      && (claims.aplicaciones ?? []).includes('FREZCO');
  }

  /** Descarta el token sin avisarle al Gateway (login exitoso pero sin acceso a Frezco). */
  descartarSesion(): void {
    this.limpiarTokens();
  }

  private guardarTokens(respuesta: AuthResponse): void {
    localStorage.setItem(CLAVE_ACCESS_TOKEN, respuesta.access_token);
    localStorage.setItem(CLAVE_REFRESH_TOKEN, respuesta.refresh_token);
    this.token.set(respuesta.access_token);
  }

  private limpiarTokens(): void {
    localStorage.removeItem(CLAVE_ACCESS_TOKEN);
    localStorage.removeItem(CLAVE_REFRESH_TOKEN);
    this.token.set(null);
  }

  /** El JWT lo emite y valida el Gateway; aca solo se lee el payload para mostrar el usuario. */
  private decodificarClaims(token: string): ClaimsToken {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload));
  }
}
