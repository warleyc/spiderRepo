import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { WMISComponentService } from '../service/wmis-component.service';

import { WMISComponentComponent } from './wmis-component.component';

describe('WMISComponent Management Component', () => {
  let comp: WMISComponentComponent;
  let fixture: ComponentFixture<WMISComponentComponent>;
  let service: WMISComponentService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([{ path: 'wmis-component', component: WMISComponentComponent }]), HttpClientTestingModule],
      declarations: [WMISComponentComponent],
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
      .overrideTemplate(WMISComponentComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(WMISComponentComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(WMISComponentService);

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
    expect(comp.wMISComponents?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to wMISComponentService', () => {
      const entity = { id: 123 };
      jest.spyOn(service, 'getWMISComponentIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getWMISComponentIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });
});
