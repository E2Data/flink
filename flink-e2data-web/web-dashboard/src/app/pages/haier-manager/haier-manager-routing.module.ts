import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HaierManagerComponent } from './haier-manager.component';
import { HaierManagerConfigurationComponent } from './configuration/haier-manager-configuration.component';
import { HaierManagerLogsComponent } from './logs/haier-manager-logs.component';

const routes: Routes = [
  {
    path: '',
    component: HaierManagerComponent,
    children: [
      {
        path: 'config',
        component: HaierManagerConfigurationComponent,
        data: {
          path: 'config'
        }
      },
      {
        path: 'logs',
        component: HaierManagerLogsComponent,
        data: {
          path: 'logs'
        }
      },
      {
        path: '**',
        redirectTo: 'config',
        pathMatch: 'full'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class HaierManagerRoutingModule {}
