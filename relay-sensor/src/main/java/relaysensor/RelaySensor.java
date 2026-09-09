package relaysensor;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class RelaySensor implements Observer {
    private String mac;        // 网关MAC
    private int nodeType;      // 节点类型
    private String addr;       // 节点IP地址
    private boolean state;     // 继电器当前状态

    public RelaySensor(String mac, int nodeType, String addr) {
        this.mac = mac;
        this.nodeType = nodeType;
        this.addr = addr;
        this.state = false;
    }

    // 组装并上传继电器状态
    public void upload(MqttClient mqttClient) throws Exception {
        String topic = "IOTV3-GW/GW{" + mac + "}";

        byte[] payload = new byte[18];
        payload[0] = 0x55;         // 包头
        payload[1] = 0x12;         // 包长度
        payload[2] = 0x01;         // 消息类型低字节
        payload[3] = 0x00;         // 消息类型高字节
        payload[4] = (byte) nodeType; // 节点类型 0x02

        // IP地址：192.168.1.100 → 0xC0,0xA8,0x01,0x64
        String[] ipParts = addr.split("\\.");
        for (int i = 0; i < 4; i++) {
            payload[5 + i] = (byte) Integer.parseInt(ipParts[i]);
        }

        // LED状态3字节，KEY状态2字节，全0
        // payload[9-13] 默认已经是0

        // 传感器ID：0x0302，低字节在前
        payload[14] = 0x02;
        payload[15] = 0x03;

        // 继电器状态：常开=1，常闭=2
        payload[16] = state ? (byte) 0x02 : (byte) 0x01;

        // 异或校验
        payload[17] = Checkout.getXor(payload);

        mqttClient.publish(topic, payload);
    }

    // 接收并解析控制指令
    @Override
    public void update(Observable o, Object arg) {
        Map<String, Object> info = (Map<String, Object>) arg;
        String topic = (String) info.get("topic");
        byte[] payload = (byte[]) info.get("payload");

        // 验证包头
        if (payload[0] != 0x55) return;

        // 验证消息类型是否为控制指令 0x8001
        if (payload[2] != (byte) 0x01 || payload[3] != (byte) 0x80) return;

        // 验证节点类型是否为Wifi 0x02
        if (payload[4] != (byte) 0x02) return;

        // 验证节点IP地址是否匹配
        String[] ipParts = addr.split("\\.");
        for (int i = 0; i < 4; i++) {
            if (payload[5 + i] != (byte) Integer.parseInt(ipParts[i])) return;
        }

        // 解析控制字段第10字节
        byte control = payload[10];
        if (control == 0x01) {
            state = false;
            System.out.println("继电器被关闭了");
        } else if (control == 0x02) {
            state = true;
            System.out.println("继电器被打开了");
        }
    }

    public static void main(String[] args) throws Exception {
        String gatewayMac = "1A2B3C4D5E6F";
        String nodeIp = "192.168.1.100";

        RelaySensor relay = new RelaySensor(gatewayMac, 0x02, nodeIp);
        MqttClient mqttClient = new MqttClient("tcp://localhost:1883");

        mqttClient.connect();

        // 添加继电器为观察者
        mqttClient.addObserver(relay);

        // 订阅控制指令主题
        mqttClient.subscribe("IOTV3-APP/GW{" + gatewayMac + "}");

        // 上传初始状态
        relay.upload(mqttClient);

        // 保持程序运行，持续监听控制指令
        System.out.println("继电器运行中，等待控制指令...");
        while (true) {
            Thread.sleep(1000);
        }
    }
}