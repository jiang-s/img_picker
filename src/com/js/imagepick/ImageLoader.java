package com.js.imagepick;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ImageLoader {
	
	private static ImageLoader mInstance;
	/***
	 * ͼƬ����ĺ��Ķ���
	 */
	private LruCache<String, Bitmap> mLruCache;
	/***
	 * �̳߳�
	 */
	private ExecutorService mThreadPool;
	private static int DEFAULT_THREAD_COUNT = 1;
	/***
	 * ���еĵ��ȷ�ʽ
	 */
	private Type mType = Type.LIFO;
	/***
	 * �������
	 */
	private LinkedList<Runnable> mTaskQueue;
	/***
	 * ��̨��ѯ�߳�
	 */
	private Thread mPoolThread;
	private Handler mPoolThreadHandler;
	/***
	 * UI�̵߳�Handler
	 */
	private Handler mUIHandler;
	public enum Type {
		FIFO, LIFO;
	}

	private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
	private Semaphore mSemaphoreThreadPool;
	
	private ImageLoader(int threadCount, Type type) {
		init(threadCount, type);
	}
	
	/***
	 * ��ʼ��
	 * @param threadCount
	 * @param type
	 */
	private void init(int threadCount, Type type) {
		// ��̨��ѯ�߳�
		mPoolThread = new Thread(){
			
			@Override
			public void run() {
				Looper.prepare();
				mPoolThreadHandler = new Handler() {
					@Override
					public void handleMessage(Message messgae) {
						// ���̳߳�ȡ��һ���߳�ִ��
						mThreadPool.execute(getTask());
						try {
							mSemaphoreThreadPool.acquire();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				mSemaphorePoolThreadHandler.release();
				Looper.loop();
			}
		};
		
		mPoolThread.start();
		
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheMemory = maxMemory / 8;
		mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
		
		// �����̳߳�
		mThreadPool = Executors.newFixedThreadPool(threadCount);
		mTaskQueue = new LinkedList<Runnable>();
		mType = type;
		
		mSemaphoreThreadPool = new Semaphore(threadCount);
		
	}
	/***
	 * ���������ȡ��һ������
	 * @return
	 */
	private Runnable getTask() {
		// TODO Auto-generated method stub
		if(mType == Type.FIFO) {
			return mTaskQueue.removeFirst();
		} else if(mType == Type.LIFO) {
			return mTaskQueue.removeLast();
		}
		return null;
	}
	
	private static ImageLoader getInstance() {
		if(mInstance == null) {
			synchronized (ImageLoader.class) {
				if(mInstance == null) {
					mInstance = new ImageLoader(DEFAULT_THREAD_COUNT, Type.LIFO);
				}
 			}
		}
		return mInstance;
	}
	
	public static ImageLoader getInstance(int threadCount, Type type) {
		if(mInstance == null) {
			synchronized (ImageLoader.class) {
				if(mInstance == null) {
					mInstance = new ImageLoader(threadCount, type);
				}
 			}
		}
		return mInstance;
	}
	
	/***
	 * ΪimageView����ͼƬ
	 * @param path
	 * @param imageView
	 */
	public void loadImage(final String path, final ImageView imageView) {
		imageView.setTag(path);
		
		if(mUIHandler == null) {
			mUIHandler = new Handler(){
				@Override
				public void handleMessage(Message message) {
					// ��ȡ�õ���ͼƬ��ΪimageView�ص�����ͼƬ
					ImageBeanHolder holder = (ImageBeanHolder) message.obj;
					Bitmap bm = holder.bitmap;
					ImageView imageView = holder.imageView;
					String path = holder.path;
					
					if(imageView.getTag().equals(path)) {
						imageView.setImageBitmap(bm);
					}
				}
			};
		}
		
		Bitmap bm = getBitmapFromLruCache(path);
		if(bm != null) {
			refeshBitmap(path, imageView, bm);
		} else {
			// cache��û���õ�ͼƬ
			addTasks(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					// ����ͼƬ
					// ͼƬ��ѹ��
					// 1. ���ͼƬ��Ҫ��ʾ�Ĵ�С
					ImageSize imageSize = getImageViewSize(imageView);
					
					// 2. ѹ��ͼƬ
					Bitmap bm = decodeSampleBitmapFromPath(path, imageSize.width, imageSize.height);
					
					// 3. ��ͼƬ���뵽����
					addBitmapToLruCache(path, bm);
					
					refeshBitmap(path, imageView, bm);
					
					mSemaphoreThreadPool.release();
				}
			});
		}
	}

	private void refeshBitmap(final String path,
			final ImageView imageView, Bitmap bm) {
		Message message = Message.obtain();
		ImageBeanHolder holder = new ImageBeanHolder();
		holder.bitmap = bm;
		holder.imageView = imageView;
		holder.path = path;
		message.obj = holder;
		mUIHandler.sendMessage(message);
	}
	
	/***
	 * ��ͼƬ����LruCache
	 * @param path
	 * @param bm
	 */
	protected void addBitmapToLruCache(String path, Bitmap bm) {
		// TODO Auto-generated method stub
		if(getBitmapFromLruCache(path) == null) {
			if(bm != null) {
				mLruCache.put(path, bm);
			}
		}
	}

	protected Bitmap decodeSampleBitmapFromPath(String path, int width, int height) {
		// TODO Auto-generated method stub
		BitmapFactory.Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		options.inSampleSize = calculateInSampleSize(options, width, height);
		
		// ʹ�û�ȡ����inSampleSize �ٴν���ͼƬ
		options.inJustDecodeBounds = false; // �����ڴ�
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}

	/***
	 * ����ͼƬ�Ŀ�͸�һ��ͼƬʵ�ʵĿ�͸߼���SampleSize
	 * @param options
	 * @param width
	 * @param height
	 * @return
	 */
	private int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
		// TODO Auto-generated method stub
		int width = options.outWidth;
		int height = options.outHeight;
		
		int inSampleSize = 1;
		
		if(width > reqWidth || height > reqHeight) {
			int widthRadio = Math.round(width * 1.0f / reqWidth);
			int heightRadio = Math.round(height * 1.0f / reqHeight);
			
			inSampleSize = Math.max(widthRadio, heightRadio);
		}
		return inSampleSize;
	}

	protected ImageSize getImageViewSize(ImageView imageView) {
		// TODO Auto-generated method stub
		ImageSize imageSize = new ImageSize();
		
		DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
		LayoutParams lp = imageView.getLayoutParams();
		
		int width = imageView.getWidth();
		if(width <= 0) {
			width = lp.width;
		}
		if(width <= 0) {
			width = imageView.getMaxWidth();
		}
		if(width <= 0) {
			width = displayMetrics.widthPixels;
		}
		
		int height = imageView.getHeight();
		if(height <= 0) {
			height = lp.height;
		}
		if(height <= 0) {
			height = imageView.getMaxHeight();
		}
		if(height <= 0) {
			height = displayMetrics.heightPixels;
		}
		
		imageSize.width = width;
		imageSize.height = height;
		return imageSize;
	}
	
	private synchronized void addTasks(Runnable runnable) {
		// TODO Auto-generated method stub
		mTaskQueue.add(runnable);
		try {
			if(mPoolThreadHandler == null)
				mSemaphorePoolThreadHandler.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mPoolThreadHandler.sendEmptyMessage(DEFAULT_THREAD_COUNT);
	}

	private class ImageBeanHolder {
		Bitmap bitmap;
		ImageView imageView;
		String path;
	}
	
	private class ImageSize {
		int width;
		int height;
	}

	private Bitmap getBitmapFromLruCache(String key) {
		// TODO Auto-generated method stub
		return mLruCache.get(key);
	}

}
