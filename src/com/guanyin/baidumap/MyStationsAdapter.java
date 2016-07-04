package com.guanyin.baidumap;

import java.util.ArrayList;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.guanyin.activity.R;
import com.guanyin.data.StationInfo;
import com.guanyin.utils.Const;

public class MyStationsAdapter extends BaseAdapter {

	private ArrayList<StationInfo> stations;
	private BaiduMapActivity context;

	public MyStationsAdapter(ArrayList<StationInfo> stations,
			BaiduMapActivity context) {
		super();
		this.stations = stations;
		this.context = context;
	}

	@Override
	public int getCount() {
		return stations.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_item_gasstation, null);
			viewHolder = new ViewHolder();
			viewHolder.station_name = (TextView) convertView
					.findViewById(R.id.item_sattion_name);
			viewHolder.station_distance = (TextView) convertView
					.findViewById(R.id.item_station_distance);
			viewHolder.station_address = (TextView) convertView
					.findViewById(R.id.item_station_address);
			viewHolder.guide = (TextView) convertView
					.findViewById(R.id.item_guide);

			convertView.setTag(viewHolder);
		}
		TextView station_name = ((ViewHolder) convertView.getTag()).station_name;
		station_name.setText(stations.get(position).station_name);
		TextView station_distance = ((ViewHolder) convertView.getTag()).station_distance;
		// 设置距离
		station_distance.setText(Math.floor(stations.get(position)
				.calculDistance()) + "m");
		TextView station_address = ((ViewHolder) convertView.getTag()).station_address;
		station_address.setText(stations.get(position).address);
		TextView guide = ((ViewHolder) convertView.getTag()).guide;

		// 设置选中某个item之后，背景和导航按钮的显示
		if (position == 0) {
			guide.setVisibility(View.VISIBLE);
			convertView.setBackgroundColor(Color.parseColor("#d4d6dc"));
		} else {
			guide.setVisibility(View.INVISIBLE);
			convertView.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
		}
		// TODO 设置导航的点击事件
		guide.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Const.showToast(context, "出发去"
						+ stations.get(position).station_name);
				context.startNavi(
						Double.parseDouble(stations.get(position).latitude_num),
						Double.parseDouble(stations.get(position).longitude_num),
						stations.get(position).station_name);
			}
		});
		return convertView;
	}

	class ViewHolder {
		TextView station_name;
		TextView station_distance;
		TextView station_address;
		TextView guide;
	}

}
