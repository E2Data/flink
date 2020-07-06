import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HaierConfigurationComponent } from './haier-configuration.component';

const routes: Routes = [
  {
    path: '',
    component: HaierConfigurationComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class HaierConfigurationRoutingModule {}
