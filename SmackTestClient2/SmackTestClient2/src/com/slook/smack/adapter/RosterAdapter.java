package com.slook.smack.adapter;

import java.util.List;

import com.slook.smack.model.User;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 数据适配器类
 * 用于进入主界面的时候显示所有好友列表
 * @author 廖乃波    
 */
public class RosterAdapter extends BaseAdapter {
	private Context context;
	private List<User> userinfos;

	public RosterAdapter(Context context, List<User> userinfos) {
		super();
		this.context = context;
		this.userinfos = userinfos;
	}

	@Override
	public int getCount() {
		return userinfos.size();
	}

	@Override
	public Object getItem(int position) {
		return userinfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private class ViewHolder {
		TextView user;
		TextView status;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		User user = userinfos.get(position);
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			LinearLayout layout = new LinearLayout(context);

			holder.user = new TextView(context);
			holder.status = new TextView(context);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			layout.addView(holder.status);
			layout.addView(holder.user);
			convertView = layout;
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.user.setText(user.getUser());
		holder.status.setText(user.getStatus());

		return convertView;
	}
}
