package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Employee;
import com.mycompany.myapp.repository.EmployeeRepository;
import com.mycompany.myapp.service.EmployeeService;
import com.mycompany.myapp.service.dto.EmployeeDTO;
import com.mycompany.myapp.service.mapper.EmployeeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Employee}.
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;

    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public Mono<EmployeeDTO> save(EmployeeDTO employeeDTO) {
        log.debug("Request to save Employee : {}", employeeDTO);
        return employeeRepository.save(employeeMapper.toEntity(employeeDTO)).map(employeeMapper::toDto);
    }

    @Override
    public Mono<EmployeeDTO> partialUpdate(EmployeeDTO employeeDTO) {
        log.debug("Request to partially update Employee : {}", employeeDTO);

        return employeeRepository
            .findById(employeeDTO.getId())
            .map(
                existingEmployee -> {
                    employeeMapper.partialUpdate(existingEmployee, employeeDTO);

                    return existingEmployee;
                }
            )
            .flatMap(employeeRepository::save)
            .map(employeeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<EmployeeDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Employees");
        return employeeRepository.findAllBy(pageable).map(employeeMapper::toDto);
    }

    public Mono<Long> countAll() {
        return employeeRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<EmployeeDTO> findOne(Long id) {
        log.debug("Request to get Employee : {}", id);
        return employeeRepository.findById(id).map(employeeMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Employee : {}", id);
        return employeeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<EmployeeDTO> findAllByDepartment(Long Departmentid) {
        log.debug("Request to get all Employees");
        return employeeRepository.findByDepartment(Departmentid).map(employeeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<EmployeeDTO> searchString(String search) {
        log.debug("Request to search by String : {}", search);
        return employeeRepository.searchString(search).map(employeeMapper::toDto);
    }
}
