import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ResourceManagerComponent } from './resource-manager.component';

const routes: Routes = [{ path: '', component: ResourceManagerComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ResourceManagerRoutingModule {}
