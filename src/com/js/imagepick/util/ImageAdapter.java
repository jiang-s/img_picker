package com.js.imagepick.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.js.imagepick.ImageLoader;
import com.js.imagepick.R;
import com.js.imagepick.ImageLoader.Type;

public class ImageAdapter extends BaseAdapter {
	
	private static Set<String> mSelectedImg = new HashSet<String>();

	private String mDirPath;
	private List<String> mImgPaths;
	private LayoutInflater mInflater;

	public ImageAdapter(Context context, List<String> mDatas, String dirPath) {
		this.mDirPath = dirPath;
		this.mImgPaths = mDatas;
		mInflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mImgPaths.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mImgPaths.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder viewHolder;

		// ���û��convertView���xml�����µģ������û����е�����Ч��
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_gridview, parent,
					false);

			viewHolder = new ViewHolder();
			viewHolder.mImg = (ImageView) convertView
					.findViewById(R.id.id_item_image);
			viewHolder.mSelect = (ImageButton) convertView
					.findViewById(R.id.id_item_select);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		// ����״̬���������ͼƬ�����������ô���ٱȽ�����ʱ������ʾһ��ͼƬ��ͼƬ�����������ʾ����ͼƬ
		viewHolder.mImg.setImageResource(R.drawable.pictures_no);
		viewHolder.mSelect.setImageResource(R.drawable.pictures_unselected);
		viewHolder.mImg.setColorFilter(null);

		// ����ͼƬ��ImageView��
		ImageLoader.getInstance(3, Type.LIFO).loadImage(
				mDirPath + "/" + mImgPaths.get(position), viewHolder.mImg);
		final String filePath = mDirPath+"/"+mImgPaths.get(position);
		viewHolder.mImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mSelectedImg.contains(filePath)) {
					// �ѱ�ѡ��
					mSelectedImg.remove(filePath);
					viewHolder.mImg.setColorFilter(null);
					viewHolder.mSelect.setImageResource(R.drawable.pictures_unselected  );
				} else {
					// δ��ѡ��
					mSelectedImg.add(filePath);
					viewHolder.mImg.setColorFilter(Color.parseColor("#77000000"));
					viewHolder.mSelect.setImageResource(R.drawable.pictures_selsectd);
				}
//				notifyDataSetChanged(); // ֪ͨdataset��ˢ���������������������˸�Ϊֻ�޸�viewHolder��״̬
			}
		});
		
		// ͼƬ��ѡ״̬�ļ���
		if(mSelectedImg.contains(filePath)) {
			viewHolder.mImg.setColorFilter(Color.parseColor("#77000000"));
			viewHolder.mSelect.setImageResource(R.drawable.pictures_selsectd);
		}
		return convertView;
	}
	
	private class ViewHolder {
		ImageView mImg;
		ImageButton mSelect;
	}

}
