package com.guanyin.userface;

import java.io.File;

import okhttp3.Call;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
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

public class RegisterActivity extends Activity implements OnClickListener {
	private String TAG = "RegisterActivity";
	private MyApplication app;
	private Context Context = RegisterActivity.this;

	// private Intent intent;
	private ProgressDialog dialog;

	private ImageView user_icon;
	private EditText phone;
	private EditText indent_code;
	private EditText username;
	private EditText password;
	private TextView get_code;
	private TextView register;
	private ImageView iv_back;

	private String xauto_code;
	private boolean submit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (app == null) {
			app = MyApplication.getInstance();
		}
		setContentView(R.layout.activity_register);

		initView();
	}

	private void initView() {
		user_icon = (ImageView) findViewById(R.id.iv_register_icon);
		phone = (EditText) findViewById(R.id.et_register_phone);
		indent_code = (EditText) findViewById(R.id.et_register_ident_code);
		username = (EditText) findViewById(R.id.et_register_username);
		password = (EditText) findViewById(R.id.et_register_pwd);
		get_code = (TextView) findViewById(R.id.tv_register_get_code);
		register = (TextView) findViewById(R.id.tv_register_register);

		iv_back = (ImageView) findViewById(R.id.iv_back);

		iv_back.setOnClickListener(this);
		user_icon.setOnClickListener(this);
		get_code.setOnClickListener(this);
		register.setOnClickListener(this);
		user_icon.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_register_icon:
			setIcon();
			break;
		case R.id.tv_register_get_code:
			getCode();
			break;
		case R.id.tv_register_register:
			if (user_allow()) {
				Const.log(TAG, "开始注册");
				register();
			}
			break;
		case R.id.iv_back:
			finish();
			break;
		default:
			break;
		}
	}

	private void setIcon() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("设置头像");
		builder.setItems(new String[] { "从相册选择", "拍照" },
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							// 从相册选择
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);
							intent.setType("image/*");
							Intent intent2 = Intent.createChooser(intent,
									"选择图片");
							this.startActivityForResult(intent2, 0);
							break;
						case 1:
							// 拍照
							Intent intent3 = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							File facePhoto = new File(Environment
									.getExternalStorageDirectory(), "/tmp.jpg");
							// 如果目录存在或创建成功，表示存储卡存在，则获得原始图片，否则就使用缩略图
							if (facePhoto.getParentFile().exists()
									|| facePhoto.getParentFile().mkdir()) {
								intent3.putExtra(MediaStore.EXTRA_OUTPUT,
										Uri.fromFile(facePhoto));
							} else { // 如果使用缩略图，则设置相机竖屏拍照(仅对预览模式有效，对原始图片无效)
								intent3.putExtra(
										MediaStore.Images.Media.ORIENTATION, 0);
							}
							this.startActivityForResult(intent3, 1);
							break;
						}
					}

					private void startActivityForResult(Intent intent2, int i) {
						switch (i) {
						case 0:
							// 获取照片

							break;
						case 1:
							// 拍照

							break;

						default:
							break;
						}

					}
				});
		builder.create().show();

	}

	private boolean user_allow() {
		if (username.getText().toString().length() < 4
				|| username.getText().toString().length() > 10) {
			Const.showToast(Context, "昵称有误 ，请重新输入");
			return false;
		}
		dialog = ProgressDialog.show(Context, null, "正在验证昵称...");
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("member_name", username.getText().toString().trim());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		OkHttpUtils.post().url(Const.serverurl)
				.addParams("route", Const.apiRouteValidate)
				.addParams("jsonText", jsonObject.toString()).build()
				.execute(new StringCallback() {

					@Override
					public void onResponse(String response, int arg) {
						Const.log(TAG, response);
						JSONObject jsonObject = null;
						try {
							jsonObject = new JSONObject(response);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						try {
							if (jsonObject.getJSONObject("status")
									.getString("succeed").equals("1")) {
								Const.showToast(Context, "昵称效验成功");
								submit = true;
							} else {
								if (jsonObject.getJSONObject("status")
										.getString("error_code").equals("2005")) {
									Const.showToast(Context, "昵称被占用！");
									submit = false;
								}
								if (jsonObject.getJSONObject("status")
										.getString("error_code").equals("2006")) {
									Const.showToast(Context, "昵称含有非法字符！");
									submit = false;
								}

							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
						dialog.dismiss();

					}

					@Override
					public void onError(Call request, Exception e, int arg) {
						Const.showToast(Context, e.getMessage());
						username.requestFocus();
						submit = false;
						dialog.dismiss();
					}
				});
		return submit;
	}

	private void register() {

		if (password.getText().toString().length() == 0) {
			Const.showToast(Context, "密码不能为空");
			return;
		}
		if (password.getText().toString().length() < 6) {
			Const.showToast(Context, "密码长度过短");
			return;
		}
		dialog = ProgressDialog.show(Context, null, "正在注册...");

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("area_code", "86");
			jsonObject.put("mobile", phone.getText().toString().trim());
			jsonObject.put("xauto_code", Const.getMD5Str(xauto_code + "xauto"));
			jsonObject.put("password", password.getText().toString().trim());
			jsonObject.put("member_name", username.getText().toString().trim());
			jsonObject.put("type", "1");
			jsonObject.put("icon_name", "1");
			// jsonObject.put("dc", "");
			// jsonObject.put("car_number", "陕A123456");
			// jsonObject.put("car_type", "别克商务");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		OkHttpUtils.post().url(Const.serverurl)
				.addParams("route", Const.apiRouteRegister)
				.addParams("jsonText", jsonObject.toString()).build()
				.execute(new StringCallback() {

					@Override
					public void onResponse(String response, int arg) {
						Const.log(TAG, response);
						JSONObject jsonObject = null;

						try {
							jsonObject = new JSONObject(response);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						try {
							if (jsonObject.getJSONObject("status")
									.getString("succeed").equals("1")) {
								Const.showToast(Context, "注册成功");
							} else {
								Const.showToast(Context,
										jsonObject.getJSONObject("status")
												.getString("error_desc"));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						dialog.dismiss();
					}

					@Override
					public void onError(Call request, Exception e, int arg) {
						Const.showToast(Context, e.getMessage());
						dialog.dismiss();
					}
				});
	}

	private void getCode() {

		if (phone.getText().toString().length() == 0) {
			Const.showToast(Context, "手机号码不能为空");
			return;
		}
		final TimerCount timerCount = new TimerCount(60000, 1000);
		timerCount.start();
		dialog = ProgressDialog.show(Context, null, "正在获取验证码...");
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("area_code", "86");
			jsonObject.put("mobile", phone.getText().toString().trim());
			jsonObject.put("code", Const.getMD5Str(phone.getText().toString()
					.trim()
					+ "xauto"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		OkHttpUtils.post().url(Const.serverurl)
				.addParams("route", Const.apiRouteGetcode)
				.addParams("jsonText", jsonObject.toString()).build()
				.execute(new StringCallback() {

					@Override
					public void onResponse(String response, int arg) {
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
								xauto_code = jObject.getJSONObject("data")
										.getString("xauto_code");
								indent_code.setText(jObject.getJSONObject(
										"data").getString("verify_code"));
							} else {
								Const.showToast(Context, "获取验证码失败");
								Const.showToast(Context,
										jObject.getJSONObject("status")
												.getString("error_desc"));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						dialog.dismiss();
					}

					@Override
					public void onError(Call request, Exception e, int arg) {
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
			get_code.setText(millisUntilFinished / 1000 + "s后重新获取验证码");
			// TODO 设置背景图片
			get_code.setClickable(false);

		}
	}
}
