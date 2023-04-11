import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../wmis-component.test-samples';

import { WMISComponentFormService } from './wmis-component-form.service';

describe('WMISComponent Form Service', () => {
  let service: WMISComponentFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WMISComponentFormService);
  });

  describe('Service methods', () => {
    describe('createWMISComponentFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createWMISComponentFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            componentName: expect.any(Object),
            description: expect.any(Object),
            instance: expect.any(Object),
          })
        );
      });

      it('passing IWMISComponent should create a new form with FormGroup', () => {
        const formGroup = service.createWMISComponentFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            componentName: expect.any(Object),
            description: expect.any(Object),
            instance: expect.any(Object),
          })
        );
      });
    });

    describe('getWMISComponent', () => {
      it('should return NewWMISComponent for default WMISComponent initial value', () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const formGroup = service.createWMISComponentFormGroup(sampleWithNewData);

        const wMISComponent = service.getWMISComponent(formGroup) as any;

        expect(wMISComponent).toMatchObject(sampleWithNewData);
      });

      it('should return NewWMISComponent for empty WMISComponent initial value', () => {
        const formGroup = service.createWMISComponentFormGroup();

        const wMISComponent = service.getWMISComponent(formGroup) as any;

        expect(wMISComponent).toMatchObject({});
      });

      it('should return IWMISComponent', () => {
        const formGroup = service.createWMISComponentFormGroup(sampleWithRequiredData);

        const wMISComponent = service.getWMISComponent(formGroup) as any;

        expect(wMISComponent).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IWMISComponent should not enable id FormControl', () => {
        const formGroup = service.createWMISComponentFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewWMISComponent should disable id FormControl', () => {
        const formGroup = service.createWMISComponentFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
