package com.igot.cb.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

class BitPositionDaoTest {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private BitPositionDao dao;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getOrInsert_Success() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq("X"), eq("X")))
            .thenReturn(42L);

        Long id = dao.getOrInsert("X");
        assertEquals(42L, id);
    }

    @Test
    void getOrInsert_ThrowsDataAccessException() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any()))
            .thenThrow(new DataAccessResourceFailureException("fail"));

        assertThrows(DataAccessException.class, () -> dao.getOrInsert("X"));
    }

}
