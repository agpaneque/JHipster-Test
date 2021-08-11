package com.mycompany.myapp.repository.rowmapper;

import com.mycompany.myapp.domain.Job;
import com.mycompany.myapp.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Job}, with proper type conversions.
 */
@Service
public class JobRowMapper implements BiFunction<Row, String, Job> {

    private final ColumnConverter converter;

    public JobRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Job} stored in the database.
     */
    @Override
    public Job apply(Row row, String prefix) {
        Job entity = new Job();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setJobTitle(converter.fromRow(row, prefix + "_job_title", String.class));
        entity.setJobDescription(converter.fromRow(row, prefix + "_job_description", String.class));
        entity.setJobHours(converter.fromRow(row, prefix + "_job_hours", Long.class));
        entity.setEmployeeId(converter.fromRow(row, prefix + "_employee_id", Long.class));
        return entity;
    }
}
