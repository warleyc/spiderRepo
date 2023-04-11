import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IWMISComponent } from '../wmis-component.model';
import { WMISComponentService } from '../service/wmis-component.service';

@Injectable({ providedIn: 'root' })
export class WMISComponentRoutingResolveService implements Resolve<IWMISComponent | null> {
  constructor(protected service: WMISComponentService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IWMISComponent | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((wMISComponent: HttpResponse<IWMISComponent>) => {
          if (wMISComponent.body) {
            return of(wMISComponent.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(null);
  }
}
