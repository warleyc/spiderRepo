import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IWMISComponent } from '../wmis-component.model';

@Component({
  selector: 'jhi-wmis-component-detail',
  templateUrl: './wmis-component-detail.component.html',
})
export class WMISComponentDetailComponent implements OnInit {
  wMISComponent: IWMISComponent | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ wMISComponent }) => {
      this.wMISComponent = wMISComponent;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
