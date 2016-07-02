package com.guanyin.userface;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.guanyin.activity.R;
import com.guanyin.utils.MyApplication;

public class EntranceActivity extends Activity {
	// 声明辅助变量
	private MyApplication app;
	private Context Context = EntranceActivity.this;
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 获取MyApplication实例
		if (app == null) {
			app = MyApplication.getInstance();
		}
		// 去除title
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		// 设置定时器来选择页面
		Timer timer = new Timer();
		if (app.sp.getString("token", "").equals("")) {

			TimerTask timerTask = new TimerTask() {

				@Override
				public void run() {
					intent = new Intent(Context, LoginActivity.class);
					startActivity(intent);
					finish();
				}
			};
			timer.schedule(timerTask, 2000);
		} else {
			TimerTask timerTask = new TimerTask() {

				@Override
				public void run() {
					intent = new Intent(Context, ViewPagerActivity.class);
					startActivity(intent);
					finish();
				}
			};
			timer.schedule(timerTask, 2000);
		}
	}

}
