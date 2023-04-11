import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { InstanceFormService, InstanceFormGroup } from './instance-form.service';
import { IInstance } from '../instance.model';
import { InstanceService } from '../service/instance.service';
import { IApplication } from 'app/entities/application/application.model';
import { ApplicationService } from 'app/entities/application/service/application.service';

@Component({
  selector: 'jhi-instance-update',
  templateUrl: './instance-update.component.html',
})
export class InstanceUpdateComponent implements OnInit {
  isSaving = false;
  instance: IInstance | null = null;

  applicationsSharedCollection: IApplication[] = [];

  editForm: InstanceFormGroup = this.instanceFormService.createInstanceFormGroup();

  constructor(
    protected instanceService: InstanceService,
    protected instanceFormService: InstanceFormService,
    protected applicationService: ApplicationService,
    protected activatedRoute: ActivatedRoute
  ) {}

  compareApplication = (o1: IApplication | null, o2: IApplication | null): boolean => this.applicationService.compareApplication(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ instance }) => {
      this.instance = instance;
      if (instance) {
        this.updateForm(instance);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const instance = this.instanceFormService.getInstance(this.editForm);
    if (instance.id !== null) {
      this.subscribeToSaveResponse(this.instanceService.update(instance));
    } else {
      this.subscribeToSaveResponse(this.instanceService.create(instance));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IInstance>>): void {
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

  protected updateForm(instance: IInstance): void {
    this.instance = instance;
    this.instanceFormService.resetForm(this.editForm, instance);

    this.applicationsSharedCollection = this.applicationService.addApplicationToCollectionIfMissing<IApplication>(
      this.applicationsSharedCollection,
      instance.application
    );
  }

  protected loadRelationshipsOptions(): void {
    this.applicationService
      .query()
      .pipe(map((res: HttpResponse<IApplication[]>) => res.body ?? []))
      .pipe(
        map((applications: IApplication[]) =>
          this.applicationService.addApplicationToCollectionIfMissing<IApplication>(applications, this.instance?.application)
        )
      )
      .subscribe((applications: IApplication[]) => (this.applicationsSharedCollection = applications));
  }
}
