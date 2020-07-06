export interface HaierParamsInterface {
  maxParetoPlans: number;
  numGenerations: number;
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
  logs: string;
}
