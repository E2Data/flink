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
import { StatusService, YarnService } from 'services';
import { YarnNodeInfoInterface, ResourceManagerNodeInterface } from 'interfaces';
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

  private networkElement: ElementRef;
  @ViewChild('network') set content(content: ElementRef) {
    if (content && this.networkVis == null) {
      // initially setter gets called with undefined
      this.networkElement = content;
      this.createNetworkTopology();
      this.networkVis.fit({ minZoomLevel: 1.2, maxZoomLevel: 1.2 });
      console.log(this.networkVis.getScale());
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
    this.networkVis = null;
    console.log(this.selectedTab);
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

    console.log(this.nodesInfo);
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
      modules.push(r.name);
    });
    const modulesString = modules.join();

    const hasCpu = modulesString.includes(this.CPU);
    const hasGpu = modulesString.includes(this.GPU);
    const hasFpga = modulesString.includes(this.FPGA);

    var usingCpu = false;
    var usingGpu = false;
    var usingFpga = false;
    node.usedResource.resourceInformations.resourceInformation.forEach((r: any) => {
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

    console.log(item);
    return item;
  }

  constructor(private cdr: ChangeDetectorRef, private yarnService: YarnService, private statusService: StatusService) {}

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
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
