package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.EmployeeRepository;
import com.mycompany.myapp.service.EmployeeService;
import com.mycompany.myapp.service.dto.DepartmentDTO;
import com.mycompany.myapp.service.dto.EmployeeDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import io.micrometer.core.ipc.http.HttpSender.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Employee}.
 */
@RestController
@RequestMapping("/api")
public class EmployeeResource {

    private final Logger log = LoggerFactory.getLogger(EmployeeResource.class);

    private static final String ENTITY_NAME = "employee";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EmployeeService employeeService;

    private final EmployeeRepository employeeRepository;

    public EmployeeResource(EmployeeService employeeService, EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
    }

    /**
     * {@code POST  /employees} : Create a new employee.
     *
     * @param employeeDTO the employeeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new employeeDTO, or with status {@code 400 (Bad Request)} if
     *         the employee has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/employees")
    public Mono<ResponseEntity<EmployeeDTO>> createEmployee(@RequestBody EmployeeDTO employeeDTO) throws URISyntaxException {
        log.debug("REST request to save Employee : {}", employeeDTO);
        if (employeeDTO.getId() != null) {
            throw new BadRequestAlertException("A new employee cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return employeeService
            .save(employeeDTO)
            .map(
                result -> {
                    try {
                        return ResponseEntity
                            .created(new URI("/api/employees/" + result.getId()))
                            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
    }

    /**
     * {@code PUT  /employees/:id} : Updates an existing employee.
     *
     * @param id          the id of the employeeDTO to save.
     * @param employeeDTO the employeeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated employeeDTO, or with status {@code 400 (Bad Request)} if
     *         the employeeDTO is not valid, or with status
     *         {@code 500 (Internal Server Error)} if the employeeDTO couldn't be
     *         updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/employees/{id}")
    public Mono<ResponseEntity<EmployeeDTO>> updateEmployee(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody EmployeeDTO employeeDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Employee : {}, {}", id, employeeDTO);
        if (employeeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, employeeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return employeeRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    return employeeService
                        .save(employeeDTO)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                        .map(
                            result ->
                                ResponseEntity
                                    .ok()
                                    .headers(
                                        HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString())
                                    )
                                    .body(result)
                        );
                }
            );
    }

    /**
     * {@code PATCH  /employees/:id} : Partial updates given fields of an existing
     * employee, field will ignore if it is null
     *
     * @param id          the id of the employeeDTO to save.
     * @param employeeDTO the employeeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated employeeDTO, or with status {@code 400 (Bad Request)} if
     *         the employeeDTO is not valid, or with status {@code 404 (Not Found)}
     *         if the employeeDTO is not found, or with status
     *         {@code 500 (Internal Server Error)} if the employeeDTO couldn't be
     *         updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/employees/{id}", consumes = "application/merge-patch+json")
    public Mono<ResponseEntity<EmployeeDTO>> partialUpdateEmployee(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody EmployeeDTO employeeDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Employee partially : {}, {}", id, employeeDTO);
        if (employeeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, employeeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return employeeRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    Mono<EmployeeDTO> result = employeeService.partialUpdate(employeeDTO);

                    return result
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                        .map(
                            res ->
                                ResponseEntity
                                    .ok()
                                    .headers(
                                        HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, res.getId().toString())
                                    )
                                    .body(res)
                        );
                }
            );
    }

    /**
     * {@code GET  /employees} : get all the employees.
     *
     * @param pageable the pagination information.
     * @param request  a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of employees in body.
     */
    @GetMapping("/employees")
    public Mono<ResponseEntity<List<EmployeeDTO>>> getAllEmployees(Pageable pageable, ServerHttpRequest request) {
        log.debug("REST request to get a page of Employees");
        return employeeService
            .countAll()
            .zipWith(employeeService.findAll(pageable).collectList())
            .map(
                countWithEntities -> {
                    return ResponseEntity
                        .ok()
                        .headers(
                            PaginationUtil.generatePaginationHttpHeaders(
                                UriComponentsBuilder.fromHttpRequest(request),
                                new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                            )
                        )
                        .body(countWithEntities.getT2());
                }
            );
    }

