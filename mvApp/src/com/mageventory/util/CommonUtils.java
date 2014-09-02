/* Copyright (c) 2014 mVentory Ltd. (http://mventory.com)
 * 
* License       http://creativecommons.org/licenses/by-nc-nd/4.0/
* 
* NonCommercial — You may not use the material for commercial purposes. 
* NoDerivatives — If you compile, transform, or build upon the material,
* you may not distribute the modified material. 
* Attribution — You must give appropriate credit, provide a link to the license,
* and indicate if changes were made. You may do so in any reasonable manner, 
* but not in any way that suggests the licensor endorses you or your use. 
*/

package com.mageventory.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.mageventory.BuildConfig;
import com.mageventory.MyApplication;
import com.mageventory.R;

/**
 * Contains various common utils methods
 * 
 * @author Eugene Popovich
 */
public class CommonUtils {
    public static final String TAG = CommonUtils.class.getSimpleName();
    /**
     * Decimal only format with no fraction digits
     */
    private static NumberFormat decimalFormat;
    static {
        decimalFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
    }
    /**
     * Decimal format with minimum 1 fractional digit and maximum 3
     */
    private static NumberFormat fractionalFormatWithRoundUpAndMinimum1FractionalDigit;
    static {
        fractionalFormatWithRoundUpAndMinimum1FractionalDigit = NumberFormat
                .getNumberInstance(Locale.ENGLISH);
        fractionalFormatWithRoundUpAndMinimum1FractionalDigit.setGroupingUsed(false);
        fractionalFormatWithRoundUpAndMinimum1FractionalDigit.setMinimumFractionDigits(1);
        fractionalFormatWithRoundUpAndMinimum1FractionalDigit.setMaximumFractionDigits(3);
        fractionalFormatWithRoundUpAndMinimum1FractionalDigit.setRoundingMode(RoundingMode.HALF_UP);
    }
    /**
     * Decimal format with 1 fractional digit 
     */
    private static NumberFormat fractionalFormatWithRoundUpAnd1FractionalDigit;
    static {
        fractionalFormatWithRoundUpAnd1FractionalDigit = NumberFormat
                .getNumberInstance(Locale.ENGLISH);
        fractionalFormatWithRoundUpAnd1FractionalDigit.setGroupingUsed(false);
        fractionalFormatWithRoundUpAnd1FractionalDigit.setMinimumFractionDigits(1);
        fractionalFormatWithRoundUpAnd1FractionalDigit.setMaximumFractionDigits(1);
        fractionalFormatWithRoundUpAnd1FractionalDigit.setRoundingMode(RoundingMode.HALF_UP);
    }
    /**
     * Default decimal format
     */
    private static NumberFormat fractionalFormat;
    static {
        fractionalFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
        fractionalFormat.setGroupingUsed(false);
    }

    /**
     * The default date time format
     */
    final static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * The default date format
     */
    final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Get string resource by id
     * 
     * @param resourceId
     * @return
     */
    public static String getStringResource(int resourceId) {
        return MyApplication.getContext().getString(resourceId);
    }

    /**
     * Get string resource by id with parameters
     * 
     * @param resourceId
     * @param args
     * @return
     */
    public static String getStringResource(int resourceId, Object... args) {
        return MyApplication.getContext().getString(resourceId, args);
    }

    /**
     * Get color resource by id
     * 
     * @param resourceId
     * @return
     */
    public static int getColorResource(int resourceId) {
        return MyApplication.getContext().getResources().getColor(resourceId);
    }

    /**
     * Write message to the debug log
     * 
     * @param TAG
     * @param message
     * @param params
     */
    public static void debug(String TAG, String message, Object... params) {
        debug(TAG, false, message, params);
    }

    /**
     * Write message to the debug log
     * 
     * @param TAG
     * @param writeToLog
     * @param message
     * @param params
     */
    public static void debug(String TAG, boolean writeToLog, String message, Object... params) {
        try {
            if (BuildConfig.DEBUG) {
                if (params == null || params.length == 0) {
                    Log.d(TAG, message);
                    if (writeToLog) {
                        com.mageventory.util.Log.d(TAG, message);
                    }
                } else {
                    Log.d(TAG, format(message, params));
                    if (writeToLog) {
                        com.mageventory.util.Log.d(TAG, format(message, params));
                    }
                }
            }
        } catch (Exception ex) {
            GuiUtils.noAlertError(TAG, ex);
        }
    }

    /**
     * Write message to the verbose log
     * 
     * @param TAG
     * @param message
     * @param params
     */
    public static void verbose(String TAG, String message, Object... params) {
        try {
            if (BuildConfig.DEBUG) {
                if (params == null || params.length == 0) {
                    Log.v(TAG, message);
                } else {
                    Log.v(TAG, format(message, params));
                }
            }
        } catch (Exception ex) {
            GuiUtils.noAlertError(TAG, ex);
        }
    }

