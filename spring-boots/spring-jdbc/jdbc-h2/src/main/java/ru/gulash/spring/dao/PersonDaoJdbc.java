package ru.gulash.spring.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gulash.spring.domain.Person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class PersonDaoJdbc implements PersonDao {
    private final JdbcOperations jdbc;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public int count() {
        Integer count = jdbc.queryForObject("select count(*) from persons", Integer.class);
        return count == null ? 0 : count;
    }

    @Override
    public void insert(Person person) {
        namedParameterJdbcTemplate.update("insert into persons (id, name) values (:id, :name)",
                Map.of("id", person.getId(), "name", person.getName()));
    }

    @Override
    public Person getById(long id) {
        //MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        Map<String,Object> params = Map.of("id", id);
        return namedParameterJdbcTemplate
                .queryForObject("select * from persons where id = :id", params, new PersonMapper());
    }

    @Override
    public List<Person> getAll() {
        return jdbc.query("select id, name from persons", new PersonMapper());
    }

    @Override
    public void deleteById(long id) {
        Map<String, Object> params = Collections.singletonMap("id", id);
        namedParameterJdbcTemplate.update(
                "delete from persons where id = :id", params
        );
    }
    
    private static class PersonMapper implements RowMapper<Person> {
        @Override
        public Person mapRow(ResultSet resultSet, int i) throws SQLException {
            long id = resultSet.getLong("id");
            String name = resultSet.getString("name");
            return new Person(id, name);
        }
    }

}
