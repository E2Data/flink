import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'visualisation-manager',
  templateUrl: './visualisation-manager.component.html',
  styleUrls: ['./visualisation-manager.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VisualisationManagerComponent {
  listOfNavigation = [{ path: 'realtime', title: 'Real time' }, { path: 'historical', title: 'Historical' }];
}
