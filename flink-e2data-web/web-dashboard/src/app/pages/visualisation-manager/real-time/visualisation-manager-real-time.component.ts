import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ElementRef,
  ViewChild
} from '@angular/core';
import { Subject } from 'rxjs';
import { YarnService } from 'services';
import { YarnNodeInfoInterface } from 'interfaces';
import { Network, DataSet } from 'vis';

@Component({
  selector: 'visualisation-manager-realtime',
  templateUrl: './visualisation-manager-real-time.component.html',
  styleUrls: ['./visualisation-manager-real-time.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VisualisationManagerRealtimeComponent implements OnInit, OnDestroy {
  readonly listKey = 'list';
  readonly topologyKey = 'topology';

  private networkElement: ElementRef;
  @ViewChild('network') set content(content: ElementRef) {
    if (content) {
      // initially setter gets called with undefined
      this.networkElement = content;
      this.createNetworkTopology();
    }
  }
  private networkVis: any;

  destroy$ = new Subject();
  isLoading = true;
  nodes: YarnNodeInfoInterface[];
  selectedNode: any;

  subTabs = [{ key: this.listKey, title: 'List' }, { key: this.topologyKey, title: 'Topology' }];
  selectedTab = this.listKey;

  log(val: any) {
    console.log(val);
  }

  onSelect(node: any) {
    this.selectedNode = node;
    this.cdr.markForCheck();
  }

  showPage(key: string) {
    this.selectedNode = null;
    this.selectedTab = key;
    console.log(this.selectedTab);
    this.cdr.markForCheck();
  }

  createNetworkTopology() {
    const container = this.networkElement.nativeElement;
    const nodesData = new DataSet<any>([{ id: 'network', label: 'Network' }]);
    const edges = new DataSet<any>([]);

    this.nodes.forEach(n => {
      nodesData.add({ id: n.id, label: n.id });
      edges.add({ from: 'network', to: n.id });
    });

    const data = { nodes: nodesData, edges };

    this.networkVis = new Network(container, data, {});
    this.networkVis.on('click', (properties: any) => {
      let nodeId = properties.nodes[0];
      const node = this.nodes.find(n => n.id === nodeId);
      this.onSelect(node);
    });
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

  //   ngAfterViewInit() {
  //     this.createNetworkTopology()
  //   }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
