import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { WMISComponentFormService } from './wmis-component-form.service';
import { WMISComponentService } from '../service/wmis-component.service';
import { IWMISComponent } from '../wmis-component.model';
import { IInstance } from 'app/entities/instance/instance.model';
import { InstanceService } from 'app/entities/instance/service/instance.service';

import { WMISComponentUpdateComponent } from './wmis-component-update.component';

describe('WMISComponent Management Update Component', () => {
  let comp: WMISComponentUpdateComponent;
  let fixture: ComponentFixture<WMISComponentUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let wMISComponentFormService: WMISComponentFormService;
  let wMISComponentService: WMISComponentService;
  let instanceService: InstanceService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [WMISComponentUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(WMISComponentUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(WMISComponentUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    wMISComponentFormService = TestBed.inject(WMISComponentFormService);
    wMISComponentService = TestBed.inject(WMISComponentService);
    instanceService = TestBed.inject(InstanceService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Instance query and add missing value', () => {
      const wMISComponent: IWMISComponent = { id: 456 };
      const instance: IInstance = { id: 38801 };
      wMISComponent.instance = instance;

      const instanceCollection: IInstance[] = [{ id: 49071 }];
      jest.spyOn(instanceService, 'query').mockReturnValue(of(new HttpResponse({ body: instanceCollection })));
      const additionalInstances = [instance];
      const expectedCollection: IInstance[] = [...additionalInstances, ...instanceCollection];
      jest.spyOn(instanceService, 'addInstanceToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ wMISComponent });
      comp.ngOnInit();

      expect(instanceService.query).toHaveBeenCalled();
      expect(instanceService.addInstanceToCollectionIfMissing).toHaveBeenCalledWith(
        instanceCollection,
        ...additionalInstances.map(expect.objectContaining)
      );
      expect(comp.instancesSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const wMISComponent: IWMISComponent = { id: 456 };
      const instance: IInstance = { id: 45235 };
      wMISComponent.instance = instance;

      activatedRoute.data = of({ wMISComponent });
      comp.ngOnInit();

      expect(comp.instancesSharedCollection).toContain(instance);
      expect(comp.wMISComponent).toEqual(wMISComponent);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IWMISComponent>>();
      const wMISComponent = { id: 123 };
      jest.spyOn(wMISComponentFormService, 'getWMISComponent').mockReturnValue(wMISComponent);
      jest.spyOn(wMISComponentService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ wMISComponent });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: wMISComponent }));
      saveSubject.complete();

      // THEN
      expect(wMISComponentFormService.getWMISComponent).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(wMISComponentService.update).toHaveBeenCalledWith(expect.objectContaining(wMISComponent));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IWMISComponent>>();
      const wMISComponent = { id: 123 };
      jest.spyOn(wMISComponentFormService, 'getWMISComponent').mockReturnValue({ id: null });
      jest.spyOn(wMISComponentService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ wMISComponent: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: wMISComponent }));
      saveSubject.complete();

      // THEN
      expect(wMISComponentFormService.getWMISComponent).toHaveBeenCalled();
      expect(wMISComponentService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IWMISComponent>>();
      const wMISComponent = { id: 123 };
      jest.spyOn(wMISComponentService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ wMISComponent });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(wMISComponentService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareInstance', () => {
      it('Should forward to instanceService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(instanceService, 'compareInstance');
        comp.compareInstance(entity, entity2);
        expect(instanceService.compareInstance).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
