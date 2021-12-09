package test;

public class BitShift {

	/*
	 * Java operators:
	 * 
	 * ~ - bnot << signed shift left >> signed shift right
	 * >>> unsigned shift right, ie doesn't maintain sign bit so uses all 32 bits
	 * & band
	 * ^ bxor
	 * | bor
	 * 
	 * can of course use hex number for bitmasks...
	 */
	
	static int eightBitMask = 0xFF;
	static int sixteenBitMask = 0xFFFF;
	static int leftSixteenBitMask = 0xFFFF0000;

	public static void main(String[] args) throws Exception {

		System.out.println(leftSixteenBitMask >>> 16);
	}

	public static int getMask(int bits) {
		int res = 0;
		for (int i = 0; i < bits; i++) {
			res += 1 << i;
		}
		return res;
	}

}
