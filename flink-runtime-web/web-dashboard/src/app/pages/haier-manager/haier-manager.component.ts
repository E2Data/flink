import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'haier-manager',
  templateUrl: './haier-manager.component.html',
  styleUrls: ['./haier-manager.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HaierManagerComponent {
  listOfNavigation = [{ path: 'config', title: 'Configuration' }, { path: 'logs', title: 'Logs' }];
}
