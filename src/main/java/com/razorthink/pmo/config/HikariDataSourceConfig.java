package com.razorthink.pmo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;


@Configuration
public class HikariDataSourceConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        int poolSize = 20;
        String poolS = this.env.getProperty("spring.datasource.pool.size");
        if (poolS != null && !poolS.isEmpty()) {
            poolSize = Integer.parseInt(this.env.getProperty("spring.datasource.pool.size"));
        }

        config.setMaximumPoolSize(poolSize);
        config.setDataSourceClassName(this.env.getProperty("spring.datasource.driver-class-name"));
        config.addDataSourceProperty("url", this.env.getProperty("spring.datasource.url"));
        config.addDataSourceProperty("user", this.env.getProperty("spring.datasource.username"));
        config.addDataSourceProperty("password", this.env.getProperty("spring.datasource.password"));
        return new HikariDataSource(config);
    }

}
