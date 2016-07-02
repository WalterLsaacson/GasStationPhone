package com.guanyin.baidumap;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.guanyin.activity.R;

public class AlertActivity extends Activity implements OnClickListener {

	private Context context = AlertActivity.this;
	private Bundle bundle;
	// 选择油品
	private TextView tv_chaiyou;
	private TextView tv_92;
	private TextView tv_95;
	// 获取到的json array
	private JSONArray jsonArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.alertdialog);
		bundle = new Bundle();
		bundle = getIntent().getExtras();
		initViews();
	}

	private void initViews() {
		// 设置alertDialog
		tv_chaiyou = (TextView) findViewById(R.id.tv_chaiyou);
		tv_92 = (TextView) findViewById(R.id.tv_92);
		tv_95 = (TextView) findViewById(R.id.tv_95);

		try {
			tv_chaiyou
					.setText(jsonArray.getJSONObject(0).getString("gas_type"));
			tv_92.setText(jsonArray.getJSONObject(1).getString("gas_type"));
			tv_95.setText(jsonArray.getJSONObject(2).getString("gas_type"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		tv_chaiyou.setOnClickListener(this);
		tv_92.setOnClickListener(this);
		tv_95.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_chaiyou:
			bundle.putString("oil_type", "柴油");
		case R.id.tv_92:
			bundle.putString("oil_type", "#92");
		case R.id.tv_95:
			bundle.putString("oil_type", "#95");
			Intent intent = new Intent(context, LicenseOilgunActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
			break;
		default:
			break;
		}

	}
}
