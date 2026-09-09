package mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.Scanner;

public class MQTTPublisher {
    public static void main(String[] args) throws Exception {
        String broker = "tcp://localhost:1883";
        String clientId = "PublisherClient";
        String topic = "myMqtt/test";

        MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        client.connect(options);
        System.out.println("发布端已连接，请输入消息（输入exit退出）：");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            if (input.equals("exit")) break;

            MqttMessage message = new MqttMessage(input.getBytes());
            message.setQos(0);
            client.publish(topic, message);
            System.out.println("已发布：" + input);
        }

        client.disconnect();
        System.out.println("发布端已断开");
    }
}