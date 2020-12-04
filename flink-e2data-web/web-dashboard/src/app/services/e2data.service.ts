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

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ReplaySubject } from 'rxjs';
import { first } from 'rxjs/operators';
import { BASE_URL } from 'config';

@Injectable({
  providedIn: 'root'
})
export class E2DataService {
  /**
   * Job Manager status event
   */
  jobManagerStatus$ = new ReplaySubject<boolean>();

  flinkWebAddress = '';

  constructor(private httpClient: HttpClient) {
    this.httpClient.get(`${BASE_URL}/config`).subscribe((data: any) => {
      console.log(data);
      this.flinkWebAddress = data['flink-web-address'];
      console.log(this.flinkWebAddress);
    });
  }

  /**
   * Pings JobManager and update broadcast if it's online or not
   */
  checkJobManagerStatus() {
    this.httpClient
      .get(`${this.flinkWebAddress}/jobs/overview`, { observe: 'response' })
      .pipe(first())
      .subscribe(
        resp => {
          console.log(resp);
          if (resp.status === 200) {
            this.jobManagerStatus$.next(true);
          } else {
            this.jobManagerStatus$.next(false);
          }
        },
        () => this.jobManagerStatus$.next(false)
      );
  }

  /**
   * Starts Flink
   */
  startFlink() {
    console.log(BASE_URL);
    //     this.httpClient.get(`${BASE_URL}/e2data/start/`)
    this.httpClient.get('/e2data/start/', { observe: 'response' }).subscribe(
      resp => {
        console.log(resp);
      },
      error => console.log(error)
    );
  }

  /**
   * Stops Flink
   */
  stopFlink() {
    //     this.httpClient.get(`${BASE_URL}/e2data/stop/`)
    this.httpClient.get('/e2data/stop-all/', { observe: 'response' }).subscribe(
      resp => {
        console.log(resp);
      },
      error => console.log(error)
    );
  }
}
