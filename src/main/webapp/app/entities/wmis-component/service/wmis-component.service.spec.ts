import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IWMISComponent } from '../wmis-component.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../wmis-component.test-samples';

import { WMISComponentService } from './wmis-component.service';

const requireRestSample: IWMISComponent = {
  ...sampleWithRequiredData,
};

describe('WMISComponent Service', () => {
  let service: WMISComponentService;
  let httpMock: HttpTestingController;
  let expectedResult: IWMISComponent | IWMISComponent[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(WMISComponentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a WMISComponent', () => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const wMISComponent = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(wMISComponent).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a WMISComponent', () => {
      const wMISComponent = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(wMISComponent).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a WMISComponent', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of WMISComponent', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a WMISComponent', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addWMISComponentToCollectionIfMissing', () => {
      it('should add a WMISComponent to an empty array', () => {
        const wMISComponent: IWMISComponent = sampleWithRequiredData;
        expectedResult = service.addWMISComponentToCollectionIfMissing([], wMISComponent);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(wMISComponent);
      });

      it('should not add a WMISComponent to an array that contains it', () => {
        const wMISComponent: IWMISComponent = sampleWithRequiredData;
        const wMISComponentCollection: IWMISComponent[] = [
          {
            ...wMISComponent,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addWMISComponentToCollectionIfMissing(wMISComponentCollection, wMISComponent);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a WMISComponent to an array that doesn't contain it", () => {
        const wMISComponent: IWMISComponent = sampleWithRequiredData;
        const wMISComponentCollection: IWMISComponent[] = [sampleWithPartialData];
        expectedResult = service.addWMISComponentToCollectionIfMissing(wMISComponentCollection, wMISComponent);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(wMISComponent);
      });

      it('should add only unique WMISComponent to an array', () => {
        const wMISComponentArray: IWMISComponent[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const wMISComponentCollection: IWMISComponent[] = [sampleWithRequiredData];
        expectedResult = service.addWMISComponentToCollectionIfMissing(wMISComponentCollection, ...wMISComponentArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const wMISComponent: IWMISComponent = sampleWithRequiredData;
        const wMISComponent2: IWMISComponent = sampleWithPartialData;
        expectedResult = service.addWMISComponentToCollectionIfMissing([], wMISComponent, wMISComponent2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(wMISComponent);
        expect(expectedResult).toContain(wMISComponent2);
      });

      it('should accept null and undefined values', () => {
        const wMISComponent: IWMISComponent = sampleWithRequiredData;
        expectedResult = service.addWMISComponentToCollectionIfMissing([], null, wMISComponent, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(wMISComponent);
      });

      it('should return initial array if no WMISComponent is added', () => {
        const wMISComponentCollection: IWMISComponent[] = [sampleWithRequiredData];
        expectedResult = service.addWMISComponentToCollectionIfMissing(wMISComponentCollection, undefined, null);
        expect(expectedResult).toEqual(wMISComponentCollection);
      });
    });

    describe('compareWMISComponent', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareWMISComponent(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareWMISComponent(entity1, entity2);
        const compareResult2 = service.compareWMISComponent(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareWMISComponent(entity1, entity2);
        const compareResult2 = service.compareWMISComponent(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareWMISComponent(entity1, entity2);
        const compareResult2 = service.compareWMISComponent(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
