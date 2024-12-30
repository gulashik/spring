package db.migration;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

// todo Java миграция посложнее; наследуемся от JavaMigration и сами реализовываем что нужно вернуть
public class AddUsersToCoursesReferenceTable implements JavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        DataSource ds = new SingleConnectionDataSource(context.getConnection(), true);
        JdbcOperations jdbc = new JdbcTemplate(ds);

        jdbc.execute("""
                CREATE TABLE users_courses (
                    user_id bigint REFERENCES users(id),
                    course_id bigint REFERENCES courses(id),
                    PRIMARY KEY(user_id, course_id)
                )
        """);
    }

    @Override
    public MigrationVersion getVersion() {
        return MigrationVersion.fromVersion("3_2");
    }

    @Override
    public String getDescription() {
        return "2020 05 26 Add users_to_courses reference table";
    }

    @Override
    public Integer getChecksum() {
        return null;
    }

    @Override
    public boolean canExecuteInTransaction() {
        return true;
    }
}
