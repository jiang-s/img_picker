package com.js.imagepick;

import java.util.List;

import com.js.imagepick.bean.FolderBean;
import com.js.imagepick.util.ListDirAdaper;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

public class ListImageDirPopupWindow extends PopupWindow {

	private int mWidth;
	private int mHeight;
	private View mContentView;
	private ListView mListView;
	private List<FolderBean> mDatas;
	
	public interface OnDirSelectedListener {
		void onSelected(FolderBean folderBean);
	}
	
	public OnDirSelectedListener mListener;
	
	public void setOnDirSelectedListener(OnDirSelectedListener mListener) {
		this.mListener = mListener;
	}

	public ListImageDirPopupWindow(Context context, List<FolderBean> datas) {
		calWidthAndHeight(context);
		
		mContentView = LayoutInflater.from(context).inflate(R.layout.popup_main, null);
		mDatas = datas;
		
		setContentView(mContentView);
		setWidth(mWidth);
		setHeight(mHeight);
		
		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);
		setBackgroundDrawable(new BitmapDrawable());
		
		setTouchInterceptor(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					dismiss();
					return true;
				}
				return false;
			}
		});
		
		initViews(context);
		initEvent();
	}

	private void initViews(Context context) {
		// TODO Auto-generated method stub
		mListView = (ListView) mContentView.findViewById(R.id.id_list_dir);
		mListView.setAdapter(new ListDirAdaper(context, mDatas));
	}

	private void initEvent() {
		// TODO Auto-generated method stub
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(mListener!=null) {
					mListener.onSelected(mDatas.get(position));
				}
			}
		});
	}

	/***
	 * 设置popupWindow的宽度和高度
	 * @param context
	 */
	private void calWidthAndHeight(Context context) {
		// TODO Auto-generated method stub
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		
		mWidth = outMetrics.widthPixels;
		mHeight = (int) (outMetrics.heightPixels * 0.7);
	}
	
}
