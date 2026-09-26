package pro.kensait.leafbooks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * 出版社の機能を提供するクラス
 */
@Entity
@Table(name = "PUBLISHER")
public class Publisher {
    // 出版社ID
    @Id
    @Column(name = "PUBLISHER_ID")
    private int publisherId;

    // 出版社名
    @Column(name = "PUBLISHER_NAME")
    private String publisherName;

    //  引数なしのコンストラクタ
    public Publisher() {
    }

    // コンストラクタ
    public Publisher(int publisherId, String publisherName) {
        this.publisherId = publisherId;
        this.publisherName = publisherName;
    }

    // 出版社IDの取得
    public int getPublisherId() {
        return publisherId;
    }

    // 出版社IDの設定
    public void setPublisherId(int publisherId) {
        this.publisherId = publisherId;
    }

    // 出版社名称の取得
    public String getPublisherName() {
        return publisherName;
    }

    // 出版社名称の設定
    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Publisher [publisherId=" + publisherId + ", publisherName="
                + publisherName + "]";
    }
}
