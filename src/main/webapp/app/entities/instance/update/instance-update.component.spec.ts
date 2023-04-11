import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { InstanceFormService } from './instance-form.service';
import { InstanceService } from '../service/instance.service';
import { IInstance } from '../instance.model';
import { IApplication } from 'app/entities/application/application.model';
import { ApplicationService } from 'app/entities/application/service/application.service';

import { InstanceUpdateComponent } from './instance-update.component';

describe('Instance Management Update Component', () => {
  let comp: InstanceUpdateComponent;
  let fixture: ComponentFixture<InstanceUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let instanceFormService: InstanceFormService;
  let instanceService: InstanceService;
  let applicationService: ApplicationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [InstanceUpdateComponent],
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
      .overrideTemplate(InstanceUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(InstanceUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    instanceFormService = TestBed.inject(InstanceFormService);
    instanceService = TestBed.inject(InstanceService);
    applicationService = TestBed.inject(ApplicationService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Application query and add missing value', () => {
      const instance: IInstance = { id: 456 };
      const application: IApplication = { id: 34344 };
      instance.application = application;

      const applicationCollection: IApplication[] = [{ id: 86874 }];
      jest.spyOn(applicationService, 'query').mockReturnValue(of(new HttpResponse({ body: applicationCollection })));
      const additionalApplications = [application];
      const expectedCollection: IApplication[] = [...additionalApplications, ...applicationCollection];
      jest.spyOn(applicationService, 'addApplicationToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ instance });
      comp.ngOnInit();

      expect(applicationService.query).toHaveBeenCalled();
      expect(applicationService.addApplicationToCollectionIfMissing).toHaveBeenCalledWith(
        applicationCollection,
        ...additionalApplications.map(expect.objectContaining)
      );
      expect(comp.applicationsSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const instance: IInstance = { id: 456 };
      const application: IApplication = { id: 98990 };
      instance.application = application;

      activatedRoute.data = of({ instance });
      comp.ngOnInit();

      expect(comp.applicationsSharedCollection).toContain(application);
      expect(comp.instance).toEqual(instance);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IInstance>>();
      const instance = { id: 123 };
      jest.spyOn(instanceFormService, 'getInstance').mockReturnValue(instance);
      jest.spyOn(instanceService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ instance });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: instance }));
      saveSubject.complete();

      // THEN
      expect(instanceFormService.getInstance).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(instanceService.update).toHaveBeenCalledWith(expect.objectContaining(instance));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IInstance>>();
      const instance = { id: 123 };
      jest.spyOn(instanceFormService, 'getInstance').mockReturnValue({ id: null });
      jest.spyOn(instanceService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ instance: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: instance }));
      saveSubject.complete();

      // THEN
      expect(instanceFormService.getInstance).toHaveBeenCalled();
      expect(instanceService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IInstance>>();
      const instance = { id: 123 };
      jest.spyOn(instanceService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ instance });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(instanceService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareApplication', () => {
      it('Should forward to applicationService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(applicationService, 'compareApplication');
        comp.compareApplication(entity, entity2);
        expect(applicationService.compareApplication).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
