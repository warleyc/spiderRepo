import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IApplication, NewApplication } from '../application.model';

export type PartialUpdateApplication = Partial<IApplication> & Pick<IApplication, 'id'>;

export type EntityResponseType = HttpResponse<IApplication>;
export type EntityArrayResponseType = HttpResponse<IApplication[]>;

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/applications');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/_search/applications');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(application: NewApplication): Observable<EntityResponseType> {
    return this.http.post<IApplication>(this.resourceUrl, application, { observe: 'response' });
  }

  update(application: IApplication): Observable<EntityResponseType> {
    return this.http.put<IApplication>(`${this.resourceUrl}/${this.getApplicationIdentifier(application)}`, application, {
      observe: 'response',
    });
  }

  partialUpdate(application: PartialUpdateApplication): Observable<EntityResponseType> {
    return this.http.patch<IApplication>(`${this.resourceUrl}/${this.getApplicationIdentifier(application)}`, application, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IApplication>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IApplication[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IApplication[]>(this.resourceSearchUrl, { params: options, observe: 'response' });
  }

  getApplicationIdentifier(application: Pick<IApplication, 'id'>): number {
    return application.id;
  }

  compareApplication(o1: Pick<IApplication, 'id'> | null, o2: Pick<IApplication, 'id'> | null): boolean {
    return o1 && o2 ? this.getApplicationIdentifier(o1) === this.getApplicationIdentifier(o2) : o1 === o2;
  }

  addApplicationToCollectionIfMissing<Type extends Pick<IApplication, 'id'>>(
    applicationCollection: Type[],
    ...applicationsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const applications: Type[] = applicationsToCheck.filter(isPresent);
    if (applications.length > 0) {
      const applicationCollectionIdentifiers = applicationCollection.map(
        applicationItem => this.getApplicationIdentifier(applicationItem)!
      );
      const applicationsToAdd = applications.filter(applicationItem => {
        const applicationIdentifier = this.getApplicationIdentifier(applicationItem);
        if (applicationCollectionIdentifiers.includes(applicationIdentifier)) {
          return false;
        }
        applicationCollectionIdentifiers.push(applicationIdentifier);
        return true;
      });
      return [...applicationsToAdd, ...applicationCollection];
    }
    return applicationCollection;
  }
}
