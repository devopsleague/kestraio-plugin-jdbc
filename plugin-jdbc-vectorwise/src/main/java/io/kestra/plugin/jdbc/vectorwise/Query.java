package io.kestra.plugin.jdbc.vectorwise;

import com.ingres.jdbc.IngresDriver;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.plugin.jdbc.AbstractCellConverter;
import io.kestra.plugin.jdbc.AbstractJdbcQuery;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.ZoneId;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Query a Vectorwise database."
)
@Plugin(
    examples = {
        @Example(
            title = "Send a SQL query to a Vectorwise database and fetch a row as output.",
            full = true,
            code = """
                   id: vectorwise_query
                   namespace: company.team

                   tasks:
                     - id: query
                       type: io.kestra.plugin.jdbc.vectorwise.Query
                       url: jdbc:vectorwise://url:port/base
                       username: "{{ secret('VECTORWISE_USERNAME') }}"
                       password: "{{ secret('VECTORWISE_PASSWORD') }}"
                       sql: select * from vectorwise_types
                       fetchType: FETCH_ONE
                   """
        )
    }
)
public class Query extends AbstractJdbcQuery implements RunnableTask<AbstractJdbcQuery.Output>, VetorwiseConnectionInterface {
    @Override
    protected AbstractCellConverter getCellConverter(ZoneId zoneId) {
        return new VectorwiseCellConverter(zoneId);
    }

    @Override
    public void registerDriver() throws SQLException {
        // only register the driver if not already exist to avoid a memory leak
        if (DriverManager.drivers().noneMatch(IngresDriver.class::isInstance)) {
            DriverManager.registerDriver(new IngresDriver());
        }
    }

}
