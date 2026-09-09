package smarthome;

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

    public void upload(MqttClient mqttClient, float co2Value) throws Exception {
        String topic = "IOTV3-GW/GW{" + mac + "}";

        byte[] payload = new byte[23];
        payload[0] = 0x55;
        payload[1] = 0x17;
        payload[2] = 0x01;
        payload[3] = 0x00;
        payload[4] = (byte) nodeType;

        byte[] addrBytes = hexStringToBytes(addr);
        System.arraycopy(addrBytes, 0, payload, 5, 6);

        payload[16] = 0x03;
        payload[17] = 0x01;

        byte[] floatBytes = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(co2Value)
                .array();
        System.arraycopy(floatBytes, 0, payload, 18, 4);

        payload[22] = Checkout.getXor(payload);

        mqttClient.publish(topic, payload);
        System.out.println("CO2数据已上传：" + co2Value + " ppm");
    }
    private byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return result;
    }
}