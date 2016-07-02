package com.guanyin.baidumap;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
	// 辅助工具的声明
	private Intent intent;
	// 声明油枪和车牌号的数组
	private ArrayList<String> oilGuns;
	private ArrayList<String> licenses;
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
	private double distance;

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

		distance = station.calculDistance();
		tv_stationDistance = (TextView) findViewById(R.id.tvStationDistance);
		tv_stationDistance.setText(distance + "m");

		tv_oilgun = (TextView) findViewById(R.id.tvBindOilgun);
		tv_license = (TextView) findViewById(R.id.tvBindLicense);

		// pop window 组件的声明
		layout = getLayoutInflater().inflate(R.layout.pop_window_oilgun, null);
		view = layout.findViewById(R.id.view);
		view.setOnClickListener(this);

		listView = (ListView) layout.findViewById(R.id.oilgun_list);

		tv_cancel = (TextView) layout.findViewById(R.id.tv_cancel);
		tv_cancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvAddOil:
			intent = new Intent(this, MyPayActivity.class);
			startActivity(intent);
			break;
		case R.id.iv_oil_gun_more:
			// 先设置list view
			myAdapter = new MyPayAdapter(context, oilGuns, "油枪号", true);
			listView.setAdapter(myAdapter);
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
