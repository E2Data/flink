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

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { EMPTY, interval, merge, Subject } from 'rxjs';
import { debounceTime, mapTo, share, startWith, switchMap } from 'rxjs/operators';
import { YarnClusterInfoInterface, YarnNodesInfoInterface } from 'interfaces';

const refreshInterval: number = 10000;

const headers = new HttpHeaders({
  'Content-Type': 'application/json'
});

@Injectable({
  providedIn: 'root'
})
export class YarnService {
  /**
   * Refresh stream generated from the configuration
   */
  refresh$ = new Subject<boolean>().asObservable();
  /**
   * Force refresh stream trigger manually
   */
  private forceRefresh$ = new Subject<boolean>();

  /**
   * Trigger force refresh
   */
  forceRefresh() {
    this.forceRefresh$.next(true);
  }

  updateMetrics() {
    return this.httpClient.get<YarnClusterInfoInterface>('http://localhost:8088/ws/v1/cluster/metrics', {
      headers: headers
    });
  }

  updateNodeInformation() {
    return this.httpClient.get<YarnNodesInfoInterface>('http://localhost:8088/ws/v1/cluster/nodes', {
      headers: headers
    });
  }

  constructor(private httpClient: HttpClient) {
    const interval$ = interval(refreshInterval).pipe(
      mapTo(true),
      startWith(true)
    );
    this.refresh$ = merge(this.forceRefresh$).pipe(
      startWith(true),
      debounceTime(300),
      switchMap(active => (active ? interval$ : EMPTY)),
      share()
    );
  }
}
