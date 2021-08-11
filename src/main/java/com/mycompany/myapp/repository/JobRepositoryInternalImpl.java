package com.mycompany.myapp.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import com.mycompany.myapp.domain.Job;
import com.mycompany.myapp.repository.rowmapper.EmployeeRowMapper;
import com.mycompany.myapp.repository.rowmapper.JobRowMapper;
import com.mycompany.myapp.service.EntityManager;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive custom repository implementation for the Job entity.
 */
@SuppressWarnings("unused")
class JobRepositoryInternalImpl implements JobRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final EmployeeRowMapper employeeMapper;
    private final JobRowMapper jobMapper;

    private static final Table entityTable = Table.aliased("job", EntityManager.ENTITY_ALIAS);
    private static final Table employeeTable = Table.aliased("employee", "employee");

    public JobRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        EmployeeRowMapper employeeMapper,
        JobRowMapper jobMapper
    ) {
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.employeeMapper = employeeMapper;
        this.jobMapper = jobMapper;
    }

    @Override
    public Flux<Job> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<Job> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<Job> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = JobSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(EmployeeSqlHelper.getColumns(employeeTable, "employee"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(employeeTable)
            .on(Column.create("employee_id", entityTable))
            .equals(Column.create("id", employeeTable));

        String select = entityManager.createSelect(selectFrom, Job.class, pageable, criteria);
        String alias = entityTable.getReferenceName().getReference();
        String selectWhere = Optional
            .ofNullable(criteria)
            .map(
                crit ->
                    new StringBuilder(select)
                        .append(" ")
                        .append("WHERE")
                        .append(" ")
                        .append(alias)
                        .append(".")
                        .append(crit.toString())
                        .toString()
            )
            .orElse(select); // TODO remove once https://github.com/spring-projects/spring-data-jdbc/issues/907 will be fixed
        return db.sql(selectWhere).map(this::process);
    }

    @Override
    public Flux<Job> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<Job> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    private Job process(Row row, RowMetadata metadata) {
        Job entity = jobMapper.apply(row, "e");
        entity.setEmployee(employeeMapper.apply(row, "employee"));
        return entity;
    }

    @Override
    public <S extends Job> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    @Override
    public <S extends Job> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity);
        } else {
            return update(entity)
                .map(
                    numberOfUpdates -> {
                        if (numberOfUpdates.intValue() <= 0) {
                            throw new IllegalStateException("Unable to update Job with id = " + entity.getId());
                        }
                        return entity;
                    }
                );
        }
    }

    @Override
    public Mono<Integer> update(Job entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }
}

class JobSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("job_title", table, columnPrefix + "_job_title"));
        columns.add(Column.aliased("job_description", table, columnPrefix + "_job_description"));
        columns.add(Column.aliased("job_hours", table, columnPrefix + "_job_hours"));

        columns.add(Column.aliased("employee_id", table, columnPrefix + "_employee_id"));
        return columns;
    }
}
