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
import pro.kensait.jakartaee.employee.entity.Department;

/** DEPARTMENT テーブル（部署マスター）を JDBC で読む */
@ApplicationScoped
public class DepartmentRepository {
    private static final String SELECT_COLUMNS = "SELECT DEPARTMENT_ID, DEPARTMENT_NAME, LOCATION FROM DEPARTMENT";

    @Resource(lookup = "java:app/jdbc/EmployeeDS")
    private DataSource dataSource;

    public List<Department> findAll() {
        String sql = SELECT_COLUMNS + " ORDER BY DEPARTMENT_ID";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            List<Department> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(toDepartment(resultSet));
            }
            return list;
        } catch (SQLException exception) {
            throw new RepositoryException("部署の取得に失敗しました", exception);
        }
    }

    public Optional<Department> findById(Integer id) {
        String sql = SELECT_COLUMNS + " WHERE DEPARTMENT_ID = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(toDepartment(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RepositoryException("部署の取得に失敗しました", exception);
        }
    }

    private Department toDepartment(ResultSet resultSet) throws SQLException {
        return new Department(resultSet.getInt("DEPARTMENT_ID"), resultSet.getString("DEPARTMENT_NAME"), resultSet.getString("LOCATION"));
    }
}
