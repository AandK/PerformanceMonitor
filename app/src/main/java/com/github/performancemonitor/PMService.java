package com.github.performancemonitor;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.github.performancemonitor.util.CpuUtil;
import com.github.performancemonitor.util.JLog;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wangxin on 11/24/16.
 */
public class PMService extends Service {

    private static final String TAG = "PMS";

    private AtomicBoolean mIsServiceDestoryed = new AtomicBoolean(false);

    //不用普通ArrayList的原因是，服务端可能被多个客户端同时绑定, aild方法就被多个binder线程同时执行, 因此要保证线程同步，而CopyOnWriteArrayList已经为我们实现了操作list时的线程同步， 这样调用aidl方法时就不要考虑线程同步的问题了.
//    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<Book>();
    // private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListenerList =
    // new CopyOnWriteArrayList<IOnNewBookArrivedListener>();

    //保存所有客户端的对象，当图书列表发生变化时，可以遍历这个list，调用客户端的方法.
    //RemoteCallbackList是系统提供的专门用于删除跨进程listener的接口.
    //用RemoteCallbackList，而不用ArrayList的原因是, 客户端的对象注册进来后， 服务端会通过它反序列化出一个新的对象保存一起，所以说已经不是同一个对象了. 在客户端调用解除注册方法时， 在list中根本就找不到它的对象， 也就无法从list中删除客户端的对象. 而RemoteCallbackList的内部保存的是客户端对象底层的binder对象, 这个binder对象在客户端对象和反序列化的新对象中是同一个对象,  RemoteCallbackList的实现原理就是利用的这个特性.
    private static RemoteCallbackList<IOnReachHighCPUUsageListener> mListenerList = new RemoteCallbackList<IOnReachHighCPUUsageListener>();

    private Binder mBinder = new IPerformanceMonitor.Stub() {

        @Override
        public void startCpuMonitoring(int pid, int threshold) throws RemoteException {
            JLog.i("remote service pid = "+pid, ", threshold value = " + threshold);
            CpuUtil.startCpuMonitoring(PMService.this, pid, threshold);

        }

        @Override
        public void stopCpuMonitoring() throws RemoteException {
            JLog.i();
            CpuUtil.stopCpuMonitoring();

        }

        //双重安全性检查.
        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            JLog.i();
            //为保护Service不被任意的其他应用绑定, 可以检查客户端是否具有特定的permission.
            //对客户端的permission进行验证, 验证不通过就返回null.
            //需要在服务端的AndroidManifest.xml中, 声明<permission android:name="com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE"/>
            //检查在客户端的AndroidManifest.xml中，是否使用了<uses-permission android:name="com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE">标签
//            int check = checkCallingOrSelfPermission("com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE");
//            Log.d(TAG, "check=" + check);
//            if (check == PackageManager.PERMISSION_DENIED) {
//                return false;
//            }
//
//            //通过getCallingUid()得到客户端的uid， 再通过PackageManager根据uid查到package name进行检查.
//            String packageName = null;
//            String[] packages = getPackageManager().getPackagesForUid(
//                    getCallingUid());
//            if (packages != null && packages.length > 0) {
//                packageName = packages[0];
//            }
//            Log.d(TAG, "onTransact: " + packageName);
//            if (!packageName.startsWith("com.ryg")) {
//                return false;
//            }

            return super.onTransact(code, data, reply, flags);
        }

        @Override
        public void registerListener(IOnReachHighCPUUsageListener listener)
                throws RemoteException {
            mListenerList.register(listener);

            final int N = mListenerList.beginBroadcast();
            mListenerList.finishBroadcast();
            Log.d(TAG, "registerListener, current size:" + N);
            JLog.i("registerListener, current size:" + N);
        }

        @Override
        public void unregisterListener(IOnReachHighCPUUsageListener listener)
                throws RemoteException {
            boolean success = mListenerList.unregister(listener);

            if (success) {
                Log.d(TAG, "unregister success.");
            } else {
                Log.d(TAG, "not found, can not unregister.");
            }
            final int N = mListenerList.beginBroadcast();
            mListenerList.finishBroadcast();
            Log.d(TAG, "unregisterListener, current size:" + N);
            JLog.i("unregisterListener, current size:" + N);
        };

    };

    @Override
    public void onCreate() {
        super.onCreate();
        JLog.i();
        new Thread(new ServiceWorker()).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //为保护Service不被任意的其他应用绑定, 可以检查客户端是否具有特定的permission.
        //对客户端的permission进行验证, 验证不通过就返回null.
        //需要在服务端的AndroidManifest.xml中, 声明<permission android:name="com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE"/>
        //检查在客户端的AndroidManifest.xml中，是否使用了<uses-permission android:name="com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE">标签
        //也可在onTransact()中检查
//        int check = checkCallingOrSelfPermission("com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE");
//        Log.d(TAG, "onbind check=" + check);
//        if (check == PackageManager.PERMISSION_DENIED) {
//            return null;
//        }
        JLog.i();
        return mBinder;//在onBind()方法内把生成类IBookManager.Stub内部类的对象返回给客户端，ServiceConnection类对象的onServiceConnected(ComponentName                 //className, IBinder service)， 把mBinder赋值给IBinder service参数.
    }

    @Override
    public void onDestroy() {
        JLog.i();
        mIsServiceDestoryed.set(true);
        super.onDestroy();
    }

    private class ServiceWorker implements Runnable {
        @Override
        public void run() {
            // do background processing here.....
            while (!mIsServiceDestoryed.get()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                int bookId = mBookList.size() + 1;
//                Book newBook = new Book(bookId, "new book#" + bookId);
//                try {
//                    onNewBookArrived(newBook);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    public static void sendCPUUsageToClient(float cpuUsage) {
        final int N = mListenerList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnReachHighCPUUsageListener l = mListenerList.getBroadcastItem(i);
            if (l != null) {
                try {
                    l.onReachHighCPUUsage((int)cpuUsage);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();
    }

}