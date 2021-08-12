import { IEmployee } from 'app/entities/employee/employee.model';

export interface IDepartment {
  id?: number;
  departmentName?: string;
  employees?: IEmployee[] | null;
}

export class Department implements IDepartment {
  constructor(public id?: number, public departmentName?: string, public employees?: IEmployee[] | null) {}
}

export function getDepartmentIdentifier(department: IDepartment): number | undefined {
  return department.id;
}
