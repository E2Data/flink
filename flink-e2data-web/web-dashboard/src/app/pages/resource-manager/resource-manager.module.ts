import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ShareModule } from 'share/share.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { NgxEchartsModule } from 'ngx-echarts';

import { ResourceManagerRoutingModule } from './resource-manager-routing.module';
import { ResourceManagerComponent } from './resource-manager.component';
import { ResourceManagerRealtimeComponent } from './real-time/resource-manager-real-time.component';
import { NodeDetailsComponent } from './real-time/node-details/node-details.component';

@NgModule({
  imports: [
    CommonModule,
    ShareModule,
    FormsModule,
    ReactiveFormsModule,
    ResourceManagerRoutingModule,
    NgxEchartsModule
  ],
  declarations: [ResourceManagerComponent, ResourceManagerRealtimeComponent, NodeDetailsComponent]
})
export class ResourceManagerModule {}
