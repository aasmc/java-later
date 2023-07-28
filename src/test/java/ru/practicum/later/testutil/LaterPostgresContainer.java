package ru.practicum.later.testutil;

import org.testcontainers.containers.PostgreSQLContainer;

public class LaterPostgresContainer extends PostgreSQLContainer<LaterPostgresContainer> {

    private static final String IMAGE_VERSION = "postgres:15.3";

    private static LaterPostgresContainer container;

    private LaterPostgresContainer() {
        super(IMAGE_VERSION);
    }

    public static LaterPostgresContainer getInstance() {
        if (container == null) {
            container = new LaterPostgresContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        // do nothing, JVM handles shut down
    }
}
