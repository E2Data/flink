import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { VisualisationManagerComponent } from './visualisation-manager.component';
import { VisualisationManagerRealtimeComponent } from './real-time/visualisation-manager-real-time.component';
import { VisualisationManagerHistoricalComponent } from './historical/visualisation-manager-historical.component';

const routes: Routes = [
  {
    path: '',
    component: VisualisationManagerComponent,
    children: [
      {
        path: 'realtime',
        component: VisualisationManagerRealtimeComponent,
        data: {
          path: 'realtime'
        }
      },
      {
        path: 'historical',
        component: VisualisationManagerHistoricalComponent,
        data: {
          path: 'historical'
        }
      },
      {
        path: '**',
        redirectTo: 'realtime',
        pathMatch: 'full'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class VisualisationManagerRoutingModule {}
