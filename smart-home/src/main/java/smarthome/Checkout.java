package smarthome;

public class Checkout {
    public static byte getXor(byte[] data) {
        byte xor = 0;
        for (int i = 0; i < data.length - 1; i++) {
            xor ^= data[i];
        }
        return xor;
    }
}