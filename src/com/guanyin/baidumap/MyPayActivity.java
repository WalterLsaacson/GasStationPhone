package com.guanyin.baidumap;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.guanyin.activity.R;
import com.guanyin.alipay.PayActivity;
import com.guanyin.data.StationInfo;
import com.guanyin.userface.ViewPagerActivity;
import com.guanyin.utils.Const;
import com.guanyin.utils.MyApplication;

public class MyPayActivity extends Activity implements OnClickListener {

	// 声明控件
	private EditText etBillInfo;
	private TextView tv_stationName;
	private TextView tv_stationDistance;
	private ImageView iv_money;
	private ImageView iv_pay_type;
	private TextView tv_money;
	private TextView tv_type;
	private TextView tv_add_oil;
	private TextView tv_pay_money;
	private ImageView iv_type;
	// pop window
	private PopupWindow popWindow;
	private View layout;
	private ListView listView;
	private MyPayAdapter myAdapter;
	// 给pop window 设置点击事件
	private View view;
	private TextView tv_cancel;
	// 给list view设置数据
	private ArrayList<String> money;
	private ArrayList<String> pay_type;

	// 辅助变量
	private Context context = MyPayActivity.this;
	private Intent intent;

	private MyApplication app;
	private StationInfo station;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		if (app == null) {
			app = MyApplication.getInstance();
		}
		setContentView(R.layout.activity_pay);

		initDatas();

		initView();

	}

	private void initDatas() {

		station = ViewPagerActivity.stations.get(0);

		money = new ArrayList<String>();
		money.add("20");
		money.add("100");
		money.add("200");
		money.add("500");

		pay_type = new ArrayList<String>();
		pay_type.add("支付宝");
		pay_type.add("微信");
	}

	private void initView() {

		etBillInfo = (EditText) findViewById(R.id.etBillInfo);
		etBillInfo.requestFocus();

		tv_stationName = (TextView) findViewById(R.id.tv_pay_name);
		tv_stationName.setText(station.station_name);

		tv_stationDistance = (TextView) findViewById(R.id.tv_pay_distance);
		// tv_stationDistance.setText(station.calculDistance() + "m");

		iv_money = (ImageView) findViewById(R.id.iv_money);
		iv_money.setOnClickListener(this);

		iv_pay_type = (ImageView) findViewById(R.id.iv_pay_type);
		iv_pay_type.setOnClickListener(this);

		tv_money = (TextView) findViewById(R.id.tvMoney);
		iv_type = (ImageView) findViewById(R.id.iv_type);
		tv_type = (TextView) findViewById(R.id.tvType);
		tv_pay_money = (TextView) findViewById(R.id.tvPayMoney);

		tv_add_oil = (TextView) findViewById(R.id.tvAddOil);
		tv_add_oil.setOnClickListener(this);

		layout = LayoutInflater.from(context).inflate(
				R.layout.pop_window_oilgun, null);
		listView = (ListView) layout.findViewById(R.id.oilgun_list);

		view = layout.findViewById(R.id.view);
		view.setOnClickListener(this);

		tv_cancel = (TextView) layout.findViewById(R.id.tv_cancel);
		tv_cancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_money:
			myAdapter = new MyPayAdapter(context, money, "金额", true);
			listView.setAdapter(myAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					tv_money.setText(money.get(position));
					tv_pay_money.setText("实付：" + money.get(position) + "元");
					popWindow.dismiss();
				}
			});
			MyPop();
			break;
		case R.id.iv_pay_type:
			myAdapter = new MyPayAdapter(context, pay_type, "", false);
			listView.setAdapter(myAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					tv_type.setText(pay_type.get(position));
					if (position == 0) {
						iv_type.setBackgroundResource(R.drawable.pay_icon_alipay);
					} else {
						iv_type.setBackgroundResource(R.drawable.pay_icon_wechat);
					}
					popWindow.dismiss();
				}
			});
			MyPop();
			break;
		case R.id.tvAddOil:
			Const.showToast(context, "获取到的内容：" + tv_type.getText().toString());
			if (tv_type.getText().toString().equals("支付宝")) {
				intent = new Intent(context, PayActivity.class);
				startActivity(intent);
			} else {
				Const.showToast(context, "正在拼命开通微信支付功能...");
			}

			break;
		case R.id.view:
		case R.id.tv_cancel:
			popWindow.dismiss();
			break;

		default:
			break;
		}
	}

	private void MyPop() {

		popWindow = new PopupWindow(layout, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, true);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		// 设置SelectPicPopupWindow弹出窗体的背景
		popWindow.setBackgroundDrawable(dw);

		popWindow.setTouchable(true); // 设置pop window可点击

		/* 设置pop window里边视图view的位置 */
		popWindow.showAtLocation(layout, Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 0, 0);
	}
}
