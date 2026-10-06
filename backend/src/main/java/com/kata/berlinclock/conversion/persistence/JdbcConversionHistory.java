package com.kata.berlinclock.conversion.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.kata.berlinclock.conversion.application.Conversion;
import com.kata.berlinclock.conversion.application.ConversionHistory;


@Repository
class JdbcConversionHistory implements ConversionHistory {

	private final JdbcClient jdbc;

	@Autowired
	JdbcConversionHistory(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Conversion save(LocalTime time, Instant convertedAt) {
		return jdbc.sql("""
				INSERT INTO conversion (time, converted_at)
				VALUES (:time, :convertedAt)
				RETURNING id, time, converted_at""")
				.param("time", time)
				.param("convertedAt", OffsetDateTime.ofInstant(convertedAt, ZoneOffset.UTC))
				.query(JdbcConversionHistory::toConversion)
				.single();
	}

	@Override
	public List<Conversion> latest(int limit) {
		return jdbc.sql("""
				SELECT id, time, converted_at
				FROM conversion
				ORDER BY converted_at DESC, id DESC
				LIMIT :limit""")
				.param("limit", limit)
				.query(JdbcConversionHistory::toConversion)
				.list();
	}

	@Override
	public Optional<Conversion> findById(long id) {
		return jdbc.sql("""
				SELECT id, time, converted_at
				FROM conversion
				WHERE id = :id""")
				.param("id", id)
				.query(JdbcConversionHistory::toConversion)
				.optional();
	}

	@Override
	public void deleteAll() {
		jdbc.sql("DELETE FROM conversion").update();
	}

	private static Conversion toConversion(ResultSet row, int rowNumber) throws SQLException {
		return new Conversion(
				row.getLong("id"),
				row.getObject("time", LocalTime.class),
				row.getObject("converted_at", OffsetDateTime.class).toInstant());
	}
}
