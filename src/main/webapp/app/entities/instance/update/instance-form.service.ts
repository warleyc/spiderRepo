import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IInstance, NewInstance } from '../instance.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IInstance for edit and NewInstanceFormGroupInput for create.
 */
type InstanceFormGroupInput = IInstance | PartialWithRequiredKeyOf<NewInstance>;

type InstanceFormDefaults = Pick<NewInstance, 'id'>;

type InstanceFormGroupContent = {
  id: FormControl<IInstance['id'] | NewInstance['id']>;
  countryName: FormControl<IInstance['countryName']>;
  application: FormControl<IInstance['application']>;
};

export type InstanceFormGroup = FormGroup<InstanceFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class InstanceFormService {
  createInstanceFormGroup(instance: InstanceFormGroupInput = { id: null }): InstanceFormGroup {
    const instanceRawValue = {
      ...this.getFormDefaults(),
      ...instance,
    };
    return new FormGroup<InstanceFormGroupContent>({
      id: new FormControl(
        { value: instanceRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      countryName: new FormControl(instanceRawValue.countryName),
      application: new FormControl(instanceRawValue.application),
    });
  }

  getInstance(form: InstanceFormGroup): IInstance | NewInstance {
    return form.getRawValue() as IInstance | NewInstance;
  }

  resetForm(form: InstanceFormGroup, instance: InstanceFormGroupInput): void {
    const instanceRawValue = { ...this.getFormDefaults(), ...instance };
    form.reset(
      {
        ...instanceRawValue,
        id: { value: instanceRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): InstanceFormDefaults {
    return {
      id: null,
    };
  }
}
