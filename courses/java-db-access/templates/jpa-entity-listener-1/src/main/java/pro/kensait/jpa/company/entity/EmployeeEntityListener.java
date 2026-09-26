package pro.kensait.jpa.company.entity;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

/*
 * 社員エンティティの機能を提供するクラス
 */
public class EmployeeEntityListener {

    // コールバックメソッド（SELECT文の発行後に呼び出される）
    @PostLoad
    public void postLoad(Employee employee) {
        System.out.println("[ EmployeeEntityListener#postLoad ]");
    }

    // コールバックメソッド（INSERT文の発行前に呼び出される）
    @PrePersist
    public void prePersist(Employee employee) {
        System.out.println("[ EmployeeEntityListener#prePersist ]");
    }

    // コールバックメソッド（INSERT文の発行後に呼び出される）
    @PostPersist
    public void postPersist(Employee employee) {
        System.out.println("[ EmployeeEntityListener#postPersist ]");
    }

    // コールバックメソッド（DELETE文の発行前に呼び出される）
    @PreRemove
    public void preRemove(Employee employee) {
        System.out.println("[ EmployeeEntityListener#preRemove ]");
    }

    // コールバックメソッド（DELETE文の発行後に呼び出される）
    @PostRemove
    public void postRemove(Employee employee) {
        System.out.println("[ EmployeeEntityListener#postRemove ]");
    }

    // コールバックメソッド（UPDATE文の発行前に呼び出される）
    @PreUpdate
    public void preUpdate(Employee employee) {
        System.out.println("[ EmployeeEntityListener#preUpdate ]");
    }

    // コールバックメソッド（UPDATE文の発行後に呼び出される）
    @PostUpdate
    public void postUpdate(Employee employee) {
        System.out.println("[ EmployeeEntityListener#postUpdate ]");
    }
}
