package com.github.performancemonitor.util;

/**
 * Created by wangxin on 11/24/16.
 */
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.github.performancemonitor.PMService;


public final class CpuUtil {

    private static final int CPU_WINDOW = 1000;

    private static final int CPU_REFRESH_RATE = 1000; // Warning: anything but > 0

    private static HandlerThread handlerThread;

//    private static TestReport output;

//    static {
//        output = new TestReport();
//        output.setDateFormat(Utils.getDateFormat(Utils.DATE_FORMAT_ENGLISH));
//    }

    private static boolean monitorCpu;

    /**;
     * Construct the class singleton. This method should be called in
     * {@link Application#onCreate()}
     *
     * @param dir
     *            the parent directory
     * @param append
     *            mode
     */
/*    public static void setOutput(File dir, boolean append) {
        try {
            File file = new File(dir, "cpu.txt");
            output.setOutputStream(new FileOutputStream(file, append));
            if (!append) {
                output.println(file.getAbsolutePath());
                output.newLine(1);

                // print header
                _printLine(output, "Process", "CPU%");

                output.flush();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }*/

    /** Start CPU monitoring */
    public static boolean startCpuMonitoring(final Context context, final int pid, final int threshold) {
        CpuUtil.monitorCpu = true;

        handlerThread = new HandlerThread("CPU monitoring"); //$NON-NLS-1$
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                while (CpuUtil.monitorCpu) {

                    LinuxUtil linuxUtil = new LinuxUtil();

//                    int pid = android.os.Process.myPid();
                    String pidName = getPidName(pid, context);
                    String cpuStat1 = linuxUtil.readSystemStat();
                    String pidStat1 = linuxUtil.readProcessStat(pid);

                    try {
                        Thread.sleep(CPU_WINDOW);
                    } catch (Exception e) {
                    }

                    String cpuStat2 = linuxUtil.readSystemStat();
                    String pidStat2 = linuxUtil.readProcessStat(pid);

                    float cpu = linuxUtil
                            .getSystemCpuUsage(cpuStat1, cpuStat2);
//                    if (cpu >= 0.0f) {
//                        _printLine(output, "total", Float.toString(cpu));
//                    }
                    JLog.i("OS total: "+cpu);

                    String[] toks = cpuStat1.split(" ");
                    long cpu1 = linuxUtil.getSystemUptime(toks);

                    toks = cpuStat2.split(" ");
                    long cpu2 = linuxUtil.getSystemUptime(toks);

                    cpu = linuxUtil.getProcessCpuUsage(pidStat1, pidStat2,
                            cpu2 - cpu1);
JLog.i("pid : "+pid+", pidName : "+pidName+", cpu : "+cpu);
                    //ahking
                    if (cpu >= threshold) {
JLog.i("read high cpu usage, pid : "+pid+", pidName : "+pidName+", cpu : "+cpu);
                        PMService.sendCPUUsageToClient(cpu);
                    }

                    try {
                        synchronized (this) {
                            wait(CPU_REFRESH_RATE);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }

                Log.i("THREAD CPU", "Finishing");
            }

        });

        return CpuUtil.monitorCpu;
    }

    /** Stop CPU monitoring */
    public static void stopCpuMonitoring() {
        if (handlerThread != null) {
            monitorCpu = false;
            handlerThread.quit();
            handlerThread = null;
        }
    }

    /** Dispose of the object and release the resources allocated for it */
/*    public void dispose() {

        monitorCpu = false;

        if (output != null) {
            OutputStream os = output.getOutputStream();
            if (os != null) {
                Streams.close(os);
                output.setOutputStream(null);
            }

            output = null;
        }
    }*/

/*    private static void _printLine(TestReport output, String process, String cpu) {
        output.stampln(process + ";" + cpu);
    }*/

    private static String getPidName(int pid, Context context) {
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {

                return appProcess.processName;
            }
        }
        return null;
    }
}