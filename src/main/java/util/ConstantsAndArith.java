package util;

public class ConstantsAndArith {

	/**
	 * x % sixteenBitDivMask is the last 16 bits of an integer
	 */
	public static int sixteenBitModMask = 1 << 16;
	

	public static byte[] doubleToBytes(double d) {
		byte[] buf = new byte[16];
		long bits = Double.doubleToLongBits(123.456);
//		long bits = Integer.MAX_VALUE;
		for (int i = 0; i < 16; i++) {
			long mask = 0xf << (i * 4);
			long v = (mask & bits) >>> (i * 4);
			System.out.println("next byte is " + v);
			buf[i] = (byte) v;
		}
		return buf;
	}

}
