package com.guanyin.userface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import okhttp3.Call;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.guanyin.activity.R;
import com.guanyin.baidumap.BaiduMapActivity;
import com.guanyin.baidumap.LicenseOilgunActivity;
import com.guanyin.data.Gas;
import com.guanyin.data.StationInfo;
import com.guanyin.utils.Const;
import com.guanyin.utils.MyApplication;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

public class ViewPagerActivity extends Activity implements OnClickListener {

	// 设置辅助变量
	private Context context = ViewPagerActivity.this;
	private String TAG = "ViewPagerActivity";
	private MyApplication app;
	private Intent intent;
	// 搭建view pager
	private ArrayList<View> fragments = new ArrayList<View>();
	private ViewPager viewPager;
	private View view0, view1;

	// 设置单选框的选择事件
	private RadioButton rb_main;
	private RadioButton rb_carb;
	private RadioButton rb_photo;
	private RadioButton rb_iphone;

	// 注销
	private AlertDialog dialog;

	// 设置跳转到加油界面
	private ImageView iv_oil;
	private TextView tv_logout;

	// 获取加油站
	private ProgressDialog progressDialog;
	public static ArrayList<StationInfo> stations;
	public static ArrayList<Gas> gasList;
	// 设置view0的界面
	private TextView tv_city_name;
	private TextView tv_cur_date;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (app == null) {
			app = MyApplication.getInstance();
		}
		setContentView(R.layout.activity_main_inter_face);

		initViews();

		initDatas();

