package mqtt;

import java.io.*;
import java.net.*;
import java.io.ByteArrayOutputStream;

public class MQTTClient {
    static OutputStream out;
    static InputStream in;

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 1883);
        out = socket.getOutputStream();
        in = socket.getInputStream();
        System.out.println("已连接到EMQX服务器");

        sendConnect("JavaClient001");
        byte[] connack = readPacket();
        System.out.println("CONNACK原始字节：" + connack[0] + " " + connack[1] + " " + connack[2] + " " + connack[3]);
        System.out.println("CONNACK响应码：" + connack[3]);

        sendSubscribe("test/topic", 1);
        byte[] suback = readPacket();
        System.out.println("SUBACK收到，订阅成功");

        sendPublish("test/topic", "Hello MQTT from Java!");
        System.out.println("PUBLISH已发送");

        Thread.sleep(2000);
        byte[] publish = readPacket();
        String received = new String(publish, publish.length - 21, 21);
        System.out.println("收到消息：" + received);
        Thread.sleep(10000);
        socket.close();
    }

    static void sendConnect(String clientId) throws Exception {
        byte[] clientIdBytes = clientId.getBytes();

        // 可变头部 + payload 长度
        int remainingLength = 2 + 4 + 1 + 1 + 2 + 2 + clientIdBytes.length;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 固定头部
        baos.write(0x10);
        baos.write(remainingLength);
        // 协议名 "MQTT"
        baos.write(0x00); baos.write(0x04);
        baos.write('M'); baos.write('Q'); baos.write('T'); baos.write('T');
        // 协议级别 MQTT 3.1.1
        baos.write(0x04);
        // 连接标志 只设置 Clean Session
        baos.write(0x02);
        // Keep Alive 60秒
        baos.write(0x00); baos.write(0x3C);
        // ClientId
        baos.write(0x00); baos.write(clientIdBytes.length);
        baos.write(clientIdBytes);

        out.write(baos.toByteArray());
        out.flush();
        System.out.println("CONNECT已发送");
    }

    static void sendSubscribe(String topic, int packetId) throws Exception {
        byte[] topicBytes = topic.getBytes();
        int length = 2 + 2 + topicBytes.length + 1;
        byte[] packet = new byte[2 + length];

        packet[0] = (byte) 0x82;
        packet[1] = (byte) length;
        packet[2] = 0x00; packet[3] = (byte) packetId;
        packet[4] = 0x00; packet[5] = (byte) topicBytes.length;
        System.arraycopy(topicBytes, 0, packet, 6, topicBytes.length);
        packet[6 + topicBytes.length] = 0x00;

        out.write(packet);
        out.flush();
        System.out.println("SUBSCRIBE已发送，Topic：" + topic);
    }

    static void sendPublish(String topic, String message) throws Exception {
        byte[] topicBytes = topic.getBytes();
        byte[] messageBytes = message.getBytes();
        int length = 2 + topicBytes.length + messageBytes.length;
        byte[] packet = new byte[2 + length];

        packet[0] = 0x30;
        packet[1] = (byte) length;
        packet[2] = 0x00; packet[3] = (byte) topicBytes.length;
        System.arraycopy(topicBytes, 0, packet, 4, topicBytes.length);
        System.arraycopy(messageBytes, 0, packet, 4 + topicBytes.length, messageBytes.length);

        out.write(packet);
        out.flush();
    }

    static byte[] readPacket() throws Exception {
        int firstByte;
        while ((firstByte = in.read()) == -1) {
            Thread.sleep(100);
        }
        int length = in.read();
        if (length == -1) length = 0;
        byte[] packet = new byte[2 + length];
        packet[0] = (byte) firstByte;
        packet[1] = (byte) length;
        int offset = 2;
        while (offset < packet.length) {
            int bytesRead = in.read(packet, offset, packet.length - offset);
            if (bytesRead > 0) offset += bytesRead;
        }
        return packet;
    }
}