package smarthome;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PM25Sensor {
    private String mac;
    private int nodeType;
    private String addr;

    public PM25Sensor(String mac, int nodeType, String addr) {
        this.mac = mac;
        this.nodeType = nodeType;
        this.addr = addr;
    }

    public void upload(MqttClient mqttClient, float pm25Value) throws Exception {
        String topic = "IOTV3-GW/GW{" + mac + "}";

        byte[] payload = new byte[23];
        payload[0] = 0x55;
        payload[1] = 0x17;
        payload[2] = 0x01;
        payload[3] = 0x00;
        payload[4] = (byte) nodeType;

        byte[] addrBytes = hexStringToBytes(addr);
        System.arraycopy(addrBytes, 0, payload, 5, 6);

        // 传感器ID：0x0104 代表PM2.5，低字节在前
        payload[16] = 0x04;
        payload[17] = 0x01;

        // float转字节，低字节在前
        byte[] floatBytes = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(pm25Value)
                .array();
        System.arraycopy(floatBytes, 0, payload, 18, 4);

        payload[22] = Checkout.getXor(payload);

        mqttClient.publish(topic, payload);
        System.out.println("PM2.5数据已上传：" + pm25Value + " μg/m³");
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