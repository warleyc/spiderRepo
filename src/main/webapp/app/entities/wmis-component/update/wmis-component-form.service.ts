import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IWMISComponent, NewWMISComponent } from '../wmis-component.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IWMISComponent for edit and NewWMISComponentFormGroupInput for create.
 */
type WMISComponentFormGroupInput = IWMISComponent | PartialWithRequiredKeyOf<NewWMISComponent>;

type WMISComponentFormDefaults = Pick<NewWMISComponent, 'id'>;

type WMISComponentFormGroupContent = {
  id: FormControl<IWMISComponent['id'] | NewWMISComponent['id']>;
  componentName: FormControl<IWMISComponent['componentName']>;
  description: FormControl<IWMISComponent['description']>;
  instance: FormControl<IWMISComponent['instance']>;
};

export type WMISComponentFormGroup = FormGroup<WMISComponentFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class WMISComponentFormService {
  createWMISComponentFormGroup(wMISComponent: WMISComponentFormGroupInput = { id: null }): WMISComponentFormGroup {
    const wMISComponentRawValue = {
      ...this.getFormDefaults(),
      ...wMISComponent,
    };
    return new FormGroup<WMISComponentFormGroupContent>({
      id: new FormControl(
        { value: wMISComponentRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      componentName: new FormControl(wMISComponentRawValue.componentName),
      description: new FormControl(wMISComponentRawValue.description),
      instance: new FormControl(wMISComponentRawValue.instance),
    });
  }

  getWMISComponent(form: WMISComponentFormGroup): IWMISComponent | NewWMISComponent {
    return form.getRawValue() as IWMISComponent | NewWMISComponent;
  }

  resetForm(form: WMISComponentFormGroup, wMISComponent: WMISComponentFormGroupInput): void {
    const wMISComponentRawValue = { ...this.getFormDefaults(), ...wMISComponent };
    form.reset(
      {
        ...wMISComponentRawValue,
        id: { value: wMISComponentRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): WMISComponentFormDefaults {
    return {
      id: null,
    };
  }
}
