import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { EmployeeFilterComponent } from './employee-filter.component';

describe('Component Tests', () => {
  describe('Employee Management Detail Component', () => {
    let comp: EmployeeFilterComponent;
    let fixture: ComponentFixture<EmployeeFilterComponent>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        declarations: [EmployeeFilterComponent],
        providers: [
          {
            provide: ActivatedRoute,
            useValue: { data: of({ employee: { id: 123 } }) },
          },
        ],
      })
        .overrideTemplate(EmployeeFilterComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(EmployeeFilterComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load employee on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.employee).toEqual(expect.objectContaining({ id: 123 }));
      });
    });
  });
});
