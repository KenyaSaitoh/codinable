// 社員通知の画面。Webブラウザ標準の WebSocket の API だけを使う（STOMP のライブラリは使わない）
const form = document.getElementById('notice-form');
const statusLine = document.getElementById('status');
const list = document.getElementById('notices');
let notices = [];   // この画面のメモリ上の履歴（最大100件。サーバーには保存されない）

function showStatus(text) {
  statusLine.textContent = text;
}

function renderNotices() {
  list.replaceChildren(...notices.map((n) => {
    const li = document.createElement('li');
    li.textContent = `${n.occurredAt}  ${n.employeeId} ${n.employeeName}: ${n.message}`;
    return li;
  }));
}

const socket = new WebSocket(location.origin.replace(/^http/, 'ws')
    + '/jakarta-websocket-employee/ws/employees'); // 【1】
socket.addEventListener('open', () => showStatus('接続しました'));
socket.addEventListener('close', () => showStatus('切断しました（自動の再接続はしません。再読み込みで接続し直します）'));
socket.addEventListener('message', (event) => { // 【2】
  const data = JSON.parse(event.data);
  if ('error' in data) { showStatus(data.error); return; }
  notices = [data, ...notices].slice(0, 100);
  renderNotices();
});
form.addEventListener('submit', (event) => { // 【3】
  event.preventDefault();
  const employeeId = Number(document.getElementById('employeeId').value);
  const employeeName = document.getElementById('employeeName').value;
  const message = document.getElementById('message').value;
  if (socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify({ employeeId, employeeName, message }));
  }
});
document.getElementById('disconnect').addEventListener('click', () => socket.close());
