import { IEmployee } from 'app/entities/employee/employee.model';

export interface IJob {
  id?: number;
  jobTitle?: string | null;
  jobDescription?: string | null;
  jobHours?: number | null;
  employee?: IEmployee | null;
}

export class Job implements IJob {
  constructor(
    public id?: number,
    public jobTitle?: string | null,
    public jobDescription?: string | null,
    public jobHours?: number | null,
    public employee?: IEmployee | null
  ) {}
}

export function getJobIdentifier(job: IJob): number | undefined {
  return job.id;
}
