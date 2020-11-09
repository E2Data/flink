import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';

@Component({
  selector: 'visualisation-manager-historical',
  templateUrl: './visualisation-manager-historical.component.html',
  styleUrls: ['./visualisation-manager-historical.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VisualisationManagerHistoricalComponent implements OnInit, OnDestroy {
  destroy$ = new Subject();
  isLoading = false;

  constructor() {}

  ngOnInit() {
    this.isLoading = true;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
