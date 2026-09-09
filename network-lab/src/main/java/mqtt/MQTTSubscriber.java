package mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTSubscriber {
    public static void main(String[] args) throws Exception {
        String broker = "tcp://localhost:1883";
        String clientId = "SubscriberClient";
        String topic = "myMqtt/test";

        MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("连接丢失：" + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                System.out.println("收到消息 [" + topic + "]：" + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        client.connect(options);
        System.out.println("订阅端已连接");

        client.subscribe(topic, 0);
        System.out.println("已订阅主题：" + topic + "，等待消息...");
    }
}