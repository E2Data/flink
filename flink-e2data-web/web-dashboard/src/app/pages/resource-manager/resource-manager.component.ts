import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'resource-manager',
  templateUrl: './resource-manager.component.html',
  styleUrls: ['./resource-manager.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResourceManagerComponent {}
