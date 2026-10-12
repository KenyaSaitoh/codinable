// jQuery で書いた名簿管理アプリケーション (レッスン7.2.1)
//
// $(function() { ... }) は $(document).ready(...) の省略記法で、
// DOM が組み上がってから中身を実行する。イベント登録と DOM 操作はすべてこの内側に書く
//
// 見どころは「編集」ボタンの処理。Person のデータはテーブルの <td> のテキストとして持っていて、
// ボタンを起点に DOM ツリーをたどって取り出している (DOM が表示とデータ保持を兼ねている)
// チャプター10.1 の React 版では、データを state として持つので、この「たどる」処理が要らなくなる

$(function() {
    // 原稿では http://localhost:8080/person_service/persons (JAX-RS)
    // ここでは同じパスを Express (server.js) が提供している
    const PERSONS_URL = "/person_service/persons";

    const GENDER_LABELS = { male: "男性", female: "女性" };

    // 起動時に一覧を取得 (GET) してテーブル領域を組み立てる
    $.ajax({
        url: PERSONS_URL,
        type: "GET",
        dataType: "json",
        success: function(persons) {
            $.each(persons, function(i, person) {
                addRowToTable(person);
            });
        },
        error: showError
    });

    // 「登録」ボタン押下時の処理
    $("#registerButton").on("click", function() {
        const idVal = $("#personId").val();
        const person = {
            personId:   idVal === "" ? null : Number(idVal),  // 新規はnull
            personName: $("#personName").val(),
            age:        Number($("#age").val()),
            gender:     $("#gender").val()
        };

        if (person.personName === "") {
            showMessage("名前を入力してください", true);
            return;
        }

        if (idVal === "") {
            // 新規登録：POST。サーバーは採番済みPersonを返す
            $.ajax({
                url: PERSONS_URL,
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(person),
                success: function(created) {
                    addRowToTable(created);  // 採番済みPersonをテーブルに追加
                    clearForm();             // 編集領域をクリア
                    showMessage("ID " + created.personId + " で登録しました（POST）");
                },
                error: showError
            });
        } else {
            // 既存更新：PUT。サーバーはvoidを返すため、送信したpersonでテーブルを更新
            $.ajax({
                url: PERSONS_URL + "/" + idVal,
                type: "PUT",
                contentType: "application/json",
                data: JSON.stringify(person),
                success: function() {
                    updateRowInTable(person);
                    clearForm();
                    showMessage("ID " + person.personId + " を更新しました（PUT）");
                },
                error: showError
            });
        }
    });

    // 「編集」ボタン押下時の処理（テーブル領域に対するイベント委譲）
    // 行は後から増えるので、tbody ではなく常にある #personTable に登録しておく
    $("#personTable").on("click", "button[name='edit']", function() {
        const row = $(this).parent().parent();                  // ボタン → td → trとたどる
        const personId   = row.children("td:first").text();     // 1列目（ID）
        const personName = row.children("td:eq(1)").text();     // 2列目（名前）
        const age        = row.children("td:eq(2)").text();     // 3列目（年齢）
        const genderText = row.children("td:eq(3)").text();     // 4列目（性別の表示文字列）
        // 性別は表示文字列（男性／女性）から値（male／female）に逆変換する
        const gender = genderText === GENDER_LABELS.female ? "female" : "male";

        // 編集領域に転記
        $("#personId").val(personId);
        $("#personName").val(personName);
        $("#age").val(age);
        $("#gender").val(gender);
        showMessage("ID " + personId + " を編集中です。「登録」で更新します");
    });

    // 「削除」ボタン押下時の処理（同じくイベント委譲）
    $("#personTable").on("click", "button[name='delete']", function() {
        const row = $(this).closest("tr");                      // closest でも親の tr を取れる
        const personId = row.children("td:first").text();

        $.ajax({
            url: PERSONS_URL + "/" + personId,
            type: "DELETE",
            success: function() {
                row.remove();
                if ($("#personId").val() === personId) clearForm();
                showMessage("ID " + personId + " を削除しました（DELETE）");
            },
            error: showError
        });
    });

    $("#clearButton").on("click", function() {
        clearForm();
        showMessage("");
    });

    // テーブル領域に1行追加する。データはtdのテキストとしてDOMに書き込まれる
    function addRowToTable(person) {
        const row = $("<tr>").attr("data-id", person.personId);
        row.append($("<td>").text(person.personId));
        row.append($("<td>").text(person.personName));
        row.append($("<td>").text(person.age));
        row.append($("<td>").text(GENDER_LABELS[person.gender]));
        row.append($("<td>").append(
            $("<button>").attr({ type: "button", name: "edit" }).text("編集"),
            " ",
            $("<button>").attr({ type: "button", name: "delete" }).text("削除")
        ));
        $("#personTable tbody").append(row);
    }

    // 更新した Person の行を探して、td のテキストを書き換える
    // 「どの DOM 要素を更新すべきか」を開発者が自分で追跡しなければならない
    function updateRowInTable(person) {
        const row = $("#personTable tbody tr[data-id='" + person.personId + "']");
        row.children("td:eq(1)").text(person.personName);
        row.children("td:eq(2)").text(person.age);
        row.children("td:eq(3)").text(GENDER_LABELS[person.gender]);
    }

    // 編集領域をクリアする
    function clearForm() {
        $("#personId").val("");
        $("#personName").val("");
        $("#age").val("");
        $("#gender").val("male");
    }

    function showMessage(text, isError) {
        $("#message").text(text).toggleClass("error", Boolean(isError));
    }

    function showError(xhr) {
        showMessage("通信に失敗しました（HTTP " + xhr.status + "）", true);
    }
});
