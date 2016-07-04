package com.guanyin.userface;

import okhttp3.Call;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.guanyin.activity.R;
import com.guanyin.utils.Const;
import com.guanyin.utils.MyApplication;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

public class LoginActivity extends Activity implements OnClickListener {
	// 辅助变量和
	private Context Context = LoginActivity.this;
	private String TAG = "LoginActivity";
	private Intent intent;
	private MyApplication application;
	// 进度条
	private ProgressDialog dialog;
	// 声明布局中的组件
	private TextView register;
	private TextView agreement;
	private TextView forget_password;
	private TextView login;
	private EditText username;
	private EditText password;
	private CheckBox cb_agree;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 获取MyApplication实例
		if (application == null) {
			application = MyApplication.getInstance();
		}
		// 去除title
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_login);

		initView();
	}

	private void initView() {
		// 初始化组件并设置监听事件
		register = (TextView) findViewById(R.id.tv_login_register);
		agreement = (TextView) findViewById(R.id.tv_agree_license);
		forget_password = (TextView) findViewById(R.id.tv_login_forget);
		login = (TextView) findViewById(R.id.tv_login_login);

		username = (EditText) findViewById(R.id.et_login_username);
		password = (EditText) findViewById(R.id.et_login_pwd);

		cb_agree = (CheckBox) findViewById(R.id.cb_agree_license);

		register.setOnClickListener(this);
		agreement.setOnClickListener(this);
		forget_password.setOnClickListener(this);
		login.setOnClickListener(this);
		cb_agree.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_login_login:
			login();
			break;
		case R.id.tv_login_register:
			// 注册
			intent = new Intent(Context, RegisterActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_login_forget:
			// 忘记密码
			intent = new Intent(Context, ForgetpwdActivity.class);
			startActivity(intent);
			break;
		// 阅读用户协议
		case R.id.tv_agree_license:
			Const.openUrl(Context, "http://connect.qq.com/agreement_chs");
			break;
		default:
			break;
		}
	}

	private void login() {
		if (dialog != null && dialog.isShowing()) {
			return;
		}
		if (username.getText().toString().length() == 0) {
			Const.showToast(Context, "用户名不能为空！");
			return;
		}
		if (password.getText().toString().length() == 0) {
			Const.showToast(Context, "密码不能为空！");
			return;
		}
		if (!cb_agree.isChecked()) {
			Const.showToast(Context, "请查看用户协议！");
			return;
		}
		dialog = ProgressDialog.show(Context, null, "正在登录，请稍后......");
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("login_name", username.getText().toString().trim());
			jsonObject.put("password", password.getText().toString().trim());
			jsonObject.put("type", "1");
			jsonObject.put(
					"code",
					Const.getMD5Str(username.getText().toString().trim()
							+ "xauto"));
			jsonObject.put("login_type", "0");
		} catch (JSONException e) {
		}
		OkHttpUtils.post().url(Const.serverurl)
				.addParams("route", Const.apiRouteLogin)
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
								// 将获取到的token和手机号码使用sp存起来
								Editor editor = application.sp.edit();
								editor.putString("token",
										jObject.getJSONObject("data")
												.getString("token"));
								editor.putString("mobile", username.getText()
										.toString());
								// 异步提交数据
								editor.apply();
								// 登录成功则跳转到地图界面
								intent = new Intent(Context,
										ViewPagerActivity.class);
								startActivity(intent);

								finish();
								Const.showToast(Context, "登录成功！");

							} else {
								Const.showToast(Context, "登录失败："
										+ jObject.getJSONObject("status")
												.getString("error_desc"));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						dialog.dismiss();
					}

					@Override
					public void onError(Call reuqest, Exception e) {
						Const.showToast(Context, e.getMessage());
						dialog.dismiss();
					}
				});

	}
}
