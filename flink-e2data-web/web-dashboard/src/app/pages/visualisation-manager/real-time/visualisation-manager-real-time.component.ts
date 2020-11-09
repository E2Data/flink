import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';

import { EChartOption } from 'echarts';

@Component({
  selector: 'visualisation-manager-realtime',
  templateUrl: './visualisation-manager-real-time.component.html',
  styleUrls: ['./visualisation-manager-real-time.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VisualisationManagerRealtimeComponent implements OnInit, OnDestroy {
  destroy$ = new Subject();
  isLoading = true;

  chartOption: EChartOption = {
    xAxis: {
      type: 'category',
      data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: [820, 932, 901, 934, 1290, 1330, 1320],
        type: 'line'
      }
    ]
  };

  constructor() {}

  ngOnInit() {}

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
