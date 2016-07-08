package com.guanyin.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class Const {

	public static final boolean debug = true;
	public static boolean firstInitList = true;
	public static final String serverurl = "http://139.129.19.236/xauto/app_interface/index.php";

	public static final String apiRouteLogin = "member/member/login";
	public static final String apiRouteGetcode = "member/member/getVerifyCode";
	public static final String apiRouteRegister = "member/member/registMember";
	public static final String apiRouteForget = "member/member/forgetPassword";
	public static final String apiRouteValidate = "member/member/validateMemberName";
	public static final String apiRouteReset = "member/member/resetPassword";
	public static final String apiGetStation = "station/station/getstation";

	private static double EARTH_RADIUS = 6378.137;

	public static final void showToast(Context context, String contents) {
		if (debug) {
			Toast.makeText(context, contents, Toast.LENGTH_SHORT).show();
		}

	}

	public static void log(String TAG, String message) {
		Log.e(TAG, message);
	}

	/*
	 * MD5加密
	 */
	public static String getMD5Str(String str) {
		MessageDigest messageDigest = null;

		try {
			messageDigest = MessageDigest.getInstance("MD5");

			messageDigest.reset();

			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
				md5StrBuff.append("0").append(
						Integer.toHexString(0xFF & byteArray[i]));
			} else {
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
			}
		}
		// // 16位加密，从第9位到25位
		// return md5StrBuff.substring(8, 24).toString()
		// .toUpperCase(Locale.CHINESE);
		// 取全部32位
		return md5StrBuff.toString();
		// toUpperCase(Locale.CHINESE);
	}

	/**
	 * 打开网页
	 * 
	 * @param context
	 * @param url
	 *            网页地址
	 */
	public static void openUrl(Context context, String url) {
		if (!isUrl(url)) {
			return;
		}
		Uri uri = null;
		if (url.startsWith("http://")) {
			uri = Uri.parse(url);
		} else {
			uri = Uri.parse("http://" + url);
		}
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(intent);
	}

	/**
	 * 判读是否为合法的url
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isUrl(String str) {
		String regex = "^(http://|https://)?((?:[A-Za-z0-9]+-[A-Za-z0-9]+|[A-Za-z0-9]+).)+([A-Za-z]+)[/?:]?.*$";
		Pattern patt = Pattern.compile(regex);
		Matcher matcher = patt.matcher(str);
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
	}

	public static double calculDistance(String latitude_num,
			String longitude_num) {
		// 计算p1、p2两点之间的直线距离，单位：米
		LatLng p1 = new LatLng(MyApplication.getInstance().latitude,
				MyApplication.getInstance().lontitude);
		LatLng p2 = new LatLng(Double.parseDouble(latitude_num),
				Double.parseDouble(longitude_num));

		return DistanceUtil.getDistance(p1, p2);

	}

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	public static double GetDistance(double lat1, double lng1, double lat2,
			double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

}
