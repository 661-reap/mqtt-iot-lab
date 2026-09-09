package multithread;

import java.io.*;
import java.net.*;

public class MultiThreadServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8889);
        System.out.println("多线程服务器启动，等待连接...");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("新客户端连接：" + socket.getInetAddress());
            new Thread(new ClientHandler(socket)).start();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("收到消息：" + msg);
                out.println("服务器回复：" + msg);
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}