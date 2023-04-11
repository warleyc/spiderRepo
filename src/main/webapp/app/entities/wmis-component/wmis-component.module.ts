import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { WMISComponentComponent } from './list/wmis-component.component';
import { WMISComponentDetailComponent } from './detail/wmis-component-detail.component';
import { WMISComponentUpdateComponent } from './update/wmis-component-update.component';
import { WMISComponentDeleteDialogComponent } from './delete/wmis-component-delete-dialog.component';
import { WMISComponentRoutingModule } from './route/wmis-component-routing.module';

@NgModule({
  imports: [SharedModule, WMISComponentRoutingModule],
  declarations: [WMISComponentComponent, WMISComponentDetailComponent, WMISComponentUpdateComponent, WMISComponentDeleteDialogComponent],
})
export class WMISComponentModule {}
