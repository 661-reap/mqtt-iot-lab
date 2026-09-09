package smarthome;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class Controller implements Observer {
    private String gatewayMac;
    private float co2Value = 0;
    private float pm25Value = 0;
    private Boolean lastState = null; // null表示初始状态
    private boolean co2Received = false;
    private boolean pm25Received = false;

    // 两个继电器
    private RelaySensor ventRelay;   // 新风系统继电器
    private RelaySensor windowRelay; // 智能窗户继电器（可选）

    private MqttClient mqttClient;

    // 阈值
    private static final float CO2_THRESHOLD = 1000f;
    private static final float PM25_THRESHOLD = 35f;

    public Controller(String gatewayMac, MqttClient mqttClient) {
        this.gatewayMac = gatewayMac;
        this.mqttClient = mqttClient;
        this.ventRelay = new RelaySensor(gatewayMac, 0x02, "192.168.1.100", "新风系统");
        this.windowRelay = new RelaySensor(gatewayMac, 0x02, "192.168.1.101", "智能窗户");
    }

    @Override
    public void update(Observable o, Object arg) {
        Map<String, Object> info = (Map<String, Object>) arg;
        byte[] payload = (byte[]) info.get("payload");

        if (payload[0] != 0x55) return;
        if (payload[2] != 0x01 || payload[3] != 0x00) return;

        int sensorId = ((payload[17] & 0xFF) << 8) | (payload[16] & 0xFF);
        float value = ByteBuffer.wrap(payload, 18, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getFloat();

        if (sensorId == 0x0103) {
            co2Value = value;
            co2Received = true;
            System.out.println("📥 收到室内CO2：" + co2Value + " ppm");
        } else if (sensorId == 0x0104) {
            pm25Value = value;
            pm25Received = true;
            System.out.println("📥 收到室外PM2.5：" + pm25Value + " μg/m³");
        } else {
            return;
        }

        // 两个数据都收到才判断
        if (co2Received && pm25Received) {
            checkAndControl();
            co2Received = false;
            pm25Received = false;
        }
    }

    private void checkAndControl() {
        boolean shouldOpen = co2Value > CO2_THRESHOLD && pm25Value < PM25_THRESHOLD;

        // 只在状态改变时才执行
        if (lastState != null && lastState == shouldOpen) return;
        lastState = shouldOpen;

        try {
            if (shouldOpen) {
                System.out.println("【联动触发】CO2=" + co2Value + "ppm超标，PM2.5=" + pm25Value + "μg/m³达标 → 开启新风和窗户");
                ventRelay.sendControl(mqttClient, true);
                windowRelay.sendControl(mqttClient, true);
            } else {
                System.out.println("【联动触发】条件不满足（CO2=" + co2Value + "ppm，PM2.5=" + pm25Value + "μg/m³）→ 关闭新风和窗户");
                ventRelay.sendControl(mqttClient, false);
                windowRelay.sendControl(mqttClient, false);
            }
            String status = "CO2=" + co2Value + "ppm | PM2.5=" + pm25Value + "μg/m³ | 新风=" + (shouldOpen ? "开启" : "关闭") + " | 窗户=" + (shouldOpen ? "开启" : "关闭");
            mqttClient.publish("SmartHome/Status", status);
        } catch (Exception e) {
            System.out.println("控制指令发送失败：" + e.getMessage());
        }
    }
    public static void main(String[] args) throws Exception {
        String gatewayMac = "1A2B3C4D5E6F";
        String brokerUrl = "tcp://localhost:1883";

        MqttClient mqttClient = new MqttClient(brokerUrl, "SmartHome_Controller");
        mqttClient.connect();

        // Controller订阅传感器数据
        Controller controller = new Controller(gatewayMac, mqttClient);
        mqttClient.addObserver(controller);
        mqttClient.subscribe("IOTV3-GW/GW{" + gatewayMac + "}");

        // 启动CO2传感器
        CO2Sensor co2 = new CO2Sensor(gatewayMac, 0x04, "123456ABCDEF");
        MqttClient co2Client = new MqttClient(brokerUrl, "CO2Sensor_001");
        co2Client.connect();

        // 启动PM2.5传感器
        PM25Sensor pm25 = new PM25Sensor(gatewayMac, 0x04, "123456ABCDE0");
        MqttClient pm25Client = new MqttClient(brokerUrl, "PM25Sensor_001");
        pm25Client.connect();

        System.out.println("智能家居系统启动，每5秒上传一次数据...");

        // 模拟持续上传数据
        float[] co2Values  = {800, 1200, 1100, 900};
        float[] pm25Values = {20,  25,   40,   30 };

        for (int i = 0; i < co2Values.length; i++) {
            System.out.println("\n--- 第" + (i+1) + "轮数据 ---");
            co2.upload(co2Client, co2Values[i]);
            pm25.upload(pm25Client, pm25Values[i]);
            Thread.sleep(5000);
        }

        System.out.println("\n✅ 所有数据发送完毕，系统保持运行中");
        System.out.println("按回车键退出...");
        new java.util.Scanner(System.in).nextLine();

        co2Client.disconnect();
        pm25Client.disconnect();
        mqttClient.disconnect();



    }
}