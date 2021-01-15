package ch.idsia.crema.search.impl;

public class Utils {
	;

	public static int tryParse(Object string, int fallback) {
		if (string == null) {
			return fallback;
		} else if (string instanceof Number) {
			return ((Number) string).intValue();
		} else {
			try {
				return Integer.parseInt(string.toString());
			} catch (NumberFormatException nfe) {
				return fallback;
			}
		}
	}

	public static long tryParse(Object string, long fallback) {
		if (string == null) {
			return fallback;
		} else if (string instanceof Number) {
			return ((Number) string).longValue();
		} else {
			try {
				return Long.parseLong(string.toString());
			} catch (NumberFormatException nfe) {
				return fallback;
			}
		}
	}

	public static double tryParse(Object string, double fallback) {
		if (string == null) {
			return fallback;
		} else if (string instanceof Number) {
			return ((Number) string).doubleValue();
		} else {
			try {
				return Double.parseDouble(string.toString());
			} catch (NumberFormatException nfe) {
				return fallback;
			}
		}
	}

	public static float tryParse(Object string, float fallback) {
		if (string == null) {
			return fallback;
		} else if (string instanceof Number) {
			return ((Number) string).floatValue();
		} else {
			try {
				return Float.parseFloat(string.toString());
			} catch (NumberFormatException nfe) {
				return fallback;
			}
		}
	}

}
