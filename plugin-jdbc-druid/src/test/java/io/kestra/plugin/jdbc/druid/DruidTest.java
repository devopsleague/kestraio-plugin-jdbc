package io.kestra.plugin.jdbc.druid;

import com.google.common.collect.ImmutableMap;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.jdbc.AbstractJdbcQuery;
import io.kestra.core.junit.annotations.KestraTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.kestra.core.models.tasks.common.FetchType.FETCH_ONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
public class DruidTest {
    @Inject
    RunContextFactory runContextFactory;

    @BeforeAll
    public static void startServer() throws Exception {
        DruidTestHelper.initServer();
    }

    @Test
    void insertAndQuery() throws Exception {
        RunContext runContext = runContextFactory.of(ImmutableMap.of());

        Query task = Query.builder()
                .url(Property.of("jdbc:avatica:remote:url=http://localhost:8888/druid/v2/sql/avatica/;transparent_reconnection=true"))
                .fetchType(Property.of(FETCH_ONE))
                .timeZoneId(Property.of("Europe/Paris"))
                .sql(Property.of("""
                        select
                          -- NULL as t_null,
                          'string' AS t_string,
                          CAST(2147483647 AS INT) as t_integer,
                          CAST(12345.124 AS FLOAT) as t_float,
                          CAST(12345.124 AS DOUBLE) as t_double,
                          TIME_FORMAT(MILLIS_TO_TIMESTAMP(1639137263000), 'yyyy-MM-dd')  as t_date,
                          TIME_FORMAT(MILLIS_TO_TIMESTAMP(1639137263000), 'yyyy-MM-dd HH:mm:ss') AS t_timestamp
                        from products
                        limit 1"""))
                .build();

        AbstractJdbcQuery.Output runOutput = task.run(runContext);
        assertThat(runOutput.getRow(), notNullValue());

        assertThat(runOutput.getRow().get("t_null"), is(nullValue()));
        assertThat(runOutput.getRow().get("t_string"), is("string"));
        assertThat(runOutput.getRow().get("t_integer"), is(2147483647));
        assertThat(runOutput.getRow().get("t_float"), is(12345.124));
        assertThat(runOutput.getRow().get("t_double"), is(12345.124D));
        assertThat(runOutput.getRow().get("t_date"), is("2021-12-10"));
        assertThat(runOutput.getRow().get("t_timestamp"), is("2021-12-10 11:54:23"));
    }
}

