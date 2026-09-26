package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;

/*
 * 検索（主キーから）
 */
public class SelectMain {

    public static void main(String[] args) {
        // EntityManagerFactoryを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // Employeeを取得する
                Employee employee = entityManager.find(Employee.class, 10001);

                // Employeeがプロパティとして保持するEmailリストを取得する
                List<String> emailList = employee.getEmailList();

                // Emailリストから個々のEmailを取得し、アドレスをコンソールに表示する
                // 遅延ロードなので、リストから値を取り出さないとSQLは発行されない
                for (String address : emailList) {
                    showSingleResult("address", address);
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
