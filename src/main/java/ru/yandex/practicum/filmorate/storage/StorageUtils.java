package ru.yandex.practicum.filmorate.storage;

import lombok.experimental.UtilityClass;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Mpa;

@UtilityClass
public class StorageUtils {

    public static Mpa makeMpa(JdbcOperations jdbcTemplate, int id) {
        String query = "SELECT * FROM MPAS WHERE MPA_ID=?;";
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(query, id);
        if (mpaRows.next()) {
            return Mpa.builder()
                    .id(mpaRows.getInt("MPA_ID"))
                    .name(mpaRows.getString("NAME"))
                    .description(mpaRows.getString("DESCRIPTION"))
                    .build();
        }
        return null;
    }
}
