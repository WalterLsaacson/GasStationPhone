package com.guanyin.mappay;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.guanyin.activity.R;
import com.guanyin.data.StationInfo;
import com.guanyin.userface.ViewPagerActivity;
import com.guanyin.utils.Const;
import com.guanyin.utils.MyApplication;

public class LicenseOilgunActivity extends Activity implements
		View.OnClickListener {

	private MyApplication app;
	private Context context = LicenseOilgunActivity.this;
	// 组件的声明
	private TextView tvAddOil;
	private ImageView iv_license_more;
	private ImageView iv_oil_gun_more;
	private TextView tv_stationName;
	private TextView tv_stationDistance;

	private TextView tv_oilgun;
	private TextView tv_license;
	private TextView tv_type;
	private ImageView iv_back;
	// 辅助工具的声明
	private Intent intent;
	// 声明油枪和车牌号的数组
	private ArrayList<String> oilGuns;
	private ArrayList<String> licenses;
	private ArrayList<String> types;
	// 获取到pop window的布局和list view
	private View layout;
	private ListView listView;
	private MyPayAdapter myAdapter;
	private PopupWindow pop;
	// 给pop window 设置点击事件
	private View view;
	private TextView tv_cancel;
	// 只有单个的加油站信息
	private StationInfo station;

	// 记录订单信息
	private String oil_price;
	private String oil_type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (app == null) {
			app = MyApplication.getInstance();
		}
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_license_oilgun);

		initDatas();
		initView();

	}

	private void initDatas() {

		station = ViewPagerActivity.stations.get(0);
		oilGuns = new ArrayList<String>();
		oilGuns.add("1号");
		oilGuns.add("3号");
		oilGuns.add("5号");
		oilGuns.add("7号");

		licenses = new ArrayList<String>();
		licenses.add("陕AIOJFI");
		licenses.add("陕AIFFDI");
		licenses.add("陕AFDBFD");
		licenses.add("陕FSFDSI");

		types = new ArrayList<String>();
		for (int i = 0; i < ViewPagerActivity.gasList.size(); i++) {
			if (ViewPagerActivity.gasList.get(i).gas_type.equals("92#")) {
				oil_price = ViewPagerActivity.gasList.get(i).oil_price;
			}
			types.add(ViewPagerActivity.gasList.get(i).gas_type);
		}
	}

	private void initView() {
		// 提交表单
		tvAddOil = (TextView) findViewById(R.id.tvAddOil);
		tvAddOil.setOnClickListener(this);

		// 设置弹出pop window
		iv_license_more = (ImageView) findViewById(R.id.iv_license_more);
		iv_license_more.setOnClickListener(this);

		iv_oil_gun_more = (ImageView) findViewById(R.id.iv_oil_gun_more);
		iv_oil_gun_more.setOnClickListener(this);

		// 设置文字
		tv_stationName = (TextView) findViewById(R.id.tvStationName);
		tv_stationName.setText(station.station_name);

		tv_stationDistance = (TextView) findViewById(R.id.tvStationDistance);
		Const.log("LicenseOilgunActivity", station.latitude_num + ""
				+ station.longitude_num);
		tv_stationDistance.setText(calculDistance(station.latitude_num,
				station.longitude_num));

		tv_oilgun = (TextView) findViewById(R.id.tvBindOilgun);
		tv_license = (TextView) findViewById(R.id.tvBindLicense);

		// pop window 组件的声明
		layout = getLayoutInflater().inflate(R.layout.pop_window_oilgun, null);
		view = layout.findViewById(R.id.view);
		view.setOnClickListener(this);

		listView = (ListView) layout.findViewById(R.id.oilgun_list);

		tv_cancel = (TextView) layout.findViewById(R.id.tv_cancel);
		tv_cancel.setOnClickListener(this);

		iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(this);

		tv_type = (TextView) findViewById(R.id.tv_type);
		tv_type.setOnClickListener(this);

		oil_type = tv_type.getText().toString();

	}

	private String calculDistance(String lati, String lon) {
		double latidouble = Double.parseDouble(lati);
		double londouble = Double.parseDouble(lati);
		return Const.GetDistance(app.latitude, app.lontitude, latidouble,
				londouble) + "m";
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvAddOil:
			Editor editor = app.sp.edit();
			editor.putString("oil_type", oil_type);
			editor.putString("oil_price", oil_price);
			editor.commit();
			intent = new Intent(this, MyPayActivity.class);
			startActivity(intent);
			break;
		case R.id.iv_back:
			finish();
			break;
		case R.id.tv_type:
			// 先设置list view
			myAdapter = new MyPayAdapter(context, types, "油品", true);
			listView.setAdapter(myAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					tv_type.setText(types.get(position) + "#");
					oil_type = tv_type.getText().toString();
					oil_price = ViewPagerActivity.gasList.get(position).oil_price;
					pop.dismiss();
				}
			});

			MyPop();
			break;
		case R.id.iv_oil_gun_more:
			// 先设置list view
			myAdapter = new MyPayAdapter(context, oilGuns, "油枪号", true);
			listView.setAdapter(myAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					tv_oilgun.setText(oilGuns.get(position));
					pop.dismiss();
				}
			});

			MyPop();

			break;
		case R.id.iv_license_more:
			// 先设置list view
			myAdapter = new MyPayAdapter(context, licenses, "车牌号", true);
			listView.setAdapter(myAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					tv_license.setText(licenses.get(position));
					pop.dismiss();
				}
			});

			MyPop();

			break;
		case R.id.view:
		case R.id.tv_cancel:
			pop.dismiss();
			break;
		}
	}

	// 弹出pop window
	private void MyPop() {

		pop = new PopupWindow(layout, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, true);

		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		// 设置SelectPicPopupWindow弹出窗体的背景
		pop.setBackgroundDrawable(dw);

		pop.setTouchable(true); // 设置pop window可点击

		/* 设置pop window里边视图view的位置 */
		pop.showAtLocation(layout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
				0, 0);
	}

}
