package pro.kensait.jakartaee.employee.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import pro.kensait.jakartaee.employee.entity.Job;

/** JOB テーブル（役職マスター）を JDBC で読む */
@ApplicationScoped
public class JobRepository {
    private static final String SELECT_COLUMNS = "SELECT JOB_ID, JOB_NAME, GRADE FROM JOB";

    @Resource(lookup = "java:app/jdbc/EmployeeDS")
    private DataSource dataSource;

    public List<Job> findAll() {
        String sql = SELECT_COLUMNS + " ORDER BY JOB_ID";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            List<Job> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(toJob(resultSet));
            }
            return list;
        } catch (SQLException exception) {
            throw new RepositoryException("役職の取得に失敗しました", exception);
        }
    }

    public Optional<Job> findById(Integer id) {
        String sql = SELECT_COLUMNS + " WHERE JOB_ID = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(toJob(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RepositoryException("役職の取得に失敗しました", exception);
        }
    }

    private Job toJob(ResultSet resultSet) throws SQLException {
        return new Job(resultSet.getInt("JOB_ID"), resultSet.getString("JOB_NAME"), resultSet.getInt("GRADE"));
    }
}
