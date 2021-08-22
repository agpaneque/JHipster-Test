package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.DepartmentDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Department}.
 */
public interface DepartmentService {
    /**
     * Save a department.
     *
     * @param departmentDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<DepartmentDTO> save(DepartmentDTO departmentDTO);

    /**
     * Partially updates a department.
     *
     * @param departmentDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<DepartmentDTO> partialUpdate(DepartmentDTO departmentDTO);

    /**
     * Get all the departments.
     *
     *
     * @return the list of entities.
     */
    Flux<DepartmentDTO> findAllAll();

    /**
     * Get all the departments.
     *
     *
     * @return the list of entities.
     */
    Flux<DepartmentDTO> findAll(Pageable pageable);

    /**
     * Returns the number of departments available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" department.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<DepartmentDTO> findOne(Long id);

    /**
     * Delete the "id" department.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);
}
