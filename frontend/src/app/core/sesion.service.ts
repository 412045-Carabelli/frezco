import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

interface EstadoSesion {
  autenticado: boolean;
  usuario: string;
}

@Injectable({ providedIn: 'root' })
export class SesionService {

  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/auth`;

  private readonly estado = signal<EstadoSesion>({ autenticado: false, usuario: '' });

  readonly autenticado = computed(() => this.estado().autenticado);
  readonly usuario = computed(() => this.estado().usuario);

  /** Se consulta al arrancar la app para decidir entre login y aplicacion. */
  verificar(): Observable<EstadoSesion> {
    return this.http.get<EstadoSesion>(`${this.base}/sesion`)
      .pipe(tap(estado => this.estado.set(estado)));
  }

  login(usuario: string, clave: string): Observable<{ usuario: string }> {
    return this.http.post<{ usuario: string }>(`${this.base}/login`, { usuario, clave })
      .pipe(tap(respuesta => this.estado.set({ autenticado: true, usuario: respuesta.usuario })));
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.base}/logout`, {})
      .pipe(tap(() => this.estado.set({ autenticado: false, usuario: '' })));
  }
}
