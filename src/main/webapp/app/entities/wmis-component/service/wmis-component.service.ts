import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IWMISComponent, NewWMISComponent } from '../wmis-component.model';

export type PartialUpdateWMISComponent = Partial<IWMISComponent> & Pick<IWMISComponent, 'id'>;

export type EntityResponseType = HttpResponse<IWMISComponent>;
export type EntityArrayResponseType = HttpResponse<IWMISComponent[]>;

@Injectable({ providedIn: 'root' })
export class WMISComponentService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/wmis-components');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/_search/wmis-components');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(wMISComponent: NewWMISComponent): Observable<EntityResponseType> {
    return this.http.post<IWMISComponent>(this.resourceUrl, wMISComponent, { observe: 'response' });
  }

  update(wMISComponent: IWMISComponent): Observable<EntityResponseType> {
    return this.http.put<IWMISComponent>(`${this.resourceUrl}/${this.getWMISComponentIdentifier(wMISComponent)}`, wMISComponent, {
      observe: 'response',
    });
  }

  partialUpdate(wMISComponent: PartialUpdateWMISComponent): Observable<EntityResponseType> {
    return this.http.patch<IWMISComponent>(`${this.resourceUrl}/${this.getWMISComponentIdentifier(wMISComponent)}`, wMISComponent, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IWMISComponent>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IWMISComponent[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IWMISComponent[]>(this.resourceSearchUrl, { params: options, observe: 'response' });
  }

  getWMISComponentIdentifier(wMISComponent: Pick<IWMISComponent, 'id'>): number {
    return wMISComponent.id;
  }

  compareWMISComponent(o1: Pick<IWMISComponent, 'id'> | null, o2: Pick<IWMISComponent, 'id'> | null): boolean {
    return o1 && o2 ? this.getWMISComponentIdentifier(o1) === this.getWMISComponentIdentifier(o2) : o1 === o2;
  }

  addWMISComponentToCollectionIfMissing<Type extends Pick<IWMISComponent, 'id'>>(
    wMISComponentCollection: Type[],
    ...wMISComponentsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const wMISComponents: Type[] = wMISComponentsToCheck.filter(isPresent);
    if (wMISComponents.length > 0) {
      const wMISComponentCollectionIdentifiers = wMISComponentCollection.map(
        wMISComponentItem => this.getWMISComponentIdentifier(wMISComponentItem)!
      );
      const wMISComponentsToAdd = wMISComponents.filter(wMISComponentItem => {
        const wMISComponentIdentifier = this.getWMISComponentIdentifier(wMISComponentItem);
        if (wMISComponentCollectionIdentifiers.includes(wMISComponentIdentifier)) {
          return false;
        }
        wMISComponentCollectionIdentifiers.push(wMISComponentIdentifier);
        return true;
      });
      return [...wMISComponentsToAdd, ...wMISComponentCollection];
    }
    return wMISComponentCollection;
  }
}
