package pro.kensait.jakartaee.employee.entity;

import java.io.Serializable;

/** 部署マスター（DEPARTMENT テーブルの 1 行） */
public record Department(Integer departmentId, String departmentName, String location)
        implements Serializable {
}
