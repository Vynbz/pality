package io.pality.runtime;

final class HexDecoder {
    public static byte[] hexStringToByteArray(String hexString) {
        if (hexString == null) {
            throw new IllegalArgumentException("Invalid hex string: null");
        }
        hexString = hexString.replaceAll("\\s+", "");
        if (hexString.isEmpty() || hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string: odd number of characters");
        }
        final int len = hexString.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

}
