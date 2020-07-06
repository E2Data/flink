import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ShareModule } from 'share/share.module';

import { HaierConfigurationRoutingModule } from './haier-configuration-routing.module';
import { HaierConfigurationComponent } from './haier-configuration.component';

@NgModule({
  imports: [CommonModule, FormsModule, ReactiveFormsModule, HaierConfigurationRoutingModule, ShareModule],
  declarations: [HaierConfigurationComponent]
})
export class HaierConfigurationModule {}
