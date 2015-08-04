package com.slook.smack.adapter;

import java.util.List;

import org.jivesoftware.smackx.muc.HostedRoom;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 聊天室 数据适配器
 * @author 廖乃波
 *
 */
public class HostRoomAdapter  extends BaseAdapter {
	private Context context;

	private List<HostedRoom> roominfos ;

	public HostRoomAdapter(Context context, List<HostedRoom> roominfos) {
		super();
		this.context = context;
		this.roominfos = roominfos;
	}

	@Override
	public int getCount() {
		return roominfos.size();
	}

	@Override
	public Object getItem(int position) {
		return roominfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private class ViewHolder {
		TextView tv_Name;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		//作为一个对象的持有者，能提高ListView的总体性能
		HostedRoom room = roominfos.get(position);
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			LinearLayout layout = new LinearLayout(context);

			holder.tv_Name = new TextView(context);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			layout.addView(holder.tv_Name);
			convertView = layout;
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tv_Name.setText(room.getJid());
		return convertView;
	}
}