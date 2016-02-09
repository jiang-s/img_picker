package com.js.imagepick.util;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.js.imagepick.ImageLoader;
import com.js.imagepick.ImageLoader.Type;
import com.js.imagepick.R;
import com.js.imagepick.bean.FolderBean;

public class ListDirAdaper extends ArrayAdapter<FolderBean> {
	
	private LayoutInflater mInflater;

	public ListDirAdaper(Context context, List<FolderBean> objects) {
		super(context, 0, objects);
		// TODO Auto-generated constructor stub
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if(convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_popup_main, parent, false);
			
			holder.mImg = (ImageView) convertView.findViewById(R.id.id_id_dir_item_image);
			holder.mDirName = (TextView) convertView.findViewById(R.id.id_dir_item_name);
			holder.mDirCount = (TextView) convertView.findViewById(R.id.id_dir_item_count);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		FolderBean bean = getItem(position);
		holder.mImg.setImageResource(R.drawable.pictures_no); // 防止复用的时候忽然从第一屏的图片换成第二屏的图片
		ImageLoader.getInstance(3, Type.LIFO).loadImage(bean.getFirstImgPath(), holder.mImg);
		
		holder.mDirCount.setText(bean.getCount()+""); // 注意将数字转化为文本
		holder.mDirName.setText(bean.getName());
		
		return convertView;
	}
	
	private class ViewHolder {
		ImageView mImg;
		TextView mDirName;
		TextView mDirCount;
	}
	
}
