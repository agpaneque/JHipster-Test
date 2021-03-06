import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IEmployee, getEmployeeIdentifier } from '../employee.model';

import { IDepartment } from '../../department/department.model';

export type EntityResponseType = HttpResponse<IEmployee>;
export type EntityArrayResponseType = HttpResponse<IEmployee[]>;

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/employees');
  protected resourceUrlFilter = this.applicationConfigService.getEndpointFor('api/employees/report');
  protected resourceUrlDepartment = this.applicationConfigService.getEndpointFor('/api/departments/');
  protected resourceUrlSearch = this.applicationConfigService.getEndpointFor('/api/employees/search/');
  protected resourceUrlemployeesWithoutD = this.applicationConfigService.getEndpointFor('/api/employees/withoutdepartments/');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(employee: IEmployee): Observable<EntityResponseType> {
    return this.http.post<IEmployee>(this.resourceUrl, employee, { observe: 'response' });
  }

  update(employee: IEmployee): Observable<EntityResponseType> {
    return this.http.put<IEmployee>(`${this.resourceUrl}/${getEmployeeIdentifier(employee) as number}`, employee, { observe: 'response' });
  }

  partialUpdate(employee: IEmployee): Observable<EntityResponseType> {
    return this.http.patch<IEmployee>(`${this.resourceUrl}/${getEmployeeIdentifier(employee) as number}`, employee, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IEmployee>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
  //Trabajando aqui
  findDepartmetById(id: number): Observable<EntityResponseType> {
    return this.http.get<IDepartment>(`${this.resourceUrlDepartment}/${id}`, { observe: 'response' });
  }

  findByDepartment(id: number): Observable<EntityArrayResponseType> {
    return this.http.get<IEmployee[]>(`${this.resourceUrlFilter}/${id}`, { observe: 'response' });
    // return this.http.get('http://localhost:8080/api/employees/report/1');
  }

  employeesWithoutDepartments(): Observable<EntityArrayResponseType> {
    return this.http.get<IEmployee[]>(`${this.resourceUrlemployeesWithoutD}/`, { observe: 'response' });
    // return this.http.get('http://localhost:8080/api/employees/report/1');
  }

  findByString(searchString: string): Observable<EntityArrayResponseType> {
    return this.http.get<IEmployee[]>(`${this.resourceUrlSearch}${searchString}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IEmployee[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  addEmployeeToCollectionIfMissing(employeeCollection: IEmployee[], ...employeesToCheck: (IEmployee | null | undefined)[]): IEmployee[] {
    const employees: IEmployee[] = employeesToCheck.filter(isPresent);
    if (employees.length > 0) {
      const employeeCollectionIdentifiers = employeeCollection.map(employeeItem => getEmployeeIdentifier(employeeItem)!);
      const employeesToAdd = employees.filter(employeeItem => {
        const employeeIdentifier = getEmployeeIdentifier(employeeItem);
        if (employeeIdentifier == null || employeeCollectionIdentifiers.includes(employeeIdentifier)) {
          return false;
        }
        employeeCollectionIdentifiers.push(employeeIdentifier);
        return true;
      });
      return [...employeesToAdd, ...employeeCollection];
    }
    return employeeCollection;
  }
}
