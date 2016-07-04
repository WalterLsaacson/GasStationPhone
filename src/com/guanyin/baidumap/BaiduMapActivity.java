package com.guanyin.baidumap;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.guanyin.activity.R;
import com.guanyin.data.StationInfo;
import com.guanyin.userface.ViewPagerActivity;
import com.guanyin.utils.Const;
import com.guanyin.utils.MyApplication;

public class BaiduMapActivity extends Activity implements OnClickListener {

	// 辅助字符串
	private MyApplication app;
	private Context context = BaiduMapActivity.this;
	// 地图组件声明
	private MapView mMapView = null;
	private BaiduMap mBaiduMap;
	// 地图上的按钮
	private Button btn_loc;
	// 设置加油站的辅助信息
	private double latitude_num;
	private double longitude_num;
	private Bundle bundle;
	// 获取到的加油站信息
	public ArrayList<StationInfo> stationInfo;
	// list view
	private ListView listview_gasstation;
	private MyStationsAdapter myAdapter;
	// 初始化marker的四种图片对象
	private BitmapDescriptor bitmapDescriptor0;
	private BitmapDescriptor bitmapDescriptor1;
	private BitmapDescriptor bitmapDescriptor02;
	private BitmapDescriptor bitmapDescriptor12;

	// 导航

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (app == null) {
			app = MyApplication.getInstance();
		}
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		SDKInitializer.initialize(app);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_baidu_map);
		// 初始化数据
		initDatas();
		// 初始化视图
		initViews();
		// 设置marker的点击事件
		setClickMap();
		setMarkers();

	}

	private void initDatas() {
		stationInfo = ViewPagerActivity.stations;
		// 在这里将地图上的图片初始化出来
		bitmapDescriptor0 = BitmapDescriptorFactory
				.fromResource(R.drawable.oil_pos0);// 未合作，未选中
		bitmapDescriptor1 = BitmapDescriptorFactory
				.fromResource(R.drawable.oil_pos1);// 未合作，已选中
		bitmapDescriptor02 = BitmapDescriptorFactory
				.fromResource(R.drawable.oil_pos02);// 合作，未选中
		bitmapDescriptor12 = BitmapDescriptorFactory
				.fromResource(R.drawable.oil_pos12);// 合作，已选中
	}

	private void initViews() {

		// 这是定位到当前位置的按钮
		btn_loc = (Button) findViewById(R.id.btn_catch_station);
		btn_loc.setOnClickListener(this);

		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		// 设置logo的位置,枚举类型的六个数据
		mMapView.setLogoPosition(LogoPosition.logoPostionRightTop);
		// 设置地图的类型
		mBaiduMap = mMapView.getMap();
		// 普通地图
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		// 设置缩放级别
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(14));
		// 设置是否显示比例尺控件
		mMapView.showScaleControl(false);
		// 设置是否显示缩放控制控件 + -
		mMapView.showZoomControls(false);

		// 显示当前位置
		setLoca();

		// 底部list view的初始化
		listview_gasstation = (ListView) findViewById(R.id.listview_gasstation);
		// 初始化list view
		initListView();
		listview_gasstation.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (stationInfo.get(position).isSelected) {
					return;
				}
				// 设置item被点击之后相应的marker也被选中
				stationInfo.get(position).isSelected = true;
				// 清除上一个marker的点击状态
				stationInfo.get(0).isSelected = false;
				// 相当于是刷新了markers
				setMarkers();
				// 设置list view的item被点击之后将这个item置顶
				changeList(position);
			}
		});

	}

	public void setLoca() {
		// 展示实时位置信息
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 构造定位数据
		MyLocationData locData = new MyLocationData.Builder()
				.accuracy(app.radius)
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(100).latitude(app.latitude).longitude(app.lontitude)
				.build();
		// 设置定位数据
		mBaiduMap.setMyLocationData(locData);
		BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
				.fromResource(R.drawable.b_poi);
		MyLocationConfiguration config = new MyLocationConfiguration(
				com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING,
				true, mCurrentMarker);
		// LocationMode mCurrentMode =
		// // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
		// MyLocationConfiguration config = new MyLocationConfiguration(
		// ,null, true, mCurrentMarker);
		mBaiduMap.setMyLocationConfigeration(config);
		setMarkers();
	}

	private void initListView() {
		for (int i = 0; i < stationInfo.size(); i++) {
			for (int j = i + 1; j < stationInfo.size(); j++) {
				if (Math.floor(stationInfo.get(i).calculDistance()
						- stationInfo.get(j).calculDistance()) > 1.0) {
					StationInfo stemp = new StationInfo();
					stemp = stationInfo.get(i);
					stationInfo.set(i, stationInfo.get(j));
					stationInfo.set(j, stemp);
				}
			}
		}
		stationInfo.get(0).isSelected = true;
		listview_gasstation.setBackgroundColor(getResources().getColor(
				R.color.white));
		myAdapter = new MyStationsAdapter(stationInfo, this);
		listview_gasstation.setAdapter(myAdapter);
	}

	private void changeList(int index) {
		// 设置list view的第一个item
		StationInfo stemp = new StationInfo();
		stemp = stationInfo.get(0);
		stationInfo.set(0, stationInfo.get(index));
		stationInfo.set(index, stemp);

		for (int i = 1; i < stationInfo.size(); i++) {
			for (int j = i + 1; j < stationInfo.size(); j++) {
				if (Math.floor(stationInfo.get(i).calculDistance()
						- stationInfo.get(j).calculDistance()) > 1.0) {
					StationInfo stemp1 = new StationInfo();
					stemp1 = stationInfo.get(i);
					stationInfo.set(i, stationInfo.get(j));
					stationInfo.set(j, stemp1);
				}
			}
		}
		myAdapter = new MyStationsAdapter(stationInfo, this);
		listview_gasstation.setAdapter(myAdapter);
	}

	private void setClickMap() {
		// 地图上marker的点击事件
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				int index = -1;
				for (int i = 0; i < stationInfo.size(); i++) {
					if (stationInfo.get(i).station_id == marker.getExtraInfo()
							.getString("id")) {
						index = i;
					}
				}
				if (stationInfo.get(index).isSelected) {
					return false;
				}
				stationInfo.get(index).isSelected = true;
				stationInfo.get(0).isSelected = false;
				changeList(index);
				setMarkers();

				Const.showToast(context, marker.getExtraInfo()
						.getString("name"));
				return true;
			}

		});
		mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {

			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {
				btn_loc.setVisibility(View.VISIBLE);
			}

			@Override
			public void onMapStatusChangeFinish(MapStatus arg0) {

			}

			@Override
			public void onMapStatusChange(MapStatus arg0) {

			}
		});

	}

	private void setMarkers() {

		// 根据数据更新或者初始化markers
		mBaiduMap.clear();
		LatLng pointLatLng;
		OverlayOptions options = null;
		for (int i = 0; i < stationInfo.size(); i++) {
			longitude_num = Double
					.parseDouble(stationInfo.get(i).longitude_num);
			latitude_num = Double.parseDouble(stationInfo.get(i).latitude_num);
			// 设置地图标注，给指定位置
			// 定义一个坐标点
			pointLatLng = new LatLng(latitude_num, longitude_num);
			// 构建Marker图标
			if (!stationInfo.get(i).isSelected) {
				if (stationInfo.get(i).is_cooperate.equals("0")) {
					options = new MarkerOptions().position(pointLatLng)
							.icon(bitmapDescriptor0)
							.title(stationInfo.get(i).station_name);
					// 在地图上添加marker，并显示
					// 在这里增加额外的信息,.setExtraInfo(),参数是bundle
					bundle = new Bundle();
					bundle.putString("name", stationInfo.get(i).station_name);
					bundle.putString("is_cooperate",
							stationInfo.get(i).is_cooperate);
					bundle.putString("id", stationInfo.get(i).station_id);
					bundle.putDouble("lati", latitude_num);
					bundle.putDouble("lon", longitude_num);
					mBaiduMap.addOverlay(options).setExtraInfo(bundle);
				} else {
					options = new MarkerOptions().position(pointLatLng)
							.icon(bitmapDescriptor02)
							.title(stationInfo.get(i).station_name);
					// 在地图上添加marker，并显示
					// 在这里增加额外的信息,.setExtraInfo(),参数是bundle
					bundle = new Bundle();
					bundle.putString("name", stationInfo.get(i).station_name);
					bundle.putString("is_cooperate",
							stationInfo.get(i).is_cooperate);
					bundle.putString("id", stationInfo.get(i).station_id);
					bundle.putDouble("lati", latitude_num);
					bundle.putDouble("lon", longitude_num);
					mBaiduMap.addOverlay(options).setExtraInfo(bundle);
				}
			}
		}
		for (int i = 0; i < stationInfo.size(); i++) {
			longitude_num = Double
					.parseDouble(stationInfo.get(i).longitude_num);
			latitude_num = Double.parseDouble(stationInfo.get(i).latitude_num);
			// 设置地图标注，给指定位置
			// 定义一个坐标点
			pointLatLng = new LatLng(latitude_num, longitude_num);
			// 构建Marker图标
			if (stationInfo.get(i).isSelected) {
				// 将地图移到当前位置
				MapStatusUpdate u = MapStatusUpdateFactory
						.newLatLng(pointLatLng);
				mBaiduMap.setMapStatus(u);

				if (stationInfo.get(i).is_cooperate.equals("0")) {
					options = new MarkerOptions().position(pointLatLng)
							.icon(bitmapDescriptor1)
							.title(stationInfo.get(i).station_name);
					// 在地图上添加marker，并显示
					// 在这里增加额外的信息,.setExtraInfo(),参数是bundle
					bundle = new Bundle();
					bundle.putString("name", stationInfo.get(i).station_name);
					bundle.putString("is_cooperate",
							stationInfo.get(i).is_cooperate);
					bundle.putString("id", stationInfo.get(i).station_id);
					bundle.putDouble("lati", latitude_num);
					bundle.putDouble("lon", longitude_num);
					mBaiduMap.addOverlay(options).setExtraInfo(bundle);
					break;
				} else {
					options = new MarkerOptions().position(pointLatLng)
							.icon(bitmapDescriptor12)
							.title(stationInfo.get(i).station_name);
					// 在地图上添加marker，并显示
					// 在这里增加额外的信息,.setExtraInfo(),参数是bundle
					bundle = new Bundle();
					bundle.putString("name", stationInfo.get(i).station_name);
					bundle.putString("is_cooperate",
							stationInfo.get(i).is_cooperate);
					bundle.putString("id", stationInfo.get(i).station_id);
					bundle.putDouble("lati", latitude_num);
					bundle.putDouble("lon", longitude_num);
					mBaiduMap.addOverlay(options).setExtraInfo(bundle);
					break;
				}
			}

		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_catch_station:
			// 显示当前位置
			setLoca();
			btn_loc.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

}
