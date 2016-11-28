package com.github.performancemonitor;

/**
 * Created by wangxin on 11/28/16.
 */
public class MonitorPerformanceHelper {
}

/*





package com.qihoo.browser.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.github.performancemonitor.IOnReachHighCPUUsageListener;
import com.github.performancemonitor.IPerformanceMonitor;

*/
/**
 * Created by wangxin on 11/24/16.
 *//*


*/
/*客户端代码*//*


*/
/* How To Use
        开始检测:
        MonitorPerformanceHelper m = MonitorPerformanceHelper.getInstance(Global.mContext);
        m.bindPM();

        停止检测:
        MonitorPerformanceHelper.getInstance(Global.mContext).unbindPM();
*//*


*/
/* Log Output
I/ahking  (15965): [ (CpuUtil.java:95)#Run ] OS total: 42.26804
I/ahking  (15965): [ (CpuUtil.java:105)#Run ] pid : 10026, pidName : com.qihoo.browser, cpu : 7.3170733

I/ahking  (15965): [ (CpuUtil.java:95)#Run ] OS total: 53.14136
I/ahking  (15965): [ (CpuUtil.java:105)#Run ] pid : 10026, pidName : com.qihoo.browser, cpu : 28.125
I/ahking  (15965): [ (CpuUtil.java:108)#Run ] reach high cpu usage, pid : 10026, pidName : com.qihoo.browser, cpu : 28.125
I/ahking  (10026): [ (MonitorPerformanceHelper.java:50)#OnReachHighCPUUsage ] browser received high cpu usage detection, usage = 28
*//*

public class MonitorPerformanceHelper {
    private static MonitorPerformanceHelper mInstance = null;
    private Context mContext = null;
    private ServiceConnection mConnection = null;
    IPerformanceMonitor mRemotePerformanceMonitor;

    private int pid = android.os.Process.myPid();
    private int threshold = 20;

    private MonitorPerformanceHelper(Context context) {
        mContext = context.getApplicationContext();
        init();
    }

    public static MonitorPerformanceHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MonitorPerformanceHelper(context);
        }
        return mInstance;
    }

    private void init() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                JLog.i();
                IPerformanceMonitor pm = IPerformanceMonitor.Stub.asInterface(service);
                mRemotePerformanceMonitor = pm;

                try {
                    mRemotePerformanceMonitor.registerListener(new IOnReachHighCPUUsageListener.Stub() {
                        @Override
                        public void onReachHighCPUUsage(int usage) throws RemoteException {
// we can dump every thread stacktrace, to see what's going on when browser's take high cpu usage.
                            JLog.i("browser received high cpu usage detection, usage = " + usage);
                        }
                    });

                    mRemotePerformanceMonitor.startCpuMonitoring(pid, threshold);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                JLog.i();
            }
        };


    }

    public void bindPM() {
        Intent intent = new Intent("com.github.performancemonitor.bindService");
        boolean b = mContext.bindService(intent, mConnection, mContext.BIND_AUTO_CREATE);
        JLog.i(b);
    }

    public void unbindPM() {
        if (mRemotePerformanceMonitor != null) {
            try {
                mRemotePerformanceMonitor.stopCpuMonitoring();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mContext.unbindService(mConnection);
    }
}
*/
