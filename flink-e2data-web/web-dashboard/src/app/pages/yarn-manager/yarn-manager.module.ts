import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ShareModule } from 'share/share.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { YarnManagerComponent } from './yarn-manager.component';

@NgModule({
  imports: [CommonModule, ShareModule, FormsModule, ReactiveFormsModule],
  declarations: [YarnManagerComponent],
  exports: [YarnManagerComponent]
})
export class YarnManagerModule {}
