package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Job;
import com.mycompany.myapp.repository.JobRepository;
import com.mycompany.myapp.service.JobService;
import com.mycompany.myapp.service.dto.JobDTO;
import com.mycompany.myapp.service.mapper.JobMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Job}.
 */
@Service
@Transactional
public class JobServiceImpl implements JobService {

    private final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    private final JobRepository jobRepository;

    private final JobMapper jobMapper;

    public JobServiceImpl(JobRepository jobRepository, JobMapper jobMapper) {
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
    }

    @Override
    public Mono<JobDTO> save(JobDTO jobDTO) {
        log.debug("Request to save Job : {}", jobDTO);
        return jobRepository.save(jobMapper.toEntity(jobDTO)).map(jobMapper::toDto);
    }

    @Override
    public Mono<JobDTO> partialUpdate(JobDTO jobDTO) {
        log.debug("Request to partially update Job : {}", jobDTO);

        return jobRepository
            .findById(jobDTO.getId())
            .map(
                existingJob -> {
                    jobMapper.partialUpdate(existingJob, jobDTO);

                    return existingJob;
                }
            )
            .flatMap(jobRepository::save)
            .map(jobMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<JobDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Jobs");
        return jobRepository.findAllBy(pageable).map(jobMapper::toDto);
    }

    public Mono<Long> countAll() {
        return jobRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<JobDTO> findOne(Long id) {
        log.debug("Request to get Job : {}", id);
        return jobRepository.findById(id).map(jobMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Job : {}", id);
        return jobRepository.deleteById(id);
    }
}
