package pro.kensait.leafbooks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * カテゴリの機能を提供するクラス
 */
@Entity
@Table(name = "CATEGORY")
public class Category {
    // カテゴリID
    @Id
    @Column(name = "CATEGORY_ID")
    private Integer categoryId;

    // カテゴリ名
    @Column(name = "CATEGORY_NAME")
    private String categoryName;

    // 引数なしのコンストラクタ
    public Category() {
    }
    
    // コンストラクタ
    public Category(Integer categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // カテゴリIDの取得
    public Integer getCategoryId() {
        return categoryId;
    }

    // カテゴリIDの設定
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    // カテゴリ名称の取得
    public String getCategoryName() {
        return categoryName;
    }

    // カテゴリ名称の設定
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Category [categoryId=" + categoryId + ", categoryName=" + categoryName
                + "]";
    }
}
