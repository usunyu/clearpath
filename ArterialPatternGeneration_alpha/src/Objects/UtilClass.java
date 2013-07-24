package Objects;

public class UtilClass {

	public static String getStartTime(int j) {

		switch (j) {
		case 0:
			return "06:00";
		case 1:
			return "06:15";
		case 2:
			return "06:30";
		case 3:
			return "06:45";

		case 4:
			return "07:00";
		case 5:
			return "07:15";
		case 6:
			return "07:30";
		case 7:
			return "07:45";

		case 8:
			return "08:00";
		case 9:
			return "08:15";
		case 10:
			return "08:30";
		case 11:
			return "08:45";

		case 12:
			return "09:00";
		case 13:
			return "09:15";
		case 14:
			return "09:30";
		case 15:
			return "09:45";

		case 16:
			return "10:00";
		case 17:
			return "10:15";
		case 18:
			return "10:30";
		case 19:
			return "10:45";

		case 20:
			return "11:00";
		case 21:
			return "11:15";
		case 22:
			return "11:30";
		case 23:
			return "11:45";

		case 24:
			return "12:00";
		case 25:
			return "12:15";
		case 26:
			return "12:30";
		case 27:
			return "12:45";

		case 28:
			return "13:00";
		case 29:
			return "13:15";
		case 30:
			return "13:30";
		case 31:
			return "13:45";

		case 32:
			return "14:00";
		case 33:
			return "14:15";
		case 34:
			return "14:30";
		case 35:
			return "14:45";

		case 36:
			return "15:00";
		case 37:
			return "15:15";
		case 38:
			return "15:30";
		case 39:
			return "15:45";

		case 40:
			return "16:00";
		case 41:
			return "16:15";
		case 42:
			return "16:30";
		case 43:
			return "16:45";

		case 44:
			return "17:00";
		case 45:
			return "17:15";
		case 46:
			return "17:30";
		case 47:
			return "17:45";

		case 48:
			return "18:00";
		case 49:
			return "18:15";
		case 50:
			return "18:30";
		case 51:
			return "18:45";

		case 52:
			return "19:00";
		case 53:
			return "19:15";
		case 54:
			return "19:30";
		case 55:
			return "19:45";

		case 56:
			return "20:00";
		case 57:
			return "20:15";
		case 58:
			return "20:30";
		case 59:
			return "20:45";

		}
		return null;
	}

	public static String getEndTime(int j) {

		switch (j) {
		case 0:
			return "06:15";
		case 1:
			return "06:30";
		case 2:
			return "06:45";
		case 3:
			return "07:00";
		case 4:
			return "07:15";
		case 5:
			return "07:30";
		case 6:
			return "07:45";
		case 7:
			return "08:00";
		case 8:
			return "08:15";
		case 9:
			return "08:30";
		case 10:
			return "08:45";
		case 11:
			return "09:00";
		case 12:
			return "09:15";
		case 13:
			return "09:30";
		case 14:
			return "09:45";
		case 15:
			return "10:00";
		case 16:
			return "10:15";
		case 17:
			return "10:30";
		case 18:
			return "10:45";
		case 19:
			return "11:00";
		case 20:
			return "11:15";
		case 21:
			return "11:30";
		case 22:
			return "11:45";
		case 23:
			return "12:00";
		case 24:
			return "12:15";
		case 25:
			return "12:30";
		case 26:
			return "12:45";
		case 27:
			return "13:00";
		case 28:
			return "13:15";
		case 29:
			return "13:30";
		case 30:
			return "13:45";
		case 31:
			return "14:00";
		case 32:
			return "14:15";
		case 33:
			return "14:30";
		case 34:
			return "14:45";
		case 35:
			return "15:00";
		case 36:
			return "15:15";
		case 37:
			return "15:30";
		case 38:
			return "15:45";
		case 39:
			return "16:00";
		case 40:
			return "16:15";
		case 41:
			return "16:30";
		case 42:
			return "16:45";
		case 43:
			return "17:00";
		case 44:
			return "17:15";
		case 45:
			return "17:30";
		case 46:
			return "17:45";
		case 47:
			return "18:00";
		case 48:
			return "18:15";
		case 49:
			return "18:30";
		case 50:
			return "18:45";
		case 51:
			return "19:00";
		case 52:
			return "19:15";
		case 53:
			return "19:30";
		case 54:
			return "19:45";
		case 55:
			return "20:00";
		case 56:
			return "20:15";
		case 57:
			return "20:30";
		case 58:
			return "20:45";
		case 59:
			return "21:00";

		}
		return null;
	}
	
	public static String getFullStartTime(int j) {

		switch (j) {
		case 0:
			return "00:00";
		case 1:
			return "00:05";
		case 2:
			return "00:10";
		case 3:
			return "00:15";
		case 4:
			return "00:20";
		case 5:
			return "00:25";

		}
		return null;
	}

	public static int getIndex(String string) {
		int hh = Integer.parseInt(string.substring(0, 2));
		int mi = Integer.parseInt(string.substring(3, 5));

		for (int i = 0; i < 15; i++) {
			if (hh >= (6 + i) && hh < (7 + i) && mi >= 0 && mi < 15)
				return (4 * i);
			if (hh >= (6 + i) && hh < (7 + i) && mi >= 15 && mi < 30)
				return 1 + (4 * i);
			if (hh >= (6 + i) && hh < (7 + i) && mi >= 30 && mi < 45)
				return 2 + (4 * i);
			if (hh >= (6 + i) && hh < (7 + i) && mi >= 45)
				return 3 + (4 * i);
		}

		return -1;
	}
}
