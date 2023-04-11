import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { InstanceService } from '../service/instance.service';

import { InstanceComponent } from './instance.component';

describe('Instance Management Component', () => {
  let comp: InstanceComponent;
  let fixture: ComponentFixture<InstanceComponent>;
  let service: InstanceService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([{ path: 'instance', component: InstanceComponent }]), HttpClientTestingModule],
      declarations: [InstanceComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            data: of({
              defaultSort: 'id,asc',
            }),
            queryParamMap: of(
              jest.requireActual('@angular/router').convertToParamMap({
                page: '1',
                size: '1',
                sort: 'id,desc',
              })
            ),
            snapshot: { queryParams: {} },
          },
        },
      ],
    })
      .overrideTemplate(InstanceComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(InstanceComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(InstanceService);

    const headers = new HttpHeaders();
    jest.spyOn(service, 'query').mockReturnValue(
      of(
        new HttpResponse({
          body: [{ id: 123 }],
          headers,
        })
      )
    );
  });

  it('Should call load all on init', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenCalled();
    expect(comp.instances?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to instanceService', () => {
      const entity = { id: 123 };
      jest.spyOn(service, 'getInstanceIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getInstanceIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });
});
