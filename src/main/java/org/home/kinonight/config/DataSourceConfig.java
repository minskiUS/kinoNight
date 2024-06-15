package org.home.kinonight.config;

import lombok.extern.slf4j.Slf4j;
import org.home.kinonight.dto.DatabaseCredentials;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource getDataSource(DatabaseCredentials databaseCredentials) {
        log.error("AAAAAA");
        log.error(databaseCredentials.toString());
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url("jdbc:postgresql://database-1.c9w26080o278.us-east-1.rds.amazonaws.com:5432/kinonight");
        dataSourceBuilder.username(databaseCredentials.getUsername());
        dataSourceBuilder.password(databaseCredentials.getPassword());
        return dataSourceBuilder.build();
    }
}