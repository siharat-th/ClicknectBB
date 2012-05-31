package com.jimmysoftware.every2;

public class LocationEncryption {
	//exclusiver = "com.jimmysoftware.locationencription"
	//exclusive in long format = 0xdca8c8570414e84cL
	
	private final static long exclusiver = 0xdca8c8570414e84cL;
    private final static long adder = 0xBL;
    private final static int radix = Character.MAX_RADIX;
    
	public static String encrypt(double doubleValue){
		long longValue = Double.doubleToLongBits(doubleValue + adder);
		long longValueEncrypt = longValue ^ exclusiver;
		String stringValueEncrypt = Long.toString(longValueEncrypt, radix);
		return stringValueEncrypt;
	}
	
	public static double decrypt(String encrypt){
		long longValueEncrypt = Long.parseLong(encrypt, radix);  //Long.valueOf(encrypt, radix);
		long longValue = (longValueEncrypt ^ exclusiver);
		double doubleValue = Double.longBitsToDouble(longValue) - adder;
		return doubleValue;
	}
}
