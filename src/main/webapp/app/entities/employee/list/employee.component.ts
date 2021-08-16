import { Component, OnInit } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { IEmployee } from '../employee.model';

import { ASC, DESC, ITEMS_PER_PAGE } from 'app/config/pagination.constants';
import { EmployeeService } from '../service/employee.service';
import { EmployeeDeleteDialogComponent } from '../delete/employee-delete-dialog.component';
import { ParseLinks } from 'app/core/util/parse-links.service';
import { DepartmentService } from '../../department/service/department.service';
import { IDepartment } from '../../department/department.model';

@Component({
  selector: 'jhi-employee',
  templateUrl: './employee.component.html',
})
export class EmployeeComponent implements OnInit {
  employees: IEmployee[];

  isLoading = false;
  itemsPerPage: number;
  links: { [key: string]: number };
  page: number;
  predicate: string;
  ascending: boolean;
  searchString: string;
  empty: boolean;

  departmentSelectet: any;

  departmentsCollection: IDepartment[] = [];

  constructor(
    protected employeeService: EmployeeService,
    protected modalService: NgbModal,
    protected parseLinks: ParseLinks,
    protected departmentService: DepartmentService
  ) {
    this.employees = [];
    this.searchString = '';
    this.empty = true;

    this.itemsPerPage = ITEMS_PER_PAGE;
    this.page = 0;
    this.links = {
      last: 0,
    };
    this.predicate = 'id';
    this.ascending = true;
  }
  //Para cargar los Nombres de los departamentos
  LoadDepartment(): void {
    this.departmentService.findAll().subscribe(
      (res: HttpResponse<IDepartment[]>) => {
        if (res.body) {
          for (const d of res.body) {
            this.departmentsCollection.push(d);
          }
        }
      },
      error => {
        //eslint-disable-next-line no-console
        console.log(error);
      }
    );
  }

  loadDepartmetById(id: number): any {
    this.employeeService.findDepartmetById(id).subscribe((res: HttpResponse<IDepartment>) => {
      if (res.body) {
        return res.body.departmentName;
      } else {
        return '';
      }
    });
  }

  loadAll(): void {
    this.isLoading = true;

    this.employeeService
      .query({
        page: this.page,
        size: this.itemsPerPage,
        sort: this.sort(),
      })
      .subscribe(
        (res: HttpResponse<IEmployee[]>) => {
          this.isLoading = false;
          this.paginateEmployees(res.body, res.headers);
        },
        () => {
          this.isLoading = false;
        }
      );
  }
  //Carga en la lista los empleados de un departamento
  loadByDepartment(departmentid: any): void {
    if (departmentid === 'All') {
      this.reset();
    } else {
      this.page = 0;
      this.employees = [];

      this.employeeService.findByDepartment(departmentid).subscribe(
        (res: HttpResponse<IEmployee[]>) => {
          if (res.body) {
            for (const d of res.body) {
              this.employees.push(d);
            }
          }
        },
        () => {
          //eslint-disable-next-line no-console
          console.log('Error en la consulta');
        }
      );
    }
  }

  goSearch(): void {
    this.reset();
  }

  reset(): void {
    this.page = 0;
    this.employees = [];
    this.loadAll();
  }

  loadPage(page: number): void {
    this.page = page;
    this.loadAll();
  }

  ngOnInit(): void {
    this.LoadDepartment();
    this.loadAll();
  }

  trackId(index: number, item: IEmployee): number {
    return item.id!;
  }

  delete(employee: IEmployee): void {
    const modalRef = this.modalService.open(EmployeeDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.employee = employee;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed.subscribe(reason => {
      if (reason === 'deleted') {
        this.reset();
      }
    });
  }

  protected sort(): string[] {
    const result = [this.predicate + ',' + (this.ascending ? ASC : DESC)];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  protected paginateEmployees(data: IEmployee[] | null, headers: HttpHeaders): void {
    this.links = this.parseLinks.parse(headers.get('link') ?? '');
    if (data) {
      for (const d of data) {
        this.employees.push(d);
      }
    }
  }
}
