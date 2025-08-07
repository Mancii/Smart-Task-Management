package com.task.utils;

import java.util.List;
import java.util.Random;

public class Utils {

	
	public static String getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		int randomNumber = r.nextInt((max - min) + 1) + min;
		String randomString = String.valueOf(randomNumber);
		if (randomString.startsWith("0")) {
			do {
				randomNumber = r.nextInt((max - min) + 1) + min;
				randomString = String.valueOf(randomNumber);
			} while (randomString.startsWith("0"));
		}
		return randomString;
	}
	public static boolean isNotEmpty(String obj) {
		return obj != null && obj.length() != 0;
	}

	public static <T> boolean isNotEmpty(List<T> obj) {
		return obj != null && !obj.isEmpty();
	}

	public static boolean isNotEmpty(Object obj) {
		return obj != null;
	}

}
