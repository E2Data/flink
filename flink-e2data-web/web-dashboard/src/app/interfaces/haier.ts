export interface HaierParamsInterface {
  max_pareto_plans: number;
  num_generations: number;
}

export interface HaierJobPlanInterface {
  plan_id: number;
  exec_time: number;
  power_cons: number;
}

export interface HaierJobPlanArrayInterface {
  plans: HaierJobPlanInterface[];
}

export interface HaierLogsInterface {
  log: string;
}
