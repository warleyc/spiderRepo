import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'application',
        data: { pageTitle: 'spiderApp.application.home.title' },
        loadChildren: () => import('./application/application.module').then(m => m.ApplicationModule),
      },
      {
        path: 'instance',
        data: { pageTitle: 'spiderApp.instance.home.title' },
        loadChildren: () => import('./instance/instance.module').then(m => m.InstanceModule),
      },
      {
        path: 'wmis-component',
        data: { pageTitle: 'spiderApp.wMISComponent.home.title' },
        loadChildren: () => import('./wmis-component/wmis-component.module').then(m => m.WMISComponentModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
