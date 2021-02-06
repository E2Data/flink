import { Component, Input } from '@angular/core';
import { YarnNodeInfoInterface } from 'interfaces';

@Component({
  selector: 'node-details',
  templateUrl: './node-details.component.html',
  styleUrls: ['./node-details.component.less']
})
export class NodeDetailsComponent {
  @Input()
  node: YarnNodeInfoInterface;
  @Input()
  pduInfo: string;

  constructor() {}
}
