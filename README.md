# MQTT IoT Lab

网络编程 / IoT 实习项目，围绕 MQTT 协议展开，从基础的 TCP/UDP 通信、手写 MQTT 报文，逐步过渡到基于 Eclipse Paho 客户端的传感器数据上报与智能家居联动控制。

## 项目结构

```
mqtt-iot-lab/
├── network-lab/     基础网络编程练习：TCP、UDP、多线程 Socket、手写 MQTT 报文
├── co2-sensor/       CO2 传感器 MQTT 上报示例
├── relay-sensor/     继电器（开关设备）MQTT 上报 + 远程控制示例
├── smart-home/       综合示例：CO2 + PM2.5 传感器联动控制新风系统/窗户
├── docs/             实习报告
└── capture/          MQTT 抓包文件（Wireshark）
```

各模块按学习/开发的演进顺序排列：`network-lab`（协议原理）→ `co2-sensor` / `relay-sensor`（单设备 MQTT 收发）→ `smart-home`（多设备联动的综合应用）。

## 各模块说明

### network-lab
网络编程基础练习，四个独立的小程序：
- `tcp/` — 最基础的 TCP 客户端/服务端
- `udp/` — UDP 收发
- `multithread/` — 多线程 TCP 服务端（每个客户端连接一个线程）
- `mqtt/`
  - `MQTTClient.java`：**不依赖任何 MQTT 库**，手动按 MQTT 3.1.1 协议格式拼装 CONNECT / SUBSCRIBE / PUBLISH 报文字节，直接用 Socket 与 Broker（如 EMQX）通信，用于理解协议报文结构
  - `MQTTPublisher.java` / `MQTTSubscriber.java`：改用 Eclipse Paho 客户端库实现的发布/订阅端

### co2-sensor
模拟一个 CO2 传感器节点，通过命令行输入数值后编码成自定义二进制协议并通过 MQTT 上报。

### relay-sensor
模拟一个继电器（如新风系统开关），既能上报自身状态，也能订阅控制主题、接收远程开关指令。

### smart-home
综合项目，整合了 CO2 传感器、PM2.5 传感器、继电器，加入了 `Controller.java` 做联动逻辑：**当室内 CO2 超标（>1000ppm）且室外 PM2.5 达标（<35μg/m³）时，自动打开新风系统和窗户**，反之关闭。

## 通信协议说明

设备上报数据使用自定义二进制格式（非标准 MQTT payload），通过 MQTT topic `IOTV3-GW/GW{网关MAC}` 发布：

| 字节 | 内容 |
|---|---|
| 0 | 包头 `0x55` |
| 1 | 包长度 |
| 2-3 | 消息类型（低字节在前） |
| 4 | 节点类型 |
| 5-10 | 节点 MAC / IP 地址 |
| ... | LED/KEY 状态等 |
| 末2字节前 | 传感器 ID + 数值（float，小端序） |
| 末字节 | 异或校验（`Checkout.getXor`） |

控制指令则发布到 `IOTV3-APP/GW{网关MAC}`，由继电器节点订阅并解析。

## 环境依赖

- JDK 11+
- Maven 3.6+
- 一个本地运行的 MQTT Broker（默认代码里连接 `tcp://localhost:1883`），推荐用 [Mosquitto](https://mosquitto.org/) 或 [EMQX](https://www.emqx.io/)：
  ```bash
  # 例如用 Docker 快速起一个 Mosquitto
  docker run -it -p 1883:1883 eclipse-mosquitto
  ```

## 如何运行

```bash
# 编译整个项目（各模块）
mvn clean install

# 运行某个模块，例如 co2-sensor
cd co2-sensor
mvn exec:java -Dexec.mainClass="co2sensor.CO2Sensor"
```

> 注：原项目依赖的 `org.eclipse.paho.client.mqttv3-1.2.5.jar` 已改为 Maven 依赖管理，无需手动下载 jar 包。

## 抓包分析

`capture/mqtt_capture.pcapng` 是运行过程中抓取的 MQTT 通信数据包，可用 [Wireshark](https://www.wireshark.org/) 打开查看 CONNECT / PUBLISH / SUBSCRIBE 等报文的完整交互过程，配合 `network-lab/mqtt/MQTTClient.java` 手写的报文拼装逻辑对照阅读效果更佳。

## 实习报告

详见 [`docs/internship-report.pdf`](docs/internship-report.pdf)。
