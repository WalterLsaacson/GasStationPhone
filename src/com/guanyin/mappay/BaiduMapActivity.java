package com.guanyin.mappay;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;
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
	public static List<Activity> activityList = new LinkedList<Activity>();

	private static final String APP_FOLDER_NAME = "Navi";

	private String mSDCardPath = null;
	public static final String ROUTE_PLAN_NODE = "routePlanNode";
	public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
	public static final String RESET_END_NODE = "resetEndNode";
	public static final String VOID_MODE = "voidMode";

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

		activityList.add(this);

		BNOuterLogUtil.setLogSwitcher(true);

		if (initDirs()) {
			initNavi();
		}

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
				.fromResource(R.drawable.map_poi);
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

	/**
	 * 启动导航，默认路线规划规则（推荐模式：NE_RoutePlan_Mode.ROUTE_PLAN_MOD_RECOMMEND），供外部调用一键导航
	 * 
	 * @param desLat
	 *            目的地纬度
	 * @param desLon
	 *            目的地经度
	 * @param destName
	 *            目的地名称
	 */
	public void startNavi(final double desLat, final double desLon,
			final String destName) {
		if (BaiduNaviManager.isNaviInited()) {
			routeplanToNavi(CoordinateType.BD09LL, desLat, desLon, destName);
		}
	}

	private boolean initDirs() {
		mSDCardPath = getSdcardDir();
		if (mSDCardPath == null) {
			return false;
		}
		File f = new File(mSDCardPath, APP_FOLDER_NAME);
		if (!f.exists()) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	String authinfo = null;

	/**
	 * 内部TTS播报状态回传handler
	 */
	private Handler ttsHandler = new Handler() {
		public void handleMessage(Message msg) {
			int type = msg.what;
			switch (type) {
			case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
				showToastMsg("Handler : TTS play start");
				break;
			}
			case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
				showToastMsg("Handler : TTS play end");
				break;
			}
			default:
				break;
			}
		}
	};

	/**
	 * 内部TTS播报状态回调接口
	 */
	private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

		@Override
		public void playEnd() {
			// showToastMsg("TTSPlayStateListener : TTS play end");
		}

		@Override
		public void playStart() {
			// showToastMsg("TTSPlayStateListener : TTS play start");
		}
	};

	// 重写showToast方法
	public void showToastMsg(final String msg) {
		BaiduMapActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(BaiduMapActivity.this, msg, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	// 初始化语音导航
	private void initNavi() {

		BNOuterTTSPlayerCallback ttsCallback = null;

		BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME,
				new NaviInitListener() {
					@Override
					public void onAuthResult(int status, String msg) {
						if (0 == status) {
							authinfo = "key校验成功!";
						} else {
							authinfo = "key校验失败, " + msg;
						}
						BaiduMapActivity.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(BaiduMapActivity.this, authinfo,
										Toast.LENGTH_LONG).show();
							}
						});
					}

					public void initSuccess() {
						Toast.makeText(BaiduMapActivity.this, "百度导航引擎初始化成功",
								Toast.LENGTH_SHORT).show();
						initSetting();
					}

					public void initStart() {
						Toast.makeText(BaiduMapActivity.this, "百度导航引擎初始化开始",
								Toast.LENGTH_SHORT).show();
					}

					public void initFailed() {
						Toast.makeText(BaiduMapActivity.this, "百度导航引擎初始化失败",
								Toast.LENGTH_SHORT).show();
					}

				}, null, ttsHandler, null);

	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	private void routeplanToNavi(CoordinateType coType, double desLat,
			double desLon, String destName) {
		BNRoutePlanNode sNode = null;
		BNRoutePlanNode eNode = null;

		sNode = new BNRoutePlanNode(app.lontitude, app.latitude, app.address,
				null, coType);
		eNode = new BNRoutePlanNode(desLon, desLat, destName, null, coType);
		if (sNode != null && eNode != null) {
			List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
			list.add(sNode);
			list.add(eNode);
			BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true,
					new DemoRoutePlanListener(sNode));
		}
	}

	public class DemoRoutePlanListener implements RoutePlanListener {

		private BNRoutePlanNode mBNRoutePlanNode = null;

		public DemoRoutePlanListener(BNRoutePlanNode node) {
			mBNRoutePlanNode = node;
		}

		@Override
		public void onJumpToNavigator() {
			/*
			 * 设置途径点以及resetEndNode会回调该接口
			 */

			for (Activity ac : activityList) {

				if (ac.getClass().getName().endsWith("BNDemoGuideActivity")) {

					return;
				}
			}
			Intent intent = new Intent(BaiduMapActivity.this,
					NaviGuideActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable(ROUTE_PLAN_NODE,
					(BNRoutePlanNode) mBNRoutePlanNode);
			intent.putExtras(bundle);
			startActivity(intent);

		}

		@Override
		public void onRoutePlanFailed() {
			// TODO Auto-generated method stub
			Toast.makeText(BaiduMapActivity.this, "算路失败", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void initSetting() {
		BNaviSettingManager
				.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
		BNaviSettingManager
				.setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
		BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
		BNaviSettingManager
				.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
		BNaviSettingManager
				.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
	}

	private BNOuterTTSPlayerCallback mTTSCallback = new BNOuterTTSPlayerCallback() {

		@Override
		public void stopTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "stopTTS");
		}

		@Override
		public void resumeTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "resumeTTS");
		}

		@Override
		public void releaseTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "releaseTTSPlayer");
		}

		@Override
		public int playTTSText(String speech, int bPreempt) {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "playTTSText" + "_" + speech + "_" + bPreempt);

			return 1;
		}

		@Override
		public void phoneHangUp() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneHangUp");
		}

		@Override
		public void phoneCalling() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneCalling");
		}

		@Override
		public void pauseTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "pauseTTS");
		}

		@Override
		public void initTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "initTTSPlayer");
		}

		@Override
		public int getTTSState() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "getTTSState");
			return 1;
		}
	};
}
