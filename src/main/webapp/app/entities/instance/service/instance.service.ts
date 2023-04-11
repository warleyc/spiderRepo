import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IInstance, NewInstance } from '../instance.model';

export type PartialUpdateInstance = Partial<IInstance> & Pick<IInstance, 'id'>;

export type EntityResponseType = HttpResponse<IInstance>;
export type EntityArrayResponseType = HttpResponse<IInstance[]>;

@Injectable({ providedIn: 'root' })
export class InstanceService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/instances');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/_search/instances');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(instance: NewInstance): Observable<EntityResponseType> {
    return this.http.post<IInstance>(this.resourceUrl, instance, { observe: 'response' });
  }

  update(instance: IInstance): Observable<EntityResponseType> {
    return this.http.put<IInstance>(`${this.resourceUrl}/${this.getInstanceIdentifier(instance)}`, instance, { observe: 'response' });
  }

  partialUpdate(instance: PartialUpdateInstance): Observable<EntityResponseType> {
    return this.http.patch<IInstance>(`${this.resourceUrl}/${this.getInstanceIdentifier(instance)}`, instance, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IInstance>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IInstance[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IInstance[]>(this.resourceSearchUrl, { params: options, observe: 'response' });
  }

  getInstanceIdentifier(instance: Pick<IInstance, 'id'>): number {
    return instance.id;
  }

  compareInstance(o1: Pick<IInstance, 'id'> | null, o2: Pick<IInstance, 'id'> | null): boolean {
    return o1 && o2 ? this.getInstanceIdentifier(o1) === this.getInstanceIdentifier(o2) : o1 === o2;
  }

  addInstanceToCollectionIfMissing<Type extends Pick<IInstance, 'id'>>(
    instanceCollection: Type[],
    ...instancesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const instances: Type[] = instancesToCheck.filter(isPresent);
    if (instances.length > 0) {
      const instanceCollectionIdentifiers = instanceCollection.map(instanceItem => this.getInstanceIdentifier(instanceItem)!);
      const instancesToAdd = instances.filter(instanceItem => {
        const instanceIdentifier = this.getInstanceIdentifier(instanceItem);
        if (instanceCollectionIdentifiers.includes(instanceIdentifier)) {
          return false;
        }
        instanceCollectionIdentifiers.push(instanceIdentifier);
        return true;
      });
      return [...instancesToAdd, ...instanceCollection];
    }
    return instanceCollection;
  }
}