    /**
     * Write message to the error log
     * 
     * @param TAG
     * @param message
     */
    public static void error(String TAG, String message) {
        error(TAG, message, null);
    }

    /**
     * Write exception to the error log
     * 
     * @param TAG
     * @param tr
     */
    public static void error(String TAG, Throwable tr) {
        error(TAG, null, tr);
    }

    /**
     * Write message to the error log
     * 
     * @param TAG
     * @param message
     * @param tr
     */
    public static void error(String TAG, String message, Throwable tr) {
        Log.e(TAG, message, tr);
        if (!TextUtils.isEmpty(message)) {
            com.mageventory.util.Log.e(TAG, message);
        }
        if (tr != null) {
            com.mageventory.util.Log.logCaughtException(tr);
            TrackerUtils.trackThrowable(tr);
        }
    }

    /**
     * Format string with params
     * 
     * @param message
     * @param params
     * @return
     */
    public static String format(String message, Object... params) {
        try {
            return String.format(Locale.ENGLISH, message, params);
        } catch (Exception ex) {
            GuiUtils.noAlertError(TAG, ex);
        }
        return null;
    }

    /**
     * Format the number with no fraction digits information
     * 
     * @param number
     * @return
     */
    public static String formatDecimalOnlyWithRoundUp(Number number) {
        return decimalFormat.format(number);
    }

    /**
     * Format the number keeping fractional digits information. Minimum is 1
     * fractional digita and maximum is 3
     * 
     * @param number
     * @return
     */
    public static String formatNumberWithFractionWithRoundUp(Number number) {
        return fractionalFormatWithRoundUpAndMinimum1FractionalDigit.format(number);
    }
    
    /**
     * Format the number keeping 1 fractional digit.
     * 
     * @param number
     * @return
     */
    public static String formatNumberWithFractionWithRoundUp1(Number number) {
        return fractionalFormatWithRoundUpAnd1FractionalDigit.format(number);
    }
    
    /**
     * Format the number keeping fractional digits information.
     * 
     * @param number
     * @return
     */
    public static String formatNumber(Number number) {
        return fractionalFormat.format(number);
    }

    /**
     * Format the number keeping fractional digits information.
     * 
     * @param number
     * @return
     */
    public static String formatNumberIfNotNull(Number number) {
        return formatNumberIfNotNull(number, null);
    }

    /**
     * Format the number keeping fractional digits information.
     * 
     * @param number
     * @param defaultValue
     * @return
     */
    public static String formatNumberIfNotNull(Number number, String defaultValue) {
        return number == null ? defaultValue : formatNumber(number);
    }

    /**
     * Parse number
     * 
     * @param str the string to parse
     * @return null in case of ParseException occurs or null parameter passed
     */
    public static Double parseNumber(String str) {
        return parseNumber(str, null);
    }

