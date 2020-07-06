import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { HaierParamsInterface, HaierJobPlanArrayInterface, HaierLogsInterface } from 'interfaces';

const BASE_URL = 'https://virtserver.swaggerhub.com/c-triantaf/e-2_data_haier_api/1.0-SNAPSHOT';
const TOKEN = 'JKmpuzHau8jVGGABWF6ITbLh6Xqi';
const headers = new HttpHeaders({
  'Content-Type': 'application/json',
  Authorization: `Bearer ${TOKEN}`
});

@Injectable({
  providedIn: 'root'
})
export class HaierService {
  constructor(private httpClient: HttpClient) {}

  /**
   * Get the params
   */
  loadParams() {
    return this.httpClient.get<HaierParamsInterface>(`${BASE_URL}/nsga2/params/`, {
      headers: headers
    });
  }

  updateParams(maxParetoPlans: number, numGenerations: number) {
    let params = new HttpParams();
    if (maxParetoPlans) {
      params = params.append('max_pareto_plans', maxParetoPlans.toString());
    }
    if (numGenerations) {
      params = params.append('num_generations', numGenerations.toString());
    }

    return this.httpClient.put(`${BASE_URL}/nsga2/params/`, params, {
      headers: headers,
      observe: 'response'
    });
  }

  loadJobPlan(jobId: string) {
    return this.httpClient.get<HaierJobPlanArrayInterface>(`${BASE_URL}/nsga2/${jobId}/plans/`, { headers: headers });
  }

  updateJobPlan(jobId: string) {
    const data = {};
    return this.httpClient.post(`${BASE_URL}/nsga2/${jobId}/plans/`, data, {
      headers: headers,
      observe: 'response'
    });
  }

  loadLogs() {
    const noCacheHeaders = headers.append('Cache-Control', 'no-cache');
    return this.httpClient.get<HaierLogsInterface>(`${BASE_URL}/logs/`, {
      headers: noCacheHeaders
    });
  }
}
