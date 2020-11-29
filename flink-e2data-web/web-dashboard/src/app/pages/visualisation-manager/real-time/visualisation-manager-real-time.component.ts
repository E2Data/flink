import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { YarnService } from 'services';
import { YarnNodeInfoInterface } from 'interfaces';

@Component({
  selector: 'visualisation-manager-realtime',
  templateUrl: './visualisation-manager-real-time.component.html',
  styleUrls: ['./visualisation-manager-real-time.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VisualisationManagerRealtimeComponent implements OnInit, OnDestroy {
  destroy$ = new Subject();
  isLoading = true;
  nodes: YarnNodeInfoInterface[];
  selectedNode: YarnNodeInfoInterface;

  log(val: any) {
    console.log(val);
  }

  onSelect(node: YarnNodeInfoInterface) {
    this.selectedNode = node;
    this.cdr.markForCheck();
  }

  constructor(private cdr: ChangeDetectorRef, private yarnService: YarnService) {}

  ngOnInit() {
    this.yarnService.updateNodeInformation().subscribe(data => {
      console.log(data);
      this.nodes = data.nodes.node;
      console.log(this.nodes);
      this.cdr.markForCheck();
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
