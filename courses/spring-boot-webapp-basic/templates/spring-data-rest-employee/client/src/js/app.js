/* 講義対象はapi。これは生成されたAPIとHALを観察するためのnpm不要の補助画面。 */
"use strict";

const element = (id) => document.getElementById(id);
const expand = (href) => href.replace(/\{[^}]*\}/g, "");
const state = {
    collections: {}, searches: {}, departments: new Map(), jobs: new Map(),
    next: null, prev: null, editing: null, busy: false
};

// URIテンプレートの省略可能部分を除去してから、レスポンスが返したリンクをたどる。
async function request(href, options = {}) {
    const response = await fetch(expand(href), {
        ...options,
        headers: { Accept: "application/hal+json", ...options.headers }
    });
    const text = await response.text();
    let body;
    try {
        body = text ? JSON.parse(text) : null;
    } catch {
        body = text;
    }
    const etag = response.headers.get("ETag");
    element("responseInfo").textContent = [
        `${options.method || "GET"} ${expand(href)}`,
        `HTTP ${response.status}`,
        etag ? `ETag: ${etag}` : "",
        response.headers.get("Location") ? `Location: ${response.headers.get("Location")}` : ""
    ].filter(Boolean).join("\n");
    element("responseBody").textContent = typeof body === "string" ? body : JSON.stringify(body, null, 2);
    if (!response.ok) {
        const detail = response.status === 412
            ? "他の操作で更新されています。「編集」で再取得してください。"
            : JSON.stringify(body);
        throw new Error(`HTTP ${response.status}: ${detail}`);
    }
    return { body, etag };
}

async function execute(action) {
    if (state.busy) return;
    state.busy = true;
    element("saveButton").disabled = true;
    element("message").textContent = "";
    element("message").className = "";
    try {
        await action();
    } catch (error) {
        element("message").textContent = error.message;
        element("message").className = "error";
    } finally {
        state.busy = false;
        element("saveButton").disabled = false;
    }
}

async function connect() {
    state.collections = {};
    state.searches = {};
    clearForm();
    const { body } = await request(element("apiBase").value.replace(/\/$/, ""));
    for (const name of ["employees", "departments", "jobs"]) {
        state.collections[name] = expand(body._links[name].href);
    }
    for (const [name, id, label] of [
        ["departments", "departmentId", "departmentName"],
        ["jobs", "jobId", "jobName"]
    ]) {
        const url = new URL(state.collections[name]);
        url.searchParams.set("size", "100");
        url.searchParams.set("sort", `${id},asc`);
        const result = await request(url.toString());
        const records = result.body._embedded[name];
        state[name] = new Map(records.map((item) => [item[id], item[label]]));
        element(id).replaceChildren(...records.map((item) => {
            const option = document.createElement("option");
            option.value = item[id];
            option.textContent = `${item[id]}: ${item[label]}`;
            return option;
        }));
    }
    const searches = await request(`${state.collections.employees}/search`);
    state.searches = searches.body._links;
    await loadList();
}

async function loadList(href) {
    if (!state.collections.employees) throw new Error("先にAPIへ接続してください。");
    let url;
    if (href) {
        url = new URL(expand(href));
    } else {
        const type = element("searchType").value;
        url = new URL(type ? expand(state.searches[type].href) : state.collections.employees);
        if (type) {
            const value = element("searchValue").value.trim();
            if (!value) throw new Error("検索する値を入力してください。");
            const parameters = {
                "by-name": "keyword", "by-department": "departmentId",
                "by-job": "jobId", "by-salary": "lowerSalary"
            };
            url.searchParams.set(parameters[type], value);
        }
        url.searchParams.set("size", "5");
        url.searchParams.set("sort", "employeeId,asc");
        if (element("projection").checked) url.searchParams.set("projection", "summary");
    }
    const { body } = await request(url.toString());
    state.next = body._links.next?.href;
    state.prev = body._links.prev?.href;
    element("nextButton").disabled = !state.next;
    element("prevButton").disabled = !state.prev;
    element("pageInfo").textContent =
        `page.number = ${body.page.number}（0始まり） / 全${body.page.totalElements}件 / ${body.page.totalPages}ページ`;
    renderRows(body._embedded?.employees || []);
}

