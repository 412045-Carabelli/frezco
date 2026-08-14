import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { MessageModule } from 'primeng/message';
import { MARCA } from '../../config/marca';
import { SesionService } from '../../core/sesion.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, PasswordModule, MessageModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {

  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  readonly marca = MARCA;
  readonly usuario = signal('');
  readonly clave = signal('');
  readonly cargando = signal(false);
  readonly error = signal('');

  ingresar(): void {
    if (!this.usuario() || !this.clave()) {
      this.error.set('Completa usuario y clave');
      return;
    }

    this.cargando.set(true);
    this.error.set('');

    this.sesion.login(this.usuario(), this.clave()).subscribe({
      next: () => {
        this.cargando.set(false);
        this.router.navigate(['/resumen']);
      },
      error: (respuesta) => {
        this.cargando.set(false);
        this.error.set(respuesta.status === 401
          ? 'Usuario o clave incorrectos'
          : 'No se pudo conectar con el servidor');
      }
    });
  }
}