    /**
     * Parse number
     * 
     * @param str the string to parse
     * @param defaultValue the defaultValue to return if parse problem occurs
     * @return defaultValue in case of ParseException occurs or null parameter
     *         passed
     */
    public static Double parseNumber(String str, Double defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return fractionalFormat.parse(str).doubleValue();
        } catch (ParseException ex) {
            GuiUtils.noAlertError(TAG, ex);
        }
        return defaultValue;
    }

    /**
     * Format the price keeping fractional digits information and appending $ at
     * the beginning.
     * 
     * @param price
     * @return
     */
    public static String formatPrice(Number price) {
        return "$" + fractionalFormat.format(price);
    }

    /**
     * Parse date/time
     * 
     * @param str
     * @return null in case of ParseException occurs  or null parameter passed
     */
    public static Date parseDateTime(String str) {
        if (str == null) {
            return null;
        }
        try {
            return dateTimeFormat.parse(str);
        } catch (ParseException ex) {
            GuiUtils.noAlertError(TAG, ex);
        }
        return null;
    }

    /**
     * Format the date time with the default formatter
     * 
     * @param date
     * @return
     */
    public static String formatDateTime(Date date) {
        return dateTimeFormat.format(date);
    }

    /**
     * Format the date time with the default formatter if date is not null
     * 
     * @param date
     * @return formatted string contains date time information. In case date is
     *         null returns null
     */
    public static String formatDateTimeIfNotNull(Date date) {
        return formatDateTimeIfNotNull(date, null);
    }

    /**
     * Format the date time with the default formatter if date is not null
     * 
     * @param date
     * @param defaultValue to return in case date is null
     * @return
     */
    public static String formatDateTimeIfNotNull(Date date, String defaultValue) {
        return date == null ? defaultValue : formatDateTime(date);
    }

    /**
     * Parse date
     * 
     * @param str
     * @return null in case of ParseException occurs or null parameter passed
     */
    public static Date parseDate(String str) {
        if (str == null) {
            return null;
        }
        try {
            return dateFormat.parse(str);
        } catch (ParseException ex) {
            GuiUtils.noAlertError(TAG, ex);
        }
        return null;
    }

    /**
     * Format the date with the default formatter if date is not null
     * 
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    /**
     * Format the date with the default formatter if date is not null
     * 
     * @param date
     * @return formatted string contains date information. In case date is null
     *         returns null
     */
    public static String formatDateIfNotNull(Date date) {
        return formatDateIfNotNull(date, null);
    }

    /**
     * Format the date with the default formatter if date is not null
     * 
     * @param date
     * @param defaultValue to return in case date parameter is null
     * @return
     */
    public static String formatDateIfNotNull(Date date, String defaultValue) {
        return date == null ? defaultValue : formatDate(date);
    }

    /**
     * Check whether the device is connected to any network
     * 
     * @return true if device is connected to any network, otherwise return
     *         false
     */
    public static boolean isOnline() {
        return isOnline(MyApplication.getContext());
    }

    /**
     * Check whether the device is connected to any network
     * 
     * @param context
     * @return true if device is connected to any network, otherwise return
     *         false
     */
    public static boolean isOnline(Context context) {
        boolean result = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                result = true;
            }
        } catch (Exception ex) {
            GuiUtils.noAlertError(TAG, "Error", ex);
        }
        return result;
    }

    /**
     * Check whether the device has enabled connectivity services
     * 
     * @return true if device has enabled any network (independently on
     *         connection state), otherwise returns false
     */
    public static boolean isInternetEnabled() {
        boolean result = false;
        try {
            Context context = MyApplication.getContext();
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            result = wifiManager != null && wifiManager.isWifiEnabled();
            if (!result) {
                TelephonyManager telephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null
                        && telephonyManager.getDataState() != TelephonyManager.DATA_DISCONNECTED) {
                    result = true;
                }
            }
        } catch (Exception ex) {
            GuiUtils.noAlertError(TAG, ex);
        }
        return result;
    }

    /**
     * Checks whether network connection is available. Otherwise shows warning
     * message
     * 
     * @return
     */
    public static boolean checkOnline() {
        return checkOnline(false);
    }

    /**
     * Checks whether network connection is available. Otherwise shows warning
     * message
     * 
     * @param silent whether or not to do not show message in case check failure
     * @return
     */
    public static boolean checkOnline(boolean silent) {
        boolean result = isOnline(MyApplication.getContext());
        if (!result && !silent) {
            GuiUtils.alert(R.string.no_internet_access);
        }
        return result;
    }

    public static boolean checkLoggedInAndOnline(boolean b) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * Checks whether the running platform version is 4.4 or higher
     * 
     * @return
     */
    public static boolean isKitKatOrHigher() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT;
    }

    /**
     * Checks whether the running platform version is 4.1 or higher
     * 
     * @return
     */
    public static boolean isJellyBeanOrHigher() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * Checks whether the running platform version is 3.0 or higher
     * 
     * @return
     */
    public static boolean isHoneyCombOrHigher() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Returns possible external sd card path. Solution taken from here
     * http://stackoverflow.com/a/13648873/527759
     * 
     * @return
     */
    public static Set<String> getExternalMounts() {
        final Set<String> out = new HashSet<String>();
        try {
            String reg = ".*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
            StringBuilder sb = new StringBuilder();
            try {
                final Process process = new ProcessBuilder().command("mount")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    sb.append(new String(buffer));
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            // parse output
            final String[] lines = sb.toString().split("\n");
            for (String line : lines) {
                if (!line.toLowerCase(Locale.ENGLISH).contains("asec")) {
                    if (line.matches(reg)) {
                        String[] parts = line.split(" ");
                        for (String part : parts) {
                            if (part.startsWith("/"))
                                if (!part.toLowerCase(Locale.ENGLISH).contains("vold")) {
                                    CommonUtils.debug(TAG, "Found path: " + part);
                                    out.add(part);
                                }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            error(TAG, null, ex);
        }
        return out;
    }

    /**
     * Convert device independent pixels value to the regular pixels value. The
     * result value is based on device density
     * 
     * @param dipValue
     * @return
     */
    public static float dipToPixels(float dipValue) {
        DisplayMetrics metrics = MyApplication.getContext().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    /**
     * Load the assets resource as String data
     * 
     * @param path the path within assets folder
     * @return
     * @throws IOException
     */
    public static String loadAssetAsString(String path) throws IOException {
        return WebUtils.convertStreamToString(MyApplication.getContext().getAssets().open(path));
    }
}
