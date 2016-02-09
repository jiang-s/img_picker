package com.js.imagepick;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.js.imagepick.ListImageDirPopupWindow.OnDirSelectedListener;
import com.js.imagepick.bean.FolderBean;
import com.js.imagepick.util.ImageAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private GridView mGridView;
	private List<String> mImgs;
	private ImageAdapter mImgAdapter;

	private RelativeLayout mBottomLy;
	private TextView mDirName;
	private TextView mDirCount;

	private File mCurrentDir;
	private int mMaxCount;

	private ProgressDialog mProgressDialog;

	private List<FolderBean> mFolderBeans = new ArrayList<FolderBean>();

	private final static int DATA_LOADED = 0x110;

	private ListImageDirPopupWindow mDirPopupWindow;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == DATA_LOADED) {
				mProgressDialog.dismiss();
				// 绑定数据到View中
				data2View();

				initDirPopupWindow();
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
		initDatas();
		initEvent();
	}

	protected void initDirPopupWindow() {
		// TODO Auto-generated method stub
		mDirPopupWindow = new ListImageDirPopupWindow(this, mFolderBeans);
		mDirPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				lightOn();
			}

		});

		mDirPopupWindow.setOnDirSelectedListener(new OnDirSelectedListener() {

			@Override
			public void onSelected(FolderBean folderBean) {
				// TODO Auto-generated method stub
				mCurrentDir = new File(folderBean.getDir());
				mImgs = Arrays.asList(mCurrentDir.list(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						// TODO Auto-generated method stub
						if (filename.endsWith(".jpg")
								|| filename.endsWith(".jpeg")
								|| filename.endsWith(".png")) {
							return true;
						}
						return false;
					}

				}));

				mImgAdapter = new ImageAdapter(MainActivity.this, mImgs,
						mCurrentDir.getAbsolutePath());
				// 可以不new adapter，而是更新之后notify changes。这里是为了方便
				mGridView.setAdapter(mImgAdapter);

				mDirCount.setText(mImgs.size() + "");
				mDirName.setText(folderBean.getName());
				
				mDirPopupWindow.dismiss();
			}
		});
		
	}

	protected void lightOn() {
		// TODO Auto-generated method stub
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 1.0f;
		getWindow().setAttributes(lp);
	}

	protected void lightOff() {
		// TODO Auto-generated method stub
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 0.3f;
		getWindow().setAttributes(lp);
	}

	protected void data2View() {
		// TODO Auto-generated method stub
		if (mCurrentDir == null) {
			Toast.makeText(this, "未扫描到任何图片", Toast.LENGTH_SHORT).show();
			return;
		}

		mImgs = Arrays.asList(mCurrentDir.list());
		mImgAdapter = new ImageAdapter(this, mImgs,
				mCurrentDir.getAbsolutePath());
		mGridView.setAdapter(mImgAdapter);

		mDirCount.setText(mMaxCount + ""); // 不要传int否则会去寻找资源
		mDirName.setText(mCurrentDir.getName());
	}

	private void initEvent() {
		// TODO Auto-generated method stub
		mBottomLy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mDirPopupWindow.setAnimationStyle(R.style.dir_popupwindow_anim);
				mDirPopupWindow.showAsDropDown(mBottomLy, 0, 0);
				lightOff();

			}
		});

	}

	/***
	 * 利用ContentProvider扫描手机中的所以图片
	 */
	private void initDatas() {
		// TODO Auto-generated method stub
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "当前存储卡不可用", Toast.LENGTH_SHORT).show();
			return;
		}

		mProgressDialog = ProgressDialog.show(this, null, "正在加载...");
		new Thread() {

			@Override
			public void run() {
				Uri uImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver cr = MainActivity.this.getContentResolver();
				Cursor cursor = cr.query(uImgUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[] { "image/jpeg", "image/png" },
						MediaStore.Images.Media.DATE_MODIFIED);
				Set<String> mDirPaths = new HashSet<String>();

				while (cursor.moveToNext()) {
					String path = cursor.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					File parentFile = new File(path).getParentFile();
					if (parentFile == null) {
						continue;
					}
					// 屏蔽节奏大师的图片。。实在太多了
					if (parentFile.getAbsolutePath().substring(0, 19).equals("/storage/sdcard0/RM")) {
						continue;
					}
					String dirPath = parentFile.getAbsolutePath();
					FolderBean folderBean = null;
					if (mDirPaths.contains(dirPath)) {
						continue;
					} else {
						mDirPaths.add(dirPath);
						folderBean = new FolderBean();
						folderBean.setDir(dirPath);
						folderBean.setFirstImgPath(path);
					}
					if (parentFile.list() == null) {
						continue;
					}
					int picSize = parentFile.list(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String filename) {
							// TODO Auto-generated method stub
							if (filename.endsWith(".jpg")
									|| filename.endsWith(".jpeg")
									|| filename.endsWith(".png")) {
								return true;
							}
							return false;
						}

					}).length;

					folderBean.setCount(picSize);
					mFolderBeans.add(folderBean);
					if (picSize > mMaxCount) {
						mMaxCount = picSize;
						mCurrentDir = parentFile;
					}
				}

				cursor.close();
				
				// 对文件夹的名称进行排序
				Collections.sort(mFolderBeans);
				
				// 通知Handler扫描图片完成
				mHandler.sendEmptyMessage(DATA_LOADED);
			}

		}.start();

	}

	private void initView() {
		// TODO Auto-generated method stub
		mGridView = (GridView) findViewById(R.id.id_gridView);
		mBottomLy = (RelativeLayout) findViewById(R.id.id_bottom_layout);
		mDirName = (TextView) findViewById(R.id.id_dir_name);
		mDirCount = (TextView) findViewById(R.id.id_dir_count);
	}

}
