package com.guanyin.data;

import java.util.ArrayList;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.guanyin.utils.MyApplication;

public class StationInfo {

	public String station_id;
	public String station_name;
	public String address;
	public String latitude_num;
	public String longitude_num;
	public String is_cooperate = "0";

	public double distance;

	public ArrayList<Gas> gasList;

	public boolean isSelected = false;

	public double calculDistance() {
		// 计算p1、p2两点之间的直线距离，单位：米
		LatLng p1 = new LatLng(MyApplication.getInstance().latitude,
				MyApplication.getInstance().lontitude);
		LatLng p2 = new LatLng(Double.parseDouble(latitude_num),
				Double.parseDouble(longitude_num));

		return DistanceUtil.getDistance(p1, p2);

	}
	// public boolean point = false;

}
