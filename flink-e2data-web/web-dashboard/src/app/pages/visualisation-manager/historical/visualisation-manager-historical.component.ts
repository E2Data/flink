import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';

import { EChartOption } from 'echarts';

@Component({
  selector: 'visualisation-manager-historical',
  templateUrl: './visualisation-manager-historical.component.html',
  styleUrls: ['./visualisation-manager-historical.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VisualisationManagerHistoricalComponent implements OnInit, OnDestroy {
  destroy$ = new Subject();
  isLoading = true;

  realtimeChartOptions: any;
  updateOptions: any;

  private oneDay = 24 * 3600 * 1000;
  private now: Date;
  private value: number;
  private data: any[];
  private timer: any;

  chartOption: EChartOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      data: ['Available', 'Allocated', 'Free']
    },
    series: [
      {
        name: 'Cores',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '30',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [{ value: 335, name: 'Available' }, { value: 310, name: 'Allocated' }, { value: 234, name: 'Free' }]
      }
    ]
  };

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    // generate some random testing data:
    this.data = [];
    this.now = new Date(1997, 9, 3);
    this.value = Math.random() * 1000;

    for (let i = 0; i < 1000; i++) {
      this.data.push(this.randomData());
    }

    this.realtimeChartOptions = {
      title: {
        text: 'Dynamic Data + Time Axis'
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          params = params[0];
          const date = new Date(params.name);
          return date.getDate() + '/' + (date.getMonth() + 1) + '/' + date.getFullYear() + ' : ' + params.value[1];
        },
        axisPointer: {
          animation: false
        }
      },
      xAxis: {
        type: 'time',
        splitLine: {
          show: false
        }
      },
      yAxis: {
        type: 'value',
        boundaryGap: [0, '100%'],
        splitLine: {
          show: false
        }
      },
      series: [
        {
          name: 'Mocking Data',
          type: 'line',
          showSymbol: false,
          hoverAnimation: false,
          data: this.data
        }
      ]
    };

    this.timer = setInterval(() => {
      for (let i = 0; i < 5; i++) {
        this.data.shift();
        this.data.push(this.randomData());
      }

      // update series data:
      this.updateOptions = {
        series: [
          {
            data: this.data
          }
        ]
      };

      this.cdr.markForCheck();
    }, 1000);
  }

  ngOnDestroy() {
    clearInterval(this.timer);
    this.destroy$.next();
    this.destroy$.complete();
  }

  randomData() {
    this.now = new Date(this.now.getTime() + this.oneDay);
    this.value = this.value + Math.random() * 21 - 10;
    return {
      name: this.now.toString(),
      value: [[this.now.getFullYear(), this.now.getMonth() + 1, this.now.getDate()].join('/'), Math.round(this.value)]
    };
  }
}
