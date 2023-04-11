import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { WMISComponentDetailComponent } from './wmis-component-detail.component';

describe('WMISComponent Management Detail Component', () => {
  let comp: WMISComponentDetailComponent;
  let fixture: ComponentFixture<WMISComponentDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [WMISComponentDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ wMISComponent: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(WMISComponentDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(WMISComponentDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load wMISComponent on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.wMISComponent).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
