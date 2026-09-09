package tcp;

import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("服务器启动，等待连接...");

        Socket socket = serverSocket.accept();
        System.out.println("客户端已连接：" + socket.getInetAddress());

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String msg = in.readLine();
        System.out.println("收到客户端消息：" + msg);
        out.println("服务器已收到：" + msg);
        socket.close();
        serverSocket.close();
    }
}