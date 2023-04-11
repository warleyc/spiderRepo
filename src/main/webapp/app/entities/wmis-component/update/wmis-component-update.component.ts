import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { WMISComponentFormService, WMISComponentFormGroup } from './wmis-component-form.service';
import { IWMISComponent } from '../wmis-component.model';
import { WMISComponentService } from '../service/wmis-component.service';
import { IInstance } from 'app/entities/instance/instance.model';
import { InstanceService } from 'app/entities/instance/service/instance.service';

@Component({
  selector: 'jhi-wmis-component-update',
  templateUrl: './wmis-component-update.component.html',
})
export class WMISComponentUpdateComponent implements OnInit {
  isSaving = false;
  wMISComponent: IWMISComponent | null = null;

  instancesSharedCollection: IInstance[] = [];

  editForm: WMISComponentFormGroup = this.wMISComponentFormService.createWMISComponentFormGroup();

  constructor(
    protected wMISComponentService: WMISComponentService,
    protected wMISComponentFormService: WMISComponentFormService,
    protected instanceService: InstanceService,
    protected activatedRoute: ActivatedRoute
  ) {}

  compareInstance = (o1: IInstance | null, o2: IInstance | null): boolean => this.instanceService.compareInstance(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ wMISComponent }) => {
      this.wMISComponent = wMISComponent;
      if (wMISComponent) {
        this.updateForm(wMISComponent);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const wMISComponent = this.wMISComponentFormService.getWMISComponent(this.editForm);
    if (wMISComponent.id !== null) {
      this.subscribeToSaveResponse(this.wMISComponentService.update(wMISComponent));
    } else {
      this.subscribeToSaveResponse(this.wMISComponentService.create(wMISComponent));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IWMISComponent>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(wMISComponent: IWMISComponent): void {
    this.wMISComponent = wMISComponent;
    this.wMISComponentFormService.resetForm(this.editForm, wMISComponent);

    this.instancesSharedCollection = this.instanceService.addInstanceToCollectionIfMissing<IInstance>(
      this.instancesSharedCollection,
      wMISComponent.instance
    );
  }

  protected loadRelationshipsOptions(): void {
    this.instanceService
      .query()
      .pipe(map((res: HttpResponse<IInstance[]>) => res.body ?? []))
      .pipe(
        map((instances: IInstance[]) =>
          this.instanceService.addInstanceToCollectionIfMissing<IInstance>(instances, this.wMISComponent?.instance)
        )
      )
      .subscribe((instances: IInstance[]) => (this.instancesSharedCollection = instances));
  }
}
