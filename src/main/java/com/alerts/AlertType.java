package com.alerts;

public enum AlertType {
    // Blood Pressure Alerts
    HIGH_SYSTOLIC_BP,
    LOW_SYSTOLIC_BP,
    HIGH_DIASTOLIC_BP,
    LOW_DIASTOLIC_BP,
    BP_INCREASING_TREND,
    BP_DECREASING_TREND,
    
    // Oxygen Saturation Alerts
    LOW_OXYGEN_SATURATION,
    RAPID_OXYGEN_DROP,
    
    // Mixed Alerts
    HYPOTENSIVE_HYPOXEMIA,
    
    // ECG Alerts
    ECG_ABNORMAL_PEAK,
    
    // Manually Triggered Alerts
    MANUAL_TRIGGER
}

