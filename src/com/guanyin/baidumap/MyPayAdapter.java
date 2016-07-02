package com.guanyin.baidumap;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.guanyin.activity.R;

//支付方式的适配器
public class MyPayAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<String> items;
	private String mold;
	private boolean type;

	public MyPayAdapter(Context context, ArrayList<String> items, String mold,
			boolean type) {
		super();
		this.context = context;
		this.items = items;
		this.mold = mold;
		this.type = type;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = new ViewHolder();
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.oilgun_item_list, null);
			viewHolder.tv_title = (TextView) convertView
					.findViewById(R.id.item_title);
			viewHolder.iv_type_pay = (ImageView) convertView
					.findViewById(R.id.item_type_pay);
			viewHolder.tv_content = (TextView) convertView
					.findViewById(R.id.item_content);
			convertView.setTag(viewHolder);
		}
		viewHolder = (ViewHolder) convertView.getTag();

		TextView tv_title = viewHolder.tv_title;
		ImageView iv_pay = viewHolder.iv_type_pay;

		if (type) {
			tv_title.setVisibility(View.VISIBLE);
			iv_pay.setVisibility(View.GONE);
			tv_title.setText(mold);
		} else {
			iv_pay.setVisibility(View.VISIBLE);
			tv_title.setVisibility(View.GONE);
			if (position == 0) {
				iv_pay.setImageResource(R.drawable.pay_icon_alipay);
			} else {
				iv_pay.setImageResource(R.drawable.pay_icon_wechat);
			}
		}
		TextView tv_content = viewHolder.tv_content;
		tv_content.setText(items.get(position));
		return convertView;
	}

	class ViewHolder {
		TextView tv_title;
		TextView tv_content;
		ImageView iv_type_pay;
	}

}
