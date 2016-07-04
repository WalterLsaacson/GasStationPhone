package com.guanyin.userface;

import okhttp3.Call;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.guanyin.activity.R;
import com.guanyin.utils.Const;
import com.guanyin.utils.MyApplication;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

public class ForgetpwdActivity extends Activity implements OnClickListener {
	private String TAG = "ForgetpwdActivity";
	private MyApplication app;
	private Context Context = ForgetpwdActivity.this;

	private EditText phone;
	private EditText indent_code;
	private EditText password1;
	private EditText password2;
	private TextView get_code;
	private TextView confirm;

	private ImageView iv_back;
	private ProgressDialog dialog;
	private String xauto_code;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (app == null) {
			app = MyApplication.getInstance();
		}
		setContentView(R.layout.activity_forgetpwd);
		initView();
	}

	private void initView() {

		phone = (EditText) findViewById(R.id.et_forget_phone);
		indent_code = (EditText) findViewById(R.id.et_forget_ident_code);
		password1 = (EditText) findViewById(R.id.et_forget_pwd1);
		password2 = (EditText) findViewById(R.id.et_forget_pwd);
		get_code = (TextView) findViewById(R.id.tv_forget_get_code);
		confirm = (TextView) findViewById(R.id.tv_forget_confirm);
		iv_back = (ImageView) findViewById(R.id.iv_back_forget);

		iv_back.setOnClickListener(this);
		get_code.setOnClickListener(this);
		confirm.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_forget_get_code:
			getCode();
			break;
		case R.id.iv_back_forget:
			finish();
			break;
		case R.id.tv_forget_confirm:
			confirm();
			break;
		}

	}

	private void confirm() {
		if (!(password1.getText().toString()).equals(password2.getText()
				.toString())) {
			Const.showToast(Context, "两次密码不一致！");
			return;
		}
		if (password1.getText().toString().trim().length() < 4) {
			Const.showToast(Context, "密码长度过短");
			return;
		}
		JSONObject jsonObject = new JSONObject();
		try {
			// {"login_name":"18602951905","type":"1","new_password":"123456","xauto_code":"3a1069442ebb5dddb62e1972032a57d7"}
			jsonObject.put("login_name", phone.getText().toString());
			jsonObject.put("type", 1);
			jsonObject.put("new_password", password1.getText().toString());
			jsonObject.put("xauto_code", Const.getMD5Str(xauto_code + "xauto"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		dialog = ProgressDialog.show(Context, null, "正在重置密码...");
		OkHttpUtils.post().url(Const.serverurl)
				.addParams("route", Const.apiRouteReset)
				.addParams("jsonText", jsonObject.toString()).build()
				.execute(new StringCallback() {

					@Override
					public void onResponse(String response) {
						Const.log(TAG, response);
						// {"status":{"succeed":"1"},"data":{"member_id":"5","member_name":"testname2","real_name":"","sex":"0","birthday":"0000-00-00 00:00:00","email":"","area_code":"86","mobile":"18602951905","icon_name":"","icon_location":"","signature":"","id_number":"","region_id":"0","token":"956ddca9d19e92aba517ad952f0a6c02"}}
						JSONObject jsonObject = null;
						try {
							jsonObject = new JSONObject(response);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						try {
							if (jsonObject.getJSONObject("status")
									.getString("succeed").equals("1")) {
								Const.showToast(Context, "密码重置成功！");
								finish();
							} else {
								Const.showToast(Context, "密码重置失败！");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						dialog.dismiss();

					}

					@Override
					public void onError(Call request, Exception e) {
						Const.log(TAG, e.getMessage());
						dialog.dismiss();
					}
				});

	}

	private void getCode() {

		if (phone.getText().toString().length() == 0) {
			Const.showToast(Context, "手机号码不能为空");
			return;
		}
		TimerCount timerCount = new TimerCount(60000, 1000);
		timerCount.start();
		dialog = ProgressDialog.show(Context, null, "正在获取验证码，请稍侯...");
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("login_name", phone.getText().toString().trim());
			jsonObject.put("type", "1");
			jsonObject.put("code", Const.getMD5Str(phone.getText().toString()
					.trim()
					+ "xauto"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		OkHttpUtils.post().url(Const.serverurl)
				.addParams("route", Const.apiRouteForget)
				.addParams("jsonText", jsonObject.toString()).build()
				.execute(new StringCallback() {

					@Override
					public void onResponse(String response) {
						Const.log(TAG, response);
						JSONObject jObject = null;

						try {
							jObject = new JSONObject(response);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						try {
							if (jObject.getJSONObject("status")
									.getString("succeed").equals("1")) {
								indent_code.setText(jObject.getJSONObject(
										"data").getString("verify_code"));
								xauto_code = jObject.getJSONObject("data")
										.getString("xauto_code");
							} else {
								Const.showToast(Context, "获取验证码失败");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						dialog.dismiss();

					}

					@Override
					public void onError(Call request, Exception e) {
						Const.showToast(Context, e.getMessage());
						dialog.dismiss();
					}
				});

	}

	public class TimerCount extends CountDownTimer {

		public TimerCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			get_code.setText("点击获取验证码");
			get_code.setClickable(true);

		}

		@Override
		public void onTick(long millisUntilFinished) {
			get_code.setClickable(false);
			get_code.setText(millisUntilFinished / 1000 + "s后重新获取验证码");
			// TODO 设置背景图片
			// get_code.setBackground();

		}
	}

}
