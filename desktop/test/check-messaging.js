// Integration check against the real portable Windows distributions.
// Uses temporary data and free ports; never touches a learner's brokers or data.
const assert = require('node:assert/strict');
const fs = require('fs');
const os = require('os');
const path = require('path');
const net = require('net');
const { spawn } = require('child_process');
const { MessagingManager, requirements, portOpen } = require('../src/main/messaging');

const repo = path.resolve(__dirname, '../..');
const root = fs.mkdtempSync(path.join(os.tmpdir(), process.env.CHECK_UNICODE ? 'codinable 日本語 ' : 'codinable-messaging-test '));
const runtime = name => path.join(repo, 'runtime', name);
const servers = [];
let manager;

async function reservePort() {
  const server = net.createServer();
  await new Promise((resolve, reject) => { server.once('error', reject); server.listen(0, '127.0.0.1', resolve); });
  servers.push(server);
  return server.address().port;
}

async function api(method, route, body) {
  const res = await fetch(`http://127.0.0.1:${manager.ports.management}/api/${route}`, {
    method, headers: { Authorization: 'Basic ' + Buffer.from('guest:guest').toString('base64'), 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(5000),
  });
  assert.ok(res.ok, `${method} ${route}: ${res.status} ${await res.clone().text()}`);
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

async function javaCheck() {
  const java = path.join(runtime('java'), 'bin', 'java.exe');
  const file = path.join(root, 'KafkaRoundTrip.java');
  fs.writeFileSync(file, `
import java.time.Duration;
import java.util.*;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.clients.consumer.*;
public class KafkaRoundTrip {
 public static void main(String[] args) throws Exception {
  var p = new Properties(); p.put("bootstrap.servers", args[0]);
  try(var admin = Admin.create(p)) { admin.createTopics(List.of(new NewTopic("codinable-check", 1, (short)1))).all().get(); }
  p.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
  p.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
  try(var producer = new KafkaProducer<String,String>(p)) { producer.send(new ProducerRecord<>("codinable-check", "hello Kafka")).get(); }
  p.put("group.id", "check"); p.put("auto.offset.reset", "earliest");
  p.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
  p.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
  try(var consumer = new KafkaConsumer<String,String>(p)) {
   consumer.subscribe(List.of("codinable-check")); long end = System.currentTimeMillis() + 15000;
   while(System.currentTimeMillis() < end) for(var r : consumer.poll(Duration.ofMillis(300)))
    if(r.value().equals("hello Kafka")) { System.out.println("KAFKA_ROUND_TRIP_OK"); return; }
   throw new IllegalStateException("No message received");
  }
 }
}`);
  const proc = spawn(java, ['-cp', path.join(runtime('kafka'), 'libs', '*'), file, `127.0.0.1:${manager.ports.kafka}`], { windowsHide: true });
  let output = '';
  proc.stdout.on('data', d => { output += d; });
  proc.stderr.on('data', d => { output += d; });
  const timer = setTimeout(() => proc.kill(), 45000);
  const code = await new Promise((resolve, reject) => { proc.on('error', reject); proc.on('close', resolve); });
  clearTimeout(timer);
  assert.equal(code, 0, output);
  assert.match(output, /KAFKA_ROUND_TRIP_OK/);
}

(async () => {
  const ports = {};
  for (const name of ['kafka', 'controller', 'rabbitmq', 'management', 'distribution', 'epmd']) ports[name] = await reservePort();
  manager = new MessagingManager({ dataDir: root, ports,
    kafkaDir: runtime('kafka'), rabbitmqDir: runtime('rabbitmq'), erlangDir: runtime('erlang'),
    javaExe: path.join(runtime('java'), 'bin', 'java.exe'), javacExe: path.join(runtime('java'), 'bin', 'javac.exe') });
  console.log('Test data: ' + root);
  assert.deepEqual(requirements(root), []);
  fs.writeFileSync(path.join(root, 'codinable.services.json'), '{"services":["kafka","kafka","rabbitmq"]}');
  assert.deepEqual(requirements(root), ['kafka', 'rabbitmq']);
  assert.throws(() => manager.start('../outside'), /Unknown/);
  await assert.rejects(manager.start('kafka'), /already in use/);
  assert.equal(await portOpen(ports.kafka), true, 'A port collision must not kill the other server');
  console.log('PASS invalid IDs, dependency config, and occupied ports');
  await Promise.all(servers.splice(0).map(s => new Promise(resolve => s.close(resolve))));
  await Promise.all([manager.start('kafka'), manager.start('kafka')]);
  console.log('PASS Kafka start and duplicate start');
  await javaCheck();
  console.log('PASS Kafka produce / consume');
  await manager.start('rabbitmq');
  console.log('PASS RabbitMQ start alongside Kafka');
  await api('PUT', 'queues/%2F/codinable-check', { durable: true, auto_delete: false, arguments: {} });
  const published = await api('POST', 'exchanges/%2F/amq.default/publish', { properties: { delivery_mode: 2 }, routing_key: 'codinable-check', payload: 'hello RabbitMQ', payload_encoding: 'string' });
  assert.equal(published.routed, true);
  await assert.rejects(manager.reset('rabbitmq'), /Stop the server/);
  await manager.stop('rabbitmq');
  for (const p of ['rabbitmq', 'management', 'distribution', 'epmd']) assert.equal(await portOpen(ports[p]), false, p + ' left listening');
  await manager.start('rabbitmq');
  const messages = await api('POST', 'queues/%2F/codinable-check/get', { count: 1, ackmode: 'ack_requeue_false', encoding: 'auto', truncate: 500 });
  assert.equal(messages[0]?.payload, 'hello RabbitMQ');
  console.log('PASS RabbitMQ publish / consume, restart persistence, all listeners stop');
  const metadata = fs.readFileSync(path.join(root, 'kafka/data/meta.properties'), 'utf8');
  await manager.stop('kafka');
  assert.equal(await portOpen(ports.kafka), false);
  await manager.start('kafka');
  assert.equal(fs.readFileSync(path.join(root, 'kafka/data/meta.properties'), 'utf8'), metadata);
  console.log('PASS Kafka restart retains cluster metadata');
  await manager.stop('kafka');
  await manager.reset('kafka');
  assert.equal(fs.existsSync(path.join(root, 'kafka/data')), false);
  const starting = manager.start('kafka');
  const result = starting.catch(e => e);
  await manager.stop('kafka');
  await result;
  assert.equal(manager.status().find(s => s.id === 'kafka').state, 'stopped');
  console.log('PASS reset and cancellation during startup');
})().catch(err => {
  console.error(err);
  if (manager) for (const s of manager.status()) if (s.state === 'error') console.error(s.id + '\n' + s.log.slice(-15000));
  process.exitCode = 1;
}).finally(async () => {
  await manager?.dispose();
  await Promise.all(servers.map(s => new Promise(resolve => s.close(resolve))));
  if (!process.exitCode) { fs.rmSync(root, { recursive: true, force: true }); console.log('PASS shutdown and cleanup'); }
});
