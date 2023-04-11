import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { WMISComponentComponent } from '../list/wmis-component.component';
import { WMISComponentDetailComponent } from '../detail/wmis-component-detail.component';
import { WMISComponentUpdateComponent } from '../update/wmis-component-update.component';
import { WMISComponentRoutingResolveService } from './wmis-component-routing-resolve.service';
import { ASC } from 'app/config/navigation.constants';

const wMISComponentRoute: Routes = [
  {
    path: '',
    component: WMISComponentComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: WMISComponentDetailComponent,
    resolve: {
      wMISComponent: WMISComponentRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: WMISComponentUpdateComponent,
    resolve: {
      wMISComponent: WMISComponentRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: WMISComponentUpdateComponent,
    resolve: {
      wMISComponent: WMISComponentRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(wMISComponentRoute)],
  exports: [RouterModule],
})
export class WMISComponentRoutingModule {}
