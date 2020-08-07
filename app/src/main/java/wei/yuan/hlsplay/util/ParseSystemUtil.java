package wei.yuan.hlsplay.util;

import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * 进制转换工具类
 *
 */
public class ParseSystemUtil {

    /**
     * 将二进制byte[]转换成16进制
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转成二进制byte[]
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        char[] hex = hexStr.toCharArray();
        // 转result长度减半
        int length = hex.length / 2;
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            // 先将hex数据转成10进制数值
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            // 将第一个值的二进制值左平移4位  ex: 00001000 => 10000000 (8=>128)
            // 与第二个值的二进制值作联集    ex: 10000000 | 00001100 => 10001100 (137)
            int value = (high << 4) | low;
            // 与FFFFFFFF作补集
            if (value > 127) {
                value -= 256;
            }
            // 最后转回byte
            result[i] = (byte) value;
        }
        return result;
    }

    /**
     * 截取byte数组   不改变原数组
     * @param b 原数组
     * @param off 偏差值（索引）
     * @param length 长度
     * @return 截取后的数组
     */
    public static byte[] getSubBytes(byte[] b, int off, int length) {
        byte[] b1 = new byte[length];
        System.arraycopy(b, off, b1, 0, length);
        return b1;
    }

    /**
     * 合并byte[]数组 （不改变原数组）
     * @param b1
     * @param b2
     * @return 合并后的数组
     */
    public static byte[] getMergedBytes(byte[] b1, byte[] b2) {
        byte[] b = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, b, 0, b1.length);
        System.arraycopy(b2, 0, b, b1.length, b2.length);
        return b;
    }

    /**
     * 根据字节数生成元素均为0的byte数组
     * @param length
     * @return
     */
    public static byte[] getZeroBytes(int length) {
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = 0;
        }
        return b;
    }

    /**
     * byte数组转换成int类型
     * @param bytes
     * @return
     */
    public static int bytesToInt(byte[] bytes) {
        return bytes[3] & 0xFF | (bytes[2] & 0xFF) << 8 | (bytes[1] & 0xFF) << 16 | (bytes[0] & 0xFF << 24);
    }

    public static int getBytesLengthWithoutZero(byte[] bytes) {
        int length = bytes.length;
        int index = 0;
        for (int i = 0; i < length; i = i + 2) {
            byte[] b = getSubBytes(bytes, i, 2);
            String hexStr = parseByte2HexStr(b);
            if (hexStr.equals("0000")) {
                index = i;
                break;
            }
        }

        return index;
    }
}
