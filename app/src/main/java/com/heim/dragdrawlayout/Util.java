package com.heim.dragdrawlayout;

import android.content.Context;
import android.widget.Toast;

/**
 * 类名:      Util
 * 创建者:    唐杨
 * 创建时间:  2016/8/9.
 * 描述：     TODO
 */
public class Util {

    private static Toast mToast;

    public static void showToast(Context context, String str) {
        if(mToast == null)
            mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);

        mToast.setText(str);
        mToast.show();
    }
}
