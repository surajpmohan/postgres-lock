package com.spm.postgreslock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class PostgresLockApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(PostgresLockApplication.class, args);
	}

	@Autowired
	JdbcTemplate jdbcTemplate;

	enum Status {
		CREATED, PROCESSING, COMPLETED
	}

	@Override
	public void run(String... strings) throws Exception {

		List<Object[]> list = new ArrayList<>();
		for (int i = 0; i<10000; i++) {
			String[] entry = new String[2];
			entry[0] = "name" + i;
			entry[1] = Status.CREATED.toString();
            list.add(entry);
		}

		jdbcTemplate.batchUpdate("INSERT INTO event(name, status) VALUES (?,?)", list);

		/*jdbcTemplate.queryForList("select * from event").stream().forEach(e -> {
		    System.out.println(e);
        });*/
		Thread t[] = new Thread[200];
        for (int i = 0; i<100; i++) {
            Thread t1 = new Thread(() -> update1());
            Thread t2 = new Thread(() -> update2());
            Thread t3 = new Thread(() -> update3());
            t[i] = t2;
        }
        for (int i=0; i<100; i++){
            t[i].start();
        }
	}

	@Transactional
	private void update1() {
        jdbcTemplate.queryForList("update event set status = '"
                + PostgresLockApplication.Status.PROCESSING.toString()+"' where status<>'COMPLETED' returning *")
                .stream().forEach(e -> {});
    }
    @Transactional
    private void update2() {
        long i = jdbcTemplate.queryForList("update event set status = '"
                + PostgresLockApplication.Status.PROCESSING.toString()+"' where id in (select id from event " +
                "where status<>'COMPLETED' order by status for update skip locked) returning *")
                .stream().count();
        System.out.println("Count: " + i);
    }
    @Transactional
    private void update3() {
        long  i = jdbcTemplate.queryForList("update event e set status = '"
                + PostgresLockApplication.Status.PROCESSING.toString()+"' from (select * from event b " +
                "where b.status<>'COMPLETED' order by b.status for no key update skip locked) u where e.id=u.id returning *")
                .stream().count();
        System.out.println("Count: " + i);
    }
}
