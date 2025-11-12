package okik.tech.coachmark;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.TypedValue;

public class ViewUtils {
    public static float dpToPx(short dp, Context context) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    /**
     * We prefer getting drawables like this because drawables defined in XML files can be a little
     * hard to configure
     */
    public static ShapeDrawable dispatchDefaultDrawable(Context context) {
        float n = dpToPx((short) 16, context);

        return new ShapeDrawable(
               new RoundRectShape(
                       new float[]{n, n, n, n, n, n, n, n},
                        null,
                        null
                )
        );
    }
}
