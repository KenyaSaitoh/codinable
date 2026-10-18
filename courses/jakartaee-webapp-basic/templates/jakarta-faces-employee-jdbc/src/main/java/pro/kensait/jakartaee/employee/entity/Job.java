package pro.kensait.jakartaee.employee.entity;

import java.io.Serializable;

/** 役職マスター（JOB テーブルの 1 行） */
public record Job(Integer jobId, String jobName, Integer grade) implements Serializable {
}
