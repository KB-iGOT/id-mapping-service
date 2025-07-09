package com.igot.cb.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BitPositionDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL = """
                WITH ins AS (
                  INSERT INTO master_bitposition_lookup(name)
                  VALUES (?)
                  ON CONFLICT (name) DO NOTHING
                  RETURNING id
                )
                SELECT id FROM ins
                UNION
                SELECT id FROM master_bitposition_lookup WHERE name = ?
            """;

    public Long getOrInsert(String name) {
        return jdbcTemplate.queryForObject(
                SQL,
                Long.class,
                name, name);
    }
}