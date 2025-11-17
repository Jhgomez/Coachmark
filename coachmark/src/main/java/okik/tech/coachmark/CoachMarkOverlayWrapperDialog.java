package okik.tech.coachmark;

import android.graphics.Color;
import android.graphics.RenderNode;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CoachMarkOverlayWrapperDialog extends DialogFragment {

    int[] location;
    int width;
    int height;
    View conent;

    public void configureWrapper(
            int[] location,
            int width,
            int height,
            View conent
    ) {
        this.location = location;
        this.width = width;
        this.height = height;
        this.conent = conent;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
//        WindowCompat.setDecorFitsSystemWindows(requireDialog().getWindow(), false);

        requireDialog().getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);

        requireDialog().getWindow().getAttributes().layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

        requireDialog().getWindow().setLayout(width, height);
//                WindowManager.LayoutParams.MATCH_PARENT

        WindowManager.LayoutParams lp = requireDialog().getWindow().getAttributes();
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = location[0];
        lp.y = location[1];
        requireDialog().getWindow().setAttributes(lp);

        return conent;
    }
}
