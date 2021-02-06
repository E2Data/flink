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
import { takeUntil } from 'rxjs/operators';
import { StatusService, YarnService, PDUService } from 'services';
import { YarnNodeInfoInterface, ResourceManagerNodeInterface, E2DataPDU } from 'interfaces';
import { Network, DataSet } from 'vis';

@Component({
  selector: 'resource-manager-realtime',
  templateUrl: './resource-manager-real-time.component.html',
  styleUrls: ['./resource-manager-real-time.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResourceManagerRealtimeComponent implements OnInit, OnDestroy {
  readonly listKey = 'list';
  readonly topologyKey = 'topology';
  readonly CPU = 'vcores';
  readonly GPU = 'yarn.io/gpu';
  readonly FPGA = 'yarn.io/fpga';
  readonly currentKey = 'rPDU2OutletMeteredStatusCurrent';
  readonly energyKey = 'rPDU2OutletMeteredStatusEnergy';
  readonly powerKey = 'rPDU2OutletMeteredStatusPower';

  private networkElement: ElementRef;
  @ViewChild('network') set content(content: ElementRef) {
    if (content && this.networkVis == null) {
      // initially setter gets called with undefined
      this.networkElement = content;
      this.createNetworkTopology();
      this.networkVis.fit({ minZoomLevel: 1.2, maxZoomLevel: 1.2 });
      //       console.log(this.networkVis.getScale());
    }
  }
  private networkVis: any;

  destroy$ = new Subject();
  isLoading = true;
  nodes: YarnNodeInfoInterface[];
  selectedNode: any;
  nodeDetailsInfo: string = '';

  nodesInfo: ResourceManagerNodeInterface[] = [];

  subTabs = [{ key: this.listKey, title: 'List' }, { key: this.topologyKey, title: 'Topology' }];
  selectedTab = this.listKey;

  pduInfoText: string = '';
  pduData: E2DataPDU;
  pduMap: any = {
    silver1: '1',
    gold1: '2',
    gold3: '4',
    termi7: '5',
    termi8: '5',
    termi9: '6',
    termi10: '6',
    termi11: '7',
    termi12: '7',
    cognito: '22',
    quest: '23'
  };

  log(val: any) {
    console.log(val);
  }

  onSelect(node: any) {
    this.selectedNode = node;
    this.getPDUInfo(node.id);
    this.cdr.markForCheck();
  }

  showPage(key: string) {
    this.selectedNode = null;
    this.selectedTab = key;
    this.networkVis = null;
    //     console.log(this.selectedTab);
    this.cdr.markForCheck();
  }

  createNetworkTopology() {
    const container = this.networkElement.nativeElement;
    const nodesData = new DataSet<any>([
      {
        id: 'network',
        label: 'Network',
        image: 'assets/icons/cloud-computing.svg',
        shape: 'image',
        size: 20
      }
    ]);
    const edges = new DataSet<any>([]);

    //     console.log(this.nodesInfo);
    this.nodesInfo.forEach(n => {
      nodesData.add({
        id: n.node.id,
        label: n.node.id,
        image: 'assets/icons/server.svg',
        shape: 'image',
        size: 20
      });
      edges.add({ from: 'network', to: n.node.id });
    });

    const data = { nodes: nodesData, edges };
    const options = {
      interaction: {
        hover: true,
        hoverConnectedEdges: false,
        dragNodes: false,
        //         dragView: false,
        //         zoomView: false,
        selectConnectedEdges: false
      },
      nodes: {
        shape: 'box',
        widthConstraint: {
          minimum: 200
        },
        heightConstraint: {
          minimum: 40
        }
      },
      layout: {
        hierarchical: {
          direction: 'UD',
          sortMethod: 'directed',
          levelSeparation: 100
        }
      },
      edges: {
        arrows: 'to'
      }
    };

    this.networkVis = new Network(container, data, options);
    this.networkVis.fit();
    this.networkVis.on('click', (properties: any) => {
      let nodeId = properties.nodes[0];
      const node = this.nodes.find(n => n.id === nodeId);
      this.onSelect(node);
    });
    this.networkVis.on('hoverNode', (params: any) => {
      const nodeInfo = this.nodesInfo.find(n => n.node.id === params.node);
      if (nodeInfo) {
        var title = nodeInfo.node.id + '\n';

        var available = '';
        if (nodeInfo.hasCpu) available += 'CPU, ';
        if (nodeInfo.hasGpu) available += 'GPU, ';
        if (nodeInfo.hasFpga) available += 'FPGA';
        if (available.endsWith(', ')) available = available.slice(0, -2);
        if (available.length > 0) title += 'Available: ' + available + '\n';

        var using = '';
        if (nodeInfo.usingCpu) using += 'CPU, ';
        if (nodeInfo.usingGpu) using += 'GPU, ';
        if (nodeInfo.usingFpga) using += 'FPGA';
        if (using.endsWith(', ')) using = using.slice(0, -2);
        if (using.length > 0) title += 'Using: ' + using + '\n';

        const pduInfo = this.getPDUInfo(nodeInfo.node.id);
        if (pduInfo.length > 0) title += pduInfo;

        this.nodeDetailsInfo = title;

        this.cdr.markForCheck();
      }
    });
    this.networkVis.on('blurNode', () => {
      this.nodeDetailsInfo = '';
      this.cdr.markForCheck();
    });
  }

  parseNode(node: any): any {
    var modules: string[] = [];
    node.availableResource.resourceInformations.resourceInformation.forEach((r: any) => {
      if (r.value > 0) {
        modules.push(r.name);
      }
    });
    const modulesString = modules.join();

    const hasCpu = modulesString.includes(this.CPU);
    const hasGpu = modulesString.includes(this.GPU);
    const hasFpga = modulesString.includes(this.FPGA);

    var usingCpu = false;
    var usingGpu = false;
    var usingFpga = false;
    node.usedResource.resourceInformations.resourceInformation.forEach((r: any) => {
      //       console.log(r)
      if (r.name.includes(this.CPU) && r.value > 0) {
        usingCpu = true;
      }
      if (r.name.includes(this.GPU) && r.value > 0) {
        usingGpu = true;
      }
      if (r.name.includes(this.FPGA) && r.value > 0) {
        usingFpga = true;
      }
    });

    const item: ResourceManagerNodeInterface = {
      node: node,
      availableModules: modules.join(),
      hasCpu: hasCpu,
      hasGpu: hasGpu,
      hasFpga: hasFpga,
      usingCpu: usingCpu,
      usingGpu: usingGpu,
      usingFpga: usingFpga
    };

    //     console.log(item);
    return item;
  }

  getPDUInfo(name: string) {
    const keys = Object.keys(this.pduMap);
    for (var i = 0; i < keys.length; i++) {
      if (name.includes(keys[i])) {
        const value = this.pduMap[keys[i]];
        const current = this.pduData[this.currentKey + value];
        const energy = this.pduData[this.energyKey + value];
        const power = this.pduData[this.powerKey + value];

        return 'Current: ' + current + '\nEnergy: ' + energy + '\nPower: ' + power;
      }
    }

    return '';
  }

  constructor(
    private cdr: ChangeDetectorRef,
    private yarnService: YarnService,
    private statusService: StatusService,
    private pduService: PDUService
  ) {}

  ngOnInit() {
    this.statusService.refresh$.pipe(takeUntil(this.destroy$)).subscribe(_ => {
      this.yarnService.updateNodeInformation().subscribe(data => {
        const selectedNodeId = this.selectedNode ? this.selectedNode.id : null;
        this.nodes = data.nodes.node;
        this.nodesInfo = [];

        this.nodes.forEach(n => {
          const node = this.parseNode(n);
          this.nodesInfo.push(node);

          if (selectedNodeId && n.id === selectedNodeId) {
            this.selectedNode = n;
          }
        });

        this.cdr.markForCheck();
      });

      this.pduService.updatePDUMetrics().subscribe(data => {
        this.pduData = data;
        this.cdr.markForCheck();
      });
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
