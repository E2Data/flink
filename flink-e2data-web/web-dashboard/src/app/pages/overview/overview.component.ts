/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { StatusService, E2DataService } from 'services';

@Component({
  selector: 'e2data-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OverviewComponent implements OnInit, OnDestroy {
  destroy$ = new Subject();
  jobManagerStatus: string = 'offline';

  constructor(
    private cdr: ChangeDetectorRef,
    private statusService: StatusService,
    private e2dataService: E2DataService
  ) {}

  startFlink() {
    console.log('starting Flink...');
    this.e2dataService.startFlink();
  }

  stopFlink() {
    console.log('stopping Flink...');
    this.e2dataService.stopFlink();
  }

  ngOnInit() {
    this.e2dataService.jobManagerStatus$.pipe(takeUntil(this.destroy$)).subscribe(online => {
      this.jobManagerStatus = online ? 'online' : 'offline';
      this.cdr.markForCheck();
      console.log(online);
      console.log(this.jobManagerStatus);
    });

    this.statusService.refresh$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.e2dataService.checkJobManagerStatus();
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
