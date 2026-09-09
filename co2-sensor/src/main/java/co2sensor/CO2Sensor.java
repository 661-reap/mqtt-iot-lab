package co2sensor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

public class CO2Sensor {
    private String mac;        // 网关MAC
    private int nodeType;      // 节点类型
    private String addr;       // 节点地址

    public CO2Sensor(String mac, int nodeType, String addr) {
        this.mac = mac;
        this.nodeType = nodeType;
        this.addr = addr;
    }

    public void upload(float co2Value) throws Exception {
        // 1. 组装topic
        String topic = "IOTV3-GW/GW{" + mac + "}";

        // 2. 组装payload
        byte[] payload = new byte[23];
        payload[0] = 0x55;                    // 包头
        payload[1] = 0x17;                    // 包长度
        payload[2] = 0x01;                    // 消息类型低字节
        payload[3] = 0x00;                    // 消息类型高字节
        payload[4] = (byte) nodeType;         // 节点类型 0x04

        // 节点MAC地址（6字节）
        byte[] addrBytes = hexStringToBytes(addr);
        System.arraycopy(addrBytes, 0, payload, 5, 6);

        // LED状态3字节，KEY状态2字节，全0
        // payload[11-15] 默认已经是0

        // 传感器ID: 0x0103（CO2），低字节在前
        payload[16] = 0x03;
        payload[17] = 0x01;

        // 传感器数据：float转字节，低字节在前
        byte[] floatBytes = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(co2Value)
                .array();
        System.arraycopy(floatBytes, 0, payload, 18, 4);

        // 异或校验
        payload[22] = Checkout.getXor(payload);

        // 3. 发布
        MqttClient mqttClient = new MqttClient("tcp://localhost:1883");
        mqttClient.connect();
        mqttClient.publish(topic, payload);
        mqttClient.disconnect();
        System.out.println("已上传CO2值：" + co2Value + " ppm");
    }

    private byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        CO2Sensor sensor = new CO2Sensor("1A2B3C4D5E6F", 0x04, "123456ABCDEF");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("请输入要上传的CO2值（ppm），输入q退出：");
            String input = scanner.next();
            if (input.equalsIgnoreCase("q")) {
                System.out.println("程序退出");
                break;
            }
            try {
                float value = Float.parseFloat(input);
                sensor.upload(value);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字或q退出");
            }
        }
        scanner.close();
    }
}