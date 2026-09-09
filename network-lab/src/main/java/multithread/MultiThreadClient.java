package multithread;

import java.io.*;
import java.net.*;

public class MultiThreadClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 8889);

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String[] messages = {"第一条消息", "第二条消息", "第三条消息"};

        for (String msg : messages) {
            out.println(msg);
            System.out.println("发送：" + msg);
            System.out.println("收到：" + in.readLine());
        }

        socket.close();
    }
}