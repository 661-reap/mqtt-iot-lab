package smarthome;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class MqttClient extends Observable implements MqttCallback {
    private MqttAsyncClient client;
    private String brokerUrl;
    private String clientId;

    public MqttClient(String brokerUrl, String clientId) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
    }


    public void connect() throws MqttException {
        client = new MqttAsyncClient(brokerUrl, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        client.setCallback(this);
        client.connect(options).waitForCompletion();
        System.out.println("【" + clientId + "】连接成功");
    }

    public void subscribe(String topic) throws MqttException {
        client.subscribe(topic, 1).waitForCompletion();
        System.out.println("订阅主题：" + topic + "成功");
    }

    public void publish(String topic, byte[] payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(1);
        client.publish(topic, message).waitForCompletion();
    }

    public void disconnect() throws MqttException {
        client.disconnect().waitForCompletion();
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("连接断开：" + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Map<String, Object> info = new HashMap<>();
        info.put("topic", topic);
        info.put("payload", message.getPayload());
        this.setChanged();
        this.notifyObservers(info);
        this.clearChanged();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    public void publish(String topic, String text) throws MqttException {
        MqttMessage message = new MqttMessage(text.getBytes());
        message.setQos(1);
        client.publish(topic, message).waitForCompletion();
    }
}