import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ShareModule } from 'share/share.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { NgxEchartsModule } from 'ngx-echarts';

import { VisualisationManagerRoutingModule } from './visualisation-manager-routing.module';
import { VisualisationManagerComponent } from './visualisation-manager.component';
import { VisualisationManagerRealtimeComponent } from './real-time/visualisation-manager-real-time.component';
import { VisualisationManagerHistoricalComponent } from './historical/visualisation-manager-historical.component';

@NgModule({
  imports: [
    CommonModule,
    ShareModule,
    FormsModule,
    ReactiveFormsModule,
    VisualisationManagerRoutingModule,
    NgxEchartsModule
  ],
  declarations: [
    VisualisationManagerComponent,
    VisualisationManagerRealtimeComponent,
    VisualisationManagerHistoricalComponent
  ]
})
export class VisualisationManagerModule {}
