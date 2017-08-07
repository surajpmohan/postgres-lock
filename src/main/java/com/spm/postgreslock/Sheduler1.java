package com.spm.postgreslock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

public class Sheduler1 {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Scheduled(fixedDelay = 1)
    public void run() {
        for (int i = 0; i<10; i++) {
            Thread t = new Thread(() -> jdbcTemplate.queryForList("update event set status1 = '"
                    + PostgresLockApplication.Status.PROCESSING.toString()+"' returning *")
                    .stream().forEach(e -> {}));
            t.start();
        }
    }
}
