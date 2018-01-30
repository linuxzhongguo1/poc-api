package io.mithrilcoin.api.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class DateUtil {

	/**
	 * 특정날짜 패턴을 다른 패턴으로 변경하는 함수
	 * 
	 * @param strDate
	 *            날짜 형식 문자열
	 * @param sourcePattern
	 *            예시 yyyyMMddHHmmss
	 * @param targetPattern
	 *            예시 yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public String dateStringReplace(String strDate, String sourcePattern, String targetPattern) {
		// yyyy-mm-dd hh:mm:ss
		String date1 = null;
		try {

			// input date format
			SimpleDateFormat df_in = new SimpleDateFormat(sourcePattern);

			// output date format
			SimpleDateFormat df_output = new SimpleDateFormat(targetPattern);
			Date date = df_in.parse(strDate);
			date1 = df_output.format(date);
		} catch (Exception e) {
			System.out.println("Invalid Date: " + e.getMessage());
		}
		if (date1 != null) {
			return date1;
		}

		return strDate;
	}

	/**
	 * Date 형식을 특정 패턴을 가지는 날짜 패턴 문자열로 변경.
	 * 
	 * @param sourceDate
	 * @param pattern
	 * @return
	 */
	public String date2String(Date sourceDate, String pattern) {

		SimpleDateFormat df_in = new SimpleDateFormat(pattern);
		String formatedString = df_in.format(sourceDate);

		return formatedString;
	}

	/**
	 * <p>
	 * Checks if two dates are on the same day ignoring time.
	 * </p>
	 * 
	 * @param date1
	 *            the first date, not altered, not null
	 * @param date2
	 *            the second date, not altered, not null
	 * @return true if they represent the same day
	 * @throws IllegalArgumentException
	 *             if either date is <code>null</code>
	 */
	public boolean isSameDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isSameDay(cal1, cal2);
	}

	/**
	 * <p>
	 * Checks if two calendars represent the same day ignoring time.
	 * </p>
	 * 
	 * @param cal1
	 *            the first calendar, not altered, not null
	 * @param cal2
	 *            the second calendar, not altered, not null
	 * @return true if they represent the same day
	 * @throws IllegalArgumentException
	 *             if either calendar is <code>null</code>
	 */
	public boolean isSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}

	/**
	 * <p>
	 * Checks if a date is today.
	 * </p>
	 * 
	 * @param date
	 *            the date, not altered, not null.
	 * @return true if the date is today.
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 */
	public boolean isToday(Date date) {
		return isSameDay(date, Calendar.getInstance().getTime());
	}

	/**
	 * <p>
	 * Checks if a calendar date is today.
	 * </p>
	 * 
	 * @param cal
	 *            the calendar, not altered, not null
	 * @return true if cal date is today
	 * @throws IllegalArgumentException
	 *             if the calendar is <code>null</code>
	 */
	public boolean isToday(Calendar cal) {
		return isSameDay(cal, Calendar.getInstance());
	}

	/**
	 * <p>
	 * Checks if the first date is before the second date ignoring time.
	 * </p>
	 * 
	 * @param date1
	 *            the first date, not altered, not null
	 * @param date2
	 *            the second date, not altered, not null
	 * @return true if the first date day is before the second date day.
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 */
	public boolean isBeforeDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isBeforeDay(cal1, cal2);
	}

	/**
	 * <p>
	 * Checks if the first calendar date is before the second calendar date ignoring
	 * time.
	 * </p>
	 * 
	 * @param cal1
	 *            the first calendar, not altered, not null.
	 * @param cal2
	 *            the second calendar, not altered, not null.
	 * @return true if cal1 date is before cal2 date ignoring time.
	 * @throws IllegalArgumentException
	 *             if either of the calendars are <code>null</code>
	 */
	public boolean isBeforeDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA))
			return true;
		if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA))
			return false;
		if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR))
			return true;
		if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR))
			return false;
		return cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * <p>
	 * Checks if the first date is after the second date ignoring time.
	 * </p>
	 * 
	 * @param date1
	 *            the first date, not altered, not null
	 * @param date2
	 *            the second date, not altered, not null
	 * @return true if the first date day is after the second date day.
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 */
	public boolean isAfterDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isAfterDay(cal1, cal2);
	}

	/**
	 * <p>
	 * Checks if the first calendar date is after the second calendar date ignoring
	 * time.
	 * </p>
	 * 
	 * @param cal1
	 *            the first calendar, not altered, not null.
	 * @param cal2
	 *            the second calendar, not altered, not null.
	 * @return true if cal1 date is after cal2 date ignoring time.
	 * @throws IllegalArgumentException
	 *             if either of the calendars are <code>null</code>
	 */
	public boolean isAfterDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA))
			return false;
		if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA))
			return true;
		if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR))
			return false;
		if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR))
			return true;
		return cal1.get(Calendar.DAY_OF_YEAR) > cal2.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * <p>
	 * Checks if a date is after today and within a number of days in the future.
	 * </p>
	 * 
	 * @param date
	 *            the date to check, not altered, not null.
	 * @param days
	 *            the number of days.
	 * @return true if the date day is after today and within days in the future .
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 */
	public boolean isWithinDaysFuture(Date date, int days) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return isWithinDaysFuture(cal, days);
	}

	/**
	 * <p>
	 * Checks if a calendar date is after today and within a number of days in the
	 * future.
	 * </p>
	 * 
	 * @param cal
	 *            the calendar, not altered, not null
	 * @param days
	 *            the number of days.
	 * @return true if the calendar date day is after today and within days in the
	 *         future .
	 * @throws IllegalArgumentException
	 *             if the calendar is <code>null</code>
	 */
	public boolean isWithinDaysFuture(Calendar cal, int days) {
		if (cal == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar today = Calendar.getInstance();
		Calendar future = Calendar.getInstance();
		future.add(Calendar.DAY_OF_YEAR, days);
		return (isAfterDay(cal, today) && !isAfterDay(cal, future));
	}

	/** Returns the given date with the time set to the start of the day. */
	public Date getStart(Date date) {
		return clearTime(date);
	}

	/** Returns the given date with the time values cleared. */
	public Date clearTime(Date date) {
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/**
	 * Determines whether or not a date has any time values (hour, minute, seconds
	 * or millisecondsReturns the given date with the time values cleared.
	 */

	/**
	 * Determines whether or not a date has any time values.
	 * 
	 * @param date
	 *            The date.
	 * @return true iff the date is not null and any of the date's hour, minute,
	 *         seconds or millisecond values are greater than zero.
	 */
	public boolean hasTime(Date date) {
		if (date == null) {
			return false;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		if (c.get(Calendar.HOUR_OF_DAY) > 0) {
			return true;
		}
		if (c.get(Calendar.MINUTE) > 0) {
			return true;
		}
		if (c.get(Calendar.SECOND) > 0) {
			return true;
		}
		if (c.get(Calendar.MILLISECOND) > 0) {
			return true;
		}
		return false;
	}

	/** Returns the given date with time set to the end of the day */
	public Date getEnd(Date date) {
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return c.getTime();
	}

	/**
	 * Returns the maximum of two dates. A null date is treated as being less than
	 * any non-null date.
	 */
	public Date max(Date d1, Date d2) {
		if (d1 == null && d2 == null)
			return null;
		if (d1 == null)
			return d2;
		if (d2 == null)
			return d1;
		return (d1.after(d2)) ? d1 : d2;
	}

	/**
	 * Returns the minimum of two dates. A null date is treated as being greater
	 * than any non-null date.
	 */
	public Date min(Date d1, Date d2) {
		if (d1 == null && d2 == null)
			return null;
		if (d1 == null)
			return d2;
		if (d2 == null)
			return d1;
		return (d1.before(d2)) ? d1 : d2;
	}

	/** The maximum date possible. */
	public Date MAX_DATE = new Date(Long.MAX_VALUE);

	public Date string2Date(String playdate, String pattern) {

		try {
			return new SimpleDateFormat(pattern).parse(playdate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	/***
	 * 현재 시간 스트링 반환 UTC 기준 
	 * @return
	 */
	public String getUTCNow() {
		ZonedDateTime date = ZonedDateTime.now(ZoneOffset.UTC);
		return String.valueOf(	date.toInstant().toEpochMilli());
	}
//	/***
//	 * 현재 시간 스트링 반환 UTC 기준 
//	 * @return
//	 */
//	public String getLocaleNow() {
//		LocalDateTime date = LocalDateTime.now();
//		ZoneOffset offset = ZoneOffset.
//		return String.valueOf(date.toEpochSecond(ZoneOffset.systemDefault()));
//	}
	
	/**
	 * UTC 시간을 현재 locale 시간으로 변환 
	 * @param utcdateString
	 * @return
	 */
	public String string2LocaleDateString(String utcdateString)
	{
		long t = Long.parseLong(utcdateString);
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
		
		return simpleDate.format(t);
	}
	/**
	 * locale 시간 문자열을 UTC 시간 문자열로 변경
	 * @param localeString yyyy-MM-dd hh:mm:ss
	 * @return
	 * @throws ParseException 
	 */
	public String localeString2UTCString(String localeString) throws ParseException
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",Locale.getDefault());
		Date date = dateFormat.parse(localeString);
		long unixTime = (long)date.getTime();
		
		return String.valueOf(unixTime);
	}

	public boolean isToday(String utcTime)
	{
		String dateString = string2LocaleDateString(utcTime);
		return isToday(string2Date(dateString, "yyyy-MM-dd hh:mm:ss"));
	}
	
	
	
}
