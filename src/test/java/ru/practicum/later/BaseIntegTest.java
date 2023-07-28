package ru.practicum.later;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.later.testutil.LaterPostgresContainer;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("integtest")
@Sql(
        scripts = "classpath:clear-db.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
public class BaseIntegTest {

    @Container
    static PostgreSQLContainer container = LaterPostgresContainer.getInstance();

    @Test
    void contextLoads() {
    }

}
