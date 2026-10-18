// InlinePage.xhtml の data-member-name 属性から値を取り出し、textContent で書き込む
const data = document.getElementById("memberData").dataset;
document.getElementById("greeting").textContent =
    "Hello, " + data.memberName + "!";
