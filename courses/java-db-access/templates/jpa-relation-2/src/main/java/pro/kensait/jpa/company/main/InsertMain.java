package pro.kensait.jpa.company.main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Address;
import pro.kensait.jpa.company.entity.Department;
import pro.kensait.jpa.company.entity.Email;
import pro.kensait.jpa.company.entity.Employee;
import pro.kensait.jpa.company.entity.Job;
import pro.kensait.jpa.company.entity.Phone;
import pro.kensait.jpa.company.entity.Project;
import pro.kensait.jpa.company.entity.Qualification;

// 挿入
public class InsertMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                // 新しい住所を生成する
                Address address = new Address(101, "152-0000", "東京都", "目黒区");

                // 役職を取得する
                Job job = entityManager.find(Job.class, 2);

                // プロジェクトを取得する
                Project project = entityManager.find(Project.class, 3);
                List<Project> projects = new ArrayList<>();
                projects.add(project);

                // 新しい社員を生成する
                Integer employeeId = 10021;
                Employee employee = new Employee(employeeId, "Steve", address,
                        LocalDate.of(2017, 10, 1), job, 380000, projects);

                // 新しいメールを生成する
                Email email = new Email(201, employeeId, "steve@gmail.com");
                List<Email> emails = new ArrayList<Email>();
                emails.add(email);
                employee.setEmails(emails);

                // 新しい電話番号を生成する
                Phone phone = new Phone(employee, "03-XXXX-XXXX", null);
                employee.setPhone(phone);

                // 新しい資格を生成する
                List<Employee> employees = new ArrayList<Employee>();
                Qualification qualification = new Qualification(41, "LPIC-1", "LPI", employees);
                List<Qualification> qualifications = new ArrayList<Qualification>();
                qualifications.add(qualification);
                employee.setQualifications(qualifications);

                // 部署を取得する
                Department department = entityManager.find(Department.class, 2);
                employee.setDepartment(department);

                // すべてのエンティティを保存する
                entityManager.persist(address);
                entityManager.persist(employee);
                entityManager.persist(email);
                entityManager.persist(phone);
                entityManager.persist(qualification);

                // EntityTransactionをコミットする
                entityTransaction.commit();

                // entityManagerをクローズする
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
