# jpa-relations — 双方向の関連・fetch join・Criteria（導入・復習用）

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-relations` を、Codinable で単独で動くようにしたものです。

部署と社員の双方向の関連を持つ小さな教材です。関連の所有側・mappedBy・cascade・fetch はそれぞれ別の役割です。

## 読む順番

1. `Department` と `Employee` で、`@OneToMany(mappedBy)` と `@ManyToOne` の所有側、`orphanRemoval=true` を確認する。
2. `Department#addEmployee` が両方向の参照をそろえることを確認する。
3. `CompanyQuery` の fetch join と、Criteria API による給与条件の検索を読む。
4. `CompanyQueryTest` で、取得結果と発行される SQL を確認する。

`CascadeType.ALL` や EAGER は効果を比べるための設定で、全関連に適用する推奨値ではありません。
コレクションの fetch join とページングを同時に使うときは注意が要ります。
この Criteria は文字列で属性を指定するため、完全なコンパイル時の型安全性はありません。

## Codinable での動かし方

- 実行対象で **Gradle: test** を選び、「実行」を押します。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:jpa_relations`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。