		PagerAdapter pagerAdapter = new PagerAdapter() {

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				return fragments.size();
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(fragments.get(position));
				return fragments.get(position);
			}
		};

		viewPager.setAdapter(pagerAdapter);

	}

	private void initDatas() {
		gasList = new ArrayList<Gas>();
		stations = new ArrayList<StationInfo>();
		fragments.add(view0);
		fragments.add(view1);
	}

	private void initViews() {
		// 初始化组件并声明选中事件

		viewPager = (ViewPager) findViewById(R.id.pager);
		rb_main = (RadioButton) findViewById(R.id.rb_main);
		rb_carb = (RadioButton) findViewById(R.id.rb_carb);
		rb_photo = (RadioButton) findViewById(R.id.rb_photo);
		rb_iphone = (RadioButton) findViewById(R.id.rb_iphone);

		view0 = getLayoutInflater().inflate(R.layout.fragment1, null);
		view1 = getLayoutInflater().inflate(R.layout.fragment2, null);

		// 设置加油的点击事件
		iv_oil = (ImageView) view0.findViewById(R.id.iv_oil);
		iv_oil.setOnClickListener(this);

		tv_logout = (TextView) view0.findViewById(R.id.tv_logout);
		tv_logout.setOnClickListener(this);
		// 设置底部radio group的点击事件
		rb_main.setOnClickListener(this);
		rb_carb.setOnClickListener(this);
		rb_photo.setOnClickListener(this);
		rb_iphone.setOnClickListener(this);
		// view0的界面
		tv_city_name = (TextView) view0.findViewById(R.id.city_name_tv);
		tv_city_name.setText(app.city);

		tv_cur_date = (TextView) view0.findViewById(R.id.cur_time_tv);
		tv_cur_date.setText(getCurDate());

	}

	private CharSequence getCurDate() {
		String[] week = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		Calendar cd = Calendar.getInstance();
		cd.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		cd.get(Calendar.YEAR);
		return cd.get(Calendar.YEAR) + "/" + (cd.get(Calendar.MONTH) + 1) + "/"
				+ cd.get(Calendar.DATE)
				+ week[cd.get(Calendar.DAY_OF_WEEK) - 1];
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_oil:
			catchStation();
			break;
		case R.id.tv_logout:
			LayoutInflater inflater = LayoutInflater.from(this);
			final View loginView = inflater.inflate(R.layout.logout_dialog,
					null);
			AlertDialog.Builder loginBuilder = new AlertDialog.Builder(this);
			loginBuilder.setView(loginView);

			loginBuilder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Editor editor = app.sp.edit();
							editor.putString("token", "");
							editor.apply();
							intent = new Intent(context, LoginActivity.class);
							startActivity(intent);
							finish();
						}

					});
			loginBuilder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							ViewPagerActivity.this.dialog.dismiss();
						}

					});

			dialog = loginBuilder.create();
			dialog.show();
			break;
		case R.id.rb_main:
			viewPager.setCurrentItem(0, true);
			break;
		case R.id.rb_carb:
			viewPager.setCurrentItem(1, true);
			break;
		case R.id.rb_photo:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivity(intent);
			break;
		case R.id.rb_iphone:
			intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse("tel:" + "18829282408"));
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	private void catchStation() {
		// 获取加油站信息,并决定跳转到支付界面还是导航界面
		JSONObject jsonText = new JSONObject();
		try {
			jsonText.put("mobile", app.sp.getString("mobile", ""))
					.put("lon", app.lontitude).put("lat", app.latitude);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		progressDialog = ProgressDialog
				.show(context, null, "定位成功，正在获取加油站信息...");
		OkHttpUtils.post().url(Const.serverurl)
				.addParams("route", Const.apiGetStation)
				.addParams("jsonText", jsonText.toString())
				.addParams("token", app.sp.getString("token", null)).build()
				.execute(new StringCallback() {
					@Override
					public void onResponse(String response) {
						JSONObject jsonresponse = null;
						Const.log(TAG, response);
						try {
							jsonresponse = new JSONObject(response);

						} catch (JSONException e) {
							e.printStackTrace();
						}
						if (jsonresponse.has("status")) {

							try {
								if (jsonresponse.getJSONObject("status")
										.getString("succeed").equals("1")) {
									Const.log(TAG, "获取到加油站信息！");
									if (jsonresponse.getJSONObject("data")
											.getString("list").equals("0")) {
										stations.clear();
										Const.log(TAG, "转向地图");
										Const.showToast(context,
												"当前位置三百米之内没有加油站！");
										JSONArray arrayStations = jsonresponse
												.getJSONObject("data")
												.getJSONArray("stations");
										if (arrayStations != null
												&& arrayStations.length() > 0) {
											for (int i = 0; i < arrayStations
													.length(); i++) {
												StationInfo siInfo = new StationInfo();
												JSONObject stationJson = new JSONObject();
												stationJson = arrayStations
														.getJSONObject(i);
												siInfo.station_id = stationJson
														.getString("station_id");
												siInfo.station_name = stationJson
														.getString("station_name");
												siInfo.address = stationJson
														.getString("address");
												siInfo.latitude_num = stationJson
														.getString("latitude_num");
												siInfo.longitude_num = stationJson
														.getString("longitude_num");
												siInfo.is_cooperate = stationJson
														.getString("is_cooperate");
												stations.add(siInfo);
											}
										}

										// 当前位置三百米之内没有加油站时转到地图，设置地图的相关事宜，然后可以导航过去
										intent = new Intent(context,
												BaiduMapActivity.class);
										startActivity(intent);
									} else {
										stations.clear();
										JSONObject stationsObject = jsonresponse
												.getJSONObject("data")
												.getJSONArray("stations")
												.getJSONObject(1);
										StationInfo siInfo = new StationInfo();
										siInfo.station_id = stationsObject
												.getString("station_id");
										siInfo.station_name = stationsObject
												.getString("station_name");
										siInfo.address = stationsObject
												.getString("address");
										siInfo.latitude_num = stationsObject
												.getString("latitude_num");
										siInfo.longitude_num = stationsObject
												.getString("longitude_num");
										siInfo.is_cooperate = stationsObject
												.getString("is_cooperate");
										JSONArray gasArray = new JSONArray();
										gasArray = stationsObject
												.getJSONArray("gasprice");
										if (gasArray != null
												&& gasArray.length() > 0) {
											gasList.clear();
											for (int i = 0; i < gasArray
													.length(); i++) {
												JSONObject gasObject = gasArray
														.getJSONObject(i);
												Gas gas = new Gas();
												gas.oil_price = gasObject
														.getString("price");
												if (i == 2) {
													gas.gas_type = gasObject
															.getString("gas_type");
												} else {
													gas.gas_type = gasObject
															.getString("gas");
												}
												gasList.add(gas);
											}
										}
										stations.add(siInfo);
										Const.log(TAG, "跳转到支付");
										intent = new Intent(context,
												LicenseOilgunActivity.class);
										startActivity(intent);
									}

								} else {
									Const.log(response, "获取失败！");
								}
								if (jsonresponse.getJSONObject("status")
										.getString("error_code").equals("2002")) {
									Const.showToast(context,
											"该用户已在其他地方登录，请重新登录！");
									intent = new Intent(context,
											LoginActivity.class);
									startActivity(intent);
									finish();
								} else {
									Const.showToast(context,
											jsonresponse
													.getJSONObject("status")
													.getString("error_desc"));
								}

								progressDialog.dismiss();
							} catch (JSONException e) {
								e.printStackTrace();
								progressDialog.dismiss();
							}
						} else {
							Const.showToast(context, "服务器异常请重试...");
						}

					}

					@Override
					public void onError(Call request, Exception e) {
						Const.log(TAG, e.getMessage());
					}
				});

	}
}
