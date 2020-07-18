import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { HaierService } from 'services';

const PARAMS_UPDATED_STATUS = 204;
const PARAMS_MALFORMED_INPUT = 400;
const GENERIC_FAILURE = 500;

@Component({
  selector: 'haier-manager-configuration',
  templateUrl: './haier-manager-configuration.component.html',
  styleUrls: ['./haier-manager-configuration.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HaierManagerConfigurationComponent implements OnInit, OnDestroy {
  destroy$ = new Subject();
  isLoadingParams = false;
  editId: string | null = null;
  ngsaParams = [{ name: 'maxParetoPlans', value: -1 }, { name: 'numGenerations', value: -1 }];

  startEdit(id: string): void {
    this.editId = id;
  }

  stopEdit(): void {
    this.editId = null;
    this.sendParams();
  }

  private getParams() {
    this.isLoadingParams = true;
    this.haierService.loadParams().subscribe(
      params => {
        console.log(params);
        this.updateParamValues(params.max_pareto_plans, params.num_generations);
      },
      () => {
        this.isLoadingParams = false;
        this.cdr.markForCheck();
      }
    );
  }

  private sendParams() {
    const maxParetoPlans = this.ngsaParams[0].value;
    const numGenerations = this.ngsaParams[1].value;

    this.haierService.updateParams(maxParetoPlans, numGenerations).subscribe(response => {
      switch (response.status) {
        case PARAMS_UPDATED_STATUS:
          console.log('Values updated');
          this.getParams();
          break;
        case PARAMS_MALFORMED_INPUT:
          break;
        case GENERIC_FAILURE:
          break;
      }
    });
  }

  private updateParamValues(maxParetoPlans: number, numGenerations: number) {
    this.ngsaParams = [
      { name: 'maxParetoPlans', value: maxParetoPlans },
      { name: 'numGenerations', value: numGenerations }
    ];

    console.log(this.ngsaParams);

    this.isLoadingParams = false;
    this.cdr.markForCheck();
  }

  private getJobPlan(jobId: string) {
    this.haierService.loadJobPlan(jobId).subscribe(data => {
      console.log(data);
    });
  }

  constructor(private haierService: HaierService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.getParams();
    this.getJobPlan('1');
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
