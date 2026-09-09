package udp;

import java.net.*;

public class UDPReceiver {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(9999);
        System.out.println("UDP接收方启动，等待数据...");

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        socket.receive(packet);

        String msg = new String(packet.getData(), 0, packet.getLength());
        System.out.println("收到来自 " + packet.getAddress() + " 的消息：" + msg);

        socket.close();
    }
}