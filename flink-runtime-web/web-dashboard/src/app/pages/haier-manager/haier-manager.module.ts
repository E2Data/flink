import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ShareModule } from 'share/share.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { HaierManagerRoutingModule } from './haier-manager-routing.module';
import { HaierManagerComponent } from './haier-manager.component';
import { HaierManagerConfigurationComponent } from './configuration/haier-manager-configuration.component';
import { HaierManagerLogsComponent } from './logs/haier-manager-logs.component';

@NgModule({
  imports: [CommonModule, ShareModule, FormsModule, ReactiveFormsModule, HaierManagerRoutingModule],
  declarations: [HaierManagerComponent, HaierManagerConfigurationComponent, HaierManagerLogsComponent]
})
export class HaierManagerModule {}
