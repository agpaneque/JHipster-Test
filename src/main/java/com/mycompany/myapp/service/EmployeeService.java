package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.EmployeeDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Employee}.
 */
public interface EmployeeService {
    /**
     * Save a employee.
     *
     * @param employeeDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<EmployeeDTO> save(EmployeeDTO employeeDTO);

    /**
     * Partially updates a employee.
     *
     * @param employeeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<EmployeeDTO> partialUpdate(EmployeeDTO employeeDTO);

    /**
     * Get all the employees.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<EmployeeDTO> findAll(Pageable pageable);

    /**
     * Returns the number of employees available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" employee.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<EmployeeDTO> findOne(Long id);

    /**
     * Delete the "id" employee.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);
}
