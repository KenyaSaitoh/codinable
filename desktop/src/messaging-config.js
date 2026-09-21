// Fixed versions are shared by setup, the installer, and the runtime status panel.
const VERSIONS = { kafka: '4.3.1', rabbitmq: '4.3.6', erlang: '28.5' };
const PORTS = { kafka: 9092, controller: 9093, rabbitmq: 5672, management: 15672,
                distribution: 25672, epmd: 43690 };

function connectionEnv(ports = PORTS) {
  return {
    KAFKA_BOOTSTRAP_SERVERS: `127.0.0.1:${ports.kafka}`,
    SPRING_KAFKA_BOOTSTRAP_SERVERS: `127.0.0.1:${ports.kafka}`,
    RABBITMQ_URL: `amqp://guest:guest@127.0.0.1:${ports.rabbitmq}/`,
    SPRING_RABBITMQ_HOST: '127.0.0.1',
    SPRING_RABBITMQ_PORT: String(ports.rabbitmq),
    SPRING_RABBITMQ_USERNAME: 'guest',
    SPRING_RABBITMQ_PASSWORD: 'guest',
  };
}

module.exports = { VERSIONS, PORTS, connectionEnv };
