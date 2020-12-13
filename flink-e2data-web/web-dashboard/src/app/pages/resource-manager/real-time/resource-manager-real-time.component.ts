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
import { YarnNodeInfoInterface, ResourceManagerNodeInterface } from 'interfaces';
import { Network, DataSet } from 'vis';
import { cpu, gpu, fpga } from './icons';

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

  parseNode(node): ResourceManagerNodeInterface {
    var modules = [];
    node.availableResource.resourceInformations.resourceInformation.forEach(r => {
      modules.push(r.name);
    });
    const modulesString = modules.join();
    console.log(modulesString);

    const hasCpu = modulesString.includes(this.CPU);
    const hasGpu = modulesString.includes(this.GPU);
    const hasFpga = modulesString.includes(this.FPGA);

    var usingCpu = false;
    var usingGpu = false;
    var usingFpga = false;
    var usedModules = [];
    node.usedResource.resourceInformations.resourceInformation.forEach(r => {
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
      nodes: node,
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

  constructor(private cdr: ChangeDetectorRef, private yarnService: YarnService) {}

  ngOnInit() {
    this.yarnService.updateNodeInformation().subscribe(data => {
      console.log(data);
      this.nodes = data.nodes.node;
      console.log(this.nodes);

      this.nodes.forEach(n => {
        const node = this.parseNode(n);
        this.nodesInfo.push(node);
      });

      this.cdr.markForCheck();
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
