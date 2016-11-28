// IPerformanceMonitor.aidl
package com.github.performancemonitor;

// Declare any non-default types here with import statements
import com.github.performancemonitor.IOnReachHighCPUUsageListener;


interface IPerformanceMonitor {
    void startCpuMonitoring(int pid, int threshold);
    void stopCpuMonitoring();

    void registerListener(IOnReachHighCPUUsageListener listener);
    void unregisterListener(IOnReachHighCPUUsageListener listener);
}
