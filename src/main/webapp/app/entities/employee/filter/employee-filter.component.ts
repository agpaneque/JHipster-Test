import { Component, OnInit } from '@angular/core';

import { HttpResponse } from '@angular/common/http';

import { IEmployee } from '../employee.model';

import { EmployeeService } from '../service/employee.service';

import { ParseLinks } from 'app/core/util/parse-links.service';

import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'jhi-employee-filter',
  templateUrl: './employee-filter.component.html',
})
export class EmployeeFilterComponent implements OnInit {
  employees: IEmployee[];

  constructor(protected employeeService: EmployeeService, protected modalService: NgbModal, protected parseLinks: ParseLinks) {
    this.employees = [];
  }

  ngOnInit(): void {
    this.loadByDepartment();
  }

  loadByDepartment(): void {
    this.employeeService.findByDepartment(1).subscribe(
      (res: HttpResponse<IEmployee[]>) => {
        if (res.body) {
          for (const d of res.body) {
            this.employees.push(d);
          }
        }
        //eslint-disable-next-line no-console
        console.log(this.employees);
      },
      () => {
        //eslint-disable-next-line no-console
        console.log('Erron en la consulta');
      }
    );
  }

  previousState(): void {
    window.history.back();
  }
}
