package com.example.barbershop.util;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class UIHelper {
    
    /**
     * Shows a toast message with a longer duration for longer messages
     */
    public static void showToast(Context context, String message) {
        if (context == null || message == null) return;
        
        int duration = message.length() > 50 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(context, message, duration).show();
    }
    
    /**
     * Shows a toast message with a custom duration
     */
    public static void showToastWithDuration(Context context, String message, int durationMs) {
        if (context == null || message == null) return;
        
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
        
        Handler handler = new Handler();
        handler.postDelayed(toast::cancel, durationMs);
    }
} 