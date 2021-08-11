package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Job;
import com.mycompany.myapp.repository.JobRepository;
import com.mycompany.myapp.service.EntityManager;
import com.mycompany.myapp.service.dto.JobDTO;
import com.mycompany.myapp.service.mapper.JobMapper;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link JobResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class JobResourceIT {

    private static final String DEFAULT_JOB_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_JOB_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_JOB_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_JOB_DESCRIPTION = "BBBBBBBBBB";

    private static final Long DEFAULT_JOB_HOURS = 1L;
    private static final Long UPDATED_JOB_HOURS = 2L;

    private static final String ENTITY_API_URL = "/api/jobs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Job job;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Job createEntity(EntityManager em) {
        Job job = new Job().jobTitle(DEFAULT_JOB_TITLE).jobDescription(DEFAULT_JOB_DESCRIPTION).jobHours(DEFAULT_JOB_HOURS);
        return job;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Job createUpdatedEntity(EntityManager em) {
        Job job = new Job().jobTitle(UPDATED_JOB_TITLE).jobDescription(UPDATED_JOB_DESCRIPTION).jobHours(UPDATED_JOB_HOURS);
        return job;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Job.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        job = createEntity(em);
    }

    @Test
    void createJob() throws Exception {
        int databaseSizeBeforeCreate = jobRepository.findAll().collectList().block().size();
        // Create the Job
        JobDTO jobDTO = jobMapper.toDto(job);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeCreate + 1);
        Job testJob = jobList.get(jobList.size() - 1);
        assertThat(testJob.getJobTitle()).isEqualTo(DEFAULT_JOB_TITLE);
        assertThat(testJob.getJobDescription()).isEqualTo(DEFAULT_JOB_DESCRIPTION);
        assertThat(testJob.getJobHours()).isEqualTo(DEFAULT_JOB_HOURS);
    }

    @Test
    void createJobWithExistingId() throws Exception {
        // Create the Job with an existing ID
        job.setId(1L);
        JobDTO jobDTO = jobMapper.toDto(job);

        int databaseSizeBeforeCreate = jobRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllJobs() {
        // Initialize the database
        jobRepository.save(job).block();

        // Get all the jobList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(job.getId().intValue()))
            .jsonPath("$.[*].jobTitle")
            .value(hasItem(DEFAULT_JOB_TITLE))
            .jsonPath("$.[*].jobDescription")
            .value(hasItem(DEFAULT_JOB_DESCRIPTION))
            .jsonPath("$.[*].jobHours")
            .value(hasItem(DEFAULT_JOB_HOURS.intValue()));
    }

    @Test
    void getJob() {
        // Initialize the database
        jobRepository.save(job).block();

        // Get the job
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, job.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(job.getId().intValue()))
            .jsonPath("$.jobTitle")
            .value(is(DEFAULT_JOB_TITLE))
            .jsonPath("$.jobDescription")
            .value(is(DEFAULT_JOB_DESCRIPTION))
            .jsonPath("$.jobHours")
            .value(is(DEFAULT_JOB_HOURS.intValue()));
    }

    @Test
    void getNonExistingJob() {
        // Get the job
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewJob() throws Exception {
        // Initialize the database
        jobRepository.save(job).block();

        int databaseSizeBeforeUpdate = jobRepository.findAll().collectList().block().size();

        // Update the job
        Job updatedJob = jobRepository.findById(job.getId()).block();
        updatedJob.jobTitle(UPDATED_JOB_TITLE).jobDescription(UPDATED_JOB_DESCRIPTION).jobHours(UPDATED_JOB_HOURS);
        JobDTO jobDTO = jobMapper.toDto(updatedJob);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, jobDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);
        Job testJob = jobList.get(jobList.size() - 1);
        assertThat(testJob.getJobTitle()).isEqualTo(UPDATED_JOB_TITLE);
        assertThat(testJob.getJobDescription()).isEqualTo(UPDATED_JOB_DESCRIPTION);
        assertThat(testJob.getJobHours()).isEqualTo(UPDATED_JOB_HOURS);
    }

    @Test
    void putNonExistingJob() throws Exception {
        int databaseSizeBeforeUpdate = jobRepository.findAll().collectList().block().size();
        job.setId(count.incrementAndGet());

        // Create the Job
        JobDTO jobDTO = jobMapper.toDto(job);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, jobDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchJob() throws Exception {
        int databaseSizeBeforeUpdate = jobRepository.findAll().collectList().block().size();
        job.setId(count.incrementAndGet());

        // Create the Job
        JobDTO jobDTO = jobMapper.toDto(job);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamJob() throws Exception {
        int databaseSizeBeforeUpdate = jobRepository.findAll().collectList().block().size();
        job.setId(count.incrementAndGet());

        // Create the Job
        JobDTO jobDTO = jobMapper.toDto(job);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateJobWithPatch() throws Exception {
        // Initialize the database
        jobRepository.save(job).block();

        int databaseSizeBeforeUpdate = jobRepository.findAll().collectList().block().size();

        // Update the job using partial update
        Job partialUpdatedJob = new Job();
        partialUpdatedJob.setId(job.getId());

        partialUpdatedJob.jobDescription(UPDATED_JOB_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedJob.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedJob))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);
        Job testJob = jobList.get(jobList.size() - 1);
        assertThat(testJob.getJobTitle()).isEqualTo(DEFAULT_JOB_TITLE);
        assertThat(testJob.getJobDescription()).isEqualTo(UPDATED_JOB_DESCRIPTION);
        assertThat(testJob.getJobHours()).isEqualTo(DEFAULT_JOB_HOURS);
    }

    @Test
    void fullUpdateJobWithPatch() throws Exception {
        // Initialize the database
        jobRepository.save(job).block();

        int databaseSizeBeforeUpdate = jobRepository.findAll().collectList().block().size();

        // Update the job using partial update
        Job partialUpdatedJob = new Job();
        partialUpdatedJob.setId(job.getId());

        partialUpdatedJob.jobTitle(UPDATED_JOB_TITLE).jobDescription(UPDATED_JOB_DESCRIPTION).jobHours(UPDATED_JOB_HOURS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedJob.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedJob))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);
        Job testJob = jobList.get(jobList.size() - 1);
        assertThat(testJob.getJobTitle()).isEqualTo(UPDATED_JOB_TITLE);
        assertThat(testJob.getJobDescription()).isEqualTo(UPDATED_JOB_DESCRIPTION);
        assertThat(testJob.getJobHours()).isEqualTo(UPDATED_JOB_HOURS);
    }

    @Test
    void patchNonExistingJob() throws Exception {
        int databaseSizeBeforeUpdate = jobRepository.findAll().collectList().block().size();
        job.setId(count.incrementAndGet());

        // Create the Job
        JobDTO jobDTO = jobMapper.toDto(job);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, jobDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchJob() throws Exception {
        int databaseSizeBeforeUpdate = jobRepository.findAll().collectList().block().size();
        job.setId(count.incrementAndGet());

        // Create the Job
        JobDTO jobDTO = jobMapper.toDto(job);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamJob() throws Exception {
        int databaseSizeBeforeUpdate = jobRepository.findAll().collectList().block().size();
        job.setId(count.incrementAndGet());

        // Create the Job
        JobDTO jobDTO = jobMapper.toDto(job);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteJob() {
        // Initialize the database
        jobRepository.save(job).block();

        int databaseSizeBeforeDelete = jobRepository.findAll().collectList().block().size();

        // Delete the job
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, job.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
