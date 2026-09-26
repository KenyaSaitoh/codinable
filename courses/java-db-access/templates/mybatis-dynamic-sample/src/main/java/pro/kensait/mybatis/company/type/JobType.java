package pro.kensait.mybatis.company.type;

/*
 * 役職型を表す列挙型
 */
public enum JobType {
    MANAGER("マネージャ"),
    LEADER("リーダー"),
    CHIEF("チーフ"),
    ASSOCIATE("アソシエイト");

    private final String jobType;

    // 役職型の初期化
    JobType(String jobType) {
        this.jobType = jobType;
    }

    // 文字列表現の生成
    public String toString() {
        return jobType;
    }
}