    /**
     * {@code GET  /employees/:id} : get the "id" employee.
     *
     * @param id the id of the employeeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the employeeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/employees/{id}")
    public Mono<ResponseEntity<EmployeeDTO>> getEmployee(@PathVariable Long id) {
        log.debug("REST request to get Employee : {}", id);
        Mono<EmployeeDTO> employeeDTO = employeeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(employeeDTO);
    }

    /**
     * {@code DELETE  /employees/:id} : delete the "id" employee.
     *
     * @param id the id of the employeeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/employees/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteEmployee(@PathVariable Long id) {
        log.debug("REST request to delete Employee : {}", id);
        return employeeService
            .delete(id)
            .map(
                result ->
                    ResponseEntity
                        .noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                        .build()
            );
    }

    /**
     * {@code GET  /employees/report} : get all the employees.
     *
     *
     * @param id of the Departament.
     * @return status {@code 200 (OK)} and the list of employees in body.
     */
    @GetMapping("/employees/report/{id}")
    public Mono<ResponseEntity<List<EmployeeDTO>>> getAllEmployeesByDepartment(@PathVariable Long id) {
        log.debug("REST request to get the Employees of a Department");

        return employeeService
            .findAllByDepartment(id)
            .collectList()
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map(
                response ->
                    ResponseEntity.ok().headers(HeaderUtil.createAlert(applicationName, ENTITY_NAME, applicationName)).body(response)
            );
    }

    /**
     * {@code GET  /employees/} : .
     *
     *
     * @param Salary.
     * @return status {@code 200 (OK)} and the list of employees in body.
     */
    @GetMapping("/employees/salarygreaterthan/{salary}")
    public Mono<ResponseEntity<List<EmployeeDTO>>> getEmployeesSalaryGreaterThan(@PathVariable Long salary) {
        log.debug("REST request to get the Employees of a Department");

        return employeeService
            .salaryGreaterThan(salary)
            .collectList()
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map(
                response ->
                    ResponseEntity.ok().headers(HeaderUtil.createAlert(applicationName, ENTITY_NAME, applicationName)).body(response)
            );
    }

    /**
     * {@code GET /employees/search/{search}} : get the "searchString".
     *
     * @param search for employees that match with search
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the list of employeeDTO that match the search, or with status
     *         {@code 404 (Not Found)}.
     */
    @GetMapping("/employees/search/{search}")
    public Mono<ResponseEntity<List<EmployeeDTO>>> searchEmployeesByString(@PathVariable String search) {
        log.debug("REST request to search Employee : {}", search);

        return employeeService
            .searchString(search)
            .collectList()
            .doOnNext(
                element -> {
                    //Solo de prueba
                    log.debug(element.toString());
                }
            )
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map(response -> ResponseEntity.ok().headers(HeaderUtil.createAlert(applicationName, ENTITY_NAME, ENTITY_NAME)).body(response));
    }

    /**
     * {@code GET /employees/withoutDepartments/} : .
     *
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the list of employeeDTO that match the search, or with status
     *         {@code 404 (Not Found)}.
     */
    @GetMapping("/employees/withoutdepartments/")
    public Mono<ResponseEntity<List<EmployeeDTO>>> employeesWithoutDepartments() {
        log.debug("REST request to search Employee without Department.");

        return employeeService
            .findAllWhereDepartmentIsNull()
            .collectList()
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map(response -> ResponseEntity.ok().headers(HeaderUtil.createAlert(applicationName, ENTITY_NAME, ENTITY_NAME)).body(response));
    }

    /**
     * {@code GET /employees/test/{search}} : get the "searchString".
     *
     * @param search for employees that match with search
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the list of employeeDTO that match the search, or with status
     *         {@code 404 (Not Found)}.
     */
    @GetMapping("/employees/test/{search}") // EndPoint para Pruebas
    public Mono<ResponseEntity<List<EmployeeDTO>>> Test(@PathVariable String search) {
        /*
         * String[] newStr = search.split("\\s+"); for (int i = 0; i < newStr.length;
         * i++) { log.debug(newStr[i]); }
         */

        Flux<EmployeeDTO> Temp = employeeService
            .findAllByDepartment(1L)
            .doOnNext(
                emp -> {
                    emp.setLastName("Valek");
                    DepartmentDTO temp = new DepartmentDTO();
                    temp.setDepartmentName("TI");
                    temp.setId(1L);

                    emp.setDepartment(temp);
                }
            )
            .filter(emp -> !emp.getFirstName().equals("Elena"));

        Temp.subscribe(
            e -> log.info(e.toString()),
            error -> log.error(error.getMessage()),
            new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    log.info("Finalizo con Exito");
                }
            }
        );

        // return Mono.just(ResponseEntity.ok(search));

        return Temp
            .collectList()
            .map(
                response ->
                    ResponseEntity.ok().headers(HeaderUtil.createAlert(applicationName, ENTITY_NAME, applicationName)).body(response)
            );
    }
}
