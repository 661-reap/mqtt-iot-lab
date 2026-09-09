package tcp;

import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 8888);

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.println("Hello, Server!");

        String response = in.readLine();
        System.out.println("收到服务器回复：" + response);

        socket.close();
    }
}