package udp;

import java.net.*;

public class UDPSender {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket();

        String msg = "Hello, UDP!";
        byte[] data = msg.getBytes();

        DatagramPacket packet = new DatagramPacket(data, data.length,
                InetAddress.getByName("localhost"), 9999);

        socket.send(packet);
        System.out.println("消息已发送：" + msg);

        socket.close();
    }
}