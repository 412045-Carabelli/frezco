import { Component } from '@angular/core';
import { TabViewModule } from 'primeng/tabview';
import { RemitosClientesComponent } from './remitos-clientes.component';
import { RemitoProveedorComponent } from './remito-proveedor.component';
import { LayoutHeaderComponent } from '../../shared/layout-header/layout-header.component';

/** Remitos de cliente y de proveedor en un solo modulo, con pestañas. */
@Component({
  selector: 'app-remitos',
  standalone: true,
  imports: [TabViewModule, RemitosClientesComponent, RemitoProveedorComponent, LayoutHeaderComponent],
  templateUrl: './remitos.component.html'
})
export class RemitosComponent {
}
