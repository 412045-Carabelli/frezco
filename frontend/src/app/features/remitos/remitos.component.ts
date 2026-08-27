import { Component } from '@angular/core';
import { TabViewModule } from 'primeng/tabview';
import { RemitosClientesComponent } from './remitos-clientes.component';
import { RemitoProveedorComponent } from './remito-proveedor.component';

/** Remitos de cliente y de proveedor en un solo modulo, con pestañas. */
@Component({
  selector: 'app-remitos',
  standalone: true,
  imports: [TabViewModule, RemitosClientesComponent, RemitoProveedorComponent],
  templateUrl: './remitos.component.html'
})
export class RemitosComponent {
}