function renderRows(employees) {
    element("employeeTable").querySelector("tbody").replaceChildren(...employees.map((employee) => {
        const row = document.createElement("tr");
        const cells = [employee.employeeCode, employee.employeeName,
            state.departments.get(employee.departmentId), state.jobs.get(employee.jobId),
            employee.salary ?? "—", employee.entranceDate ?? "—"];
        for (const value of cells) {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        }
        const actions = document.createElement("td");
        for (const [label, action] of [
            ["編集", () => editEmployee(employee._links.self.href)],
            ["削除", () => deleteEmployee(employee._links.self.href)]
        ]) {
            const button = document.createElement("button");
            button.type = "button";
            button.textContent = label;
            button.addEventListener("click", () => execute(action));
            actions.appendChild(button);
        }
        row.appendChild(actions);
        return row;
    }));
}

async function editEmployee(href) {
    // Projectionの省略項目を空値で上書きしないよう、通常の単一リソースを読み直す。
    const url = new URL(expand(href));
    url.searchParams.delete("projection");
    const { body, etag } = await request(url.toString());
    if (!etag) throw new Error("更新に必要なETagを取得できませんでした。");
    state.editing = { href: expand(body._links.self.href), etag };
    for (const name of ["employeeName", "departmentId", "jobId", "salary", "entranceDate"]) {
        element(name).value = body[name];
    }
    element("formTitle").textContent = `社員の編集: ${body.employeeCode}`;
    element("editingInfo").textContent = `保存時に If-Match: ${etag} を送信します。`;
}

function clearForm() {
    state.editing = null;
    element("employeeForm").reset();
    element("formTitle").textContent = "社員の新規登録";
    element("editingInfo").textContent = "社員コードはサーバーが採番します。";
}

async function saveEmployee() {
    const input = {
        employeeName: element("employeeName").value,
        departmentId: Number(element("departmentId").value),
        jobId: Number(element("jobId").value),
        salary: Number(element("salary").value),
        entranceDate: element("entranceDate").value
    };
    const editing = state.editing;
    await request(editing ? editing.href : state.collections.employees, {
        method: editing ? "PUT" : "POST",
        headers: {
            "Content-Type": "application/json",
            ...(editing ? { "If-Match": editing.etag } : {})
        },
        body: JSON.stringify(input)
    });
    clearForm();
    await loadList();
    element("message").textContent = editing ? "更新しました。" : "登録しました。";
}

async function deleteEmployee(href) {
    const url = new URL(expand(href));
    url.searchParams.delete("projection");
    const { etag } = await request(url.toString());
    if (!etag) throw new Error("削除に必要なETagを取得できませんでした。");
    await request(url.toString(), { method: "DELETE", headers: { "If-Match": etag } });
    clearForm();
    await loadList();
    element("message").textContent = "論理削除しました。";
}

for (const [id, action] of [
    ["connectionForm", connect], ["searchForm", () => loadList()], ["employeeForm", saveEmployee]
]) {
    element(id).addEventListener("submit", (event) => {
        event.preventDefault();
        execute(action);
    });
}
element("clearButton").addEventListener("click", clearForm);
element("nextButton").addEventListener("click", () => execute(() => loadList(state.next)));
element("prevButton").addEventListener("click", () => execute(() => loadList(state.prev)));
element("rootButton").addEventListener("click", () => execute(() => request(element("apiBase").value)));
element("searchButton").addEventListener("click", () => execute(() => {
    if (!state.collections.employees) throw new Error("先にAPIへ接続してください。");
    return request(`${state.collections.employees}/search`);
}));
execute(connect);
