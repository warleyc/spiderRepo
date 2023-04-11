import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { IWMISComponent } from '../wmis-component.model';
import { WMISComponentService } from '../service/wmis-component.service';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';

@Component({
  templateUrl: './wmis-component-delete-dialog.component.html',
})
export class WMISComponentDeleteDialogComponent {
  wMISComponent?: IWMISComponent;

  constructor(protected wMISComponentService: WMISComponentService, protected activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.wMISComponentService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
