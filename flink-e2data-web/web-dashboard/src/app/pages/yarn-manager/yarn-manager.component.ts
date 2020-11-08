import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { YarnService } from 'services';
import { YarnClusterMetricsInterface } from 'interfaces';

@Component({
  selector: 'yarn-manager',
  templateUrl: './yarn-manager.component.html',
  styleUrls: ['./yarn-manager.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class YarnManagerComponent implements OnInit, OnDestroy {
  destroy$ = new Subject();
  isLoading = false;
  dataSet: YarnClusterMetricsInterface[] = [];

  constructor(private cdr: ChangeDetectorRef, private yarnService: YarnService) {}

  ngOnInit() {
    this.yarnService.updateMetrics().subscribe(data => {
      //       console.log(data);
      const metrics = data.clusterMetrics;
      //       console.log(metrics)
      this.dataSet = [metrics];
      this.isLoading = false;
      console.log(this.dataSet);
      this.cdr.markForCheck();
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
