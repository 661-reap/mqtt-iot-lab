package co2sensor;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttClient {
    private MqttAsyncClient client;
    private String brokerUrl;

    public MqttClient(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public void connect() throws MqttException {
        client = new MqttAsyncClient(brokerUrl, "CO2Sensor_001", new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        client.connect(options).waitForCompletion();
        System.out.println("MQTT 连接成功");
    }

    public void publish(String topic, byte[] payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(1);
        client.publish(topic, message).waitForCompletion();
        System.out.println("发布成功");
    }

    public void disconnect() throws MqttException {
        client.disconnect().waitForCompletion();
    }
}