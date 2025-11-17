package okik.tech.coachmark;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RenderNode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

public class CoachmarkContainer extends FrameLayout {
    @Nullable
    public CoachMarkOverlay dialogWrapperLayout = null;
    @Nullable
    private final RenderNode contentCopy;
    @Nullable
    private CoachMarkOverlayWrapperDialog coachmarWrapper = null;

    public CoachmarkContainer(Context context) {
        this(context, null);
    }

    public CoachmarkContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentCopy = new RenderNode("ContentCopy");
        } else {
            contentCopy = null;
        }
    }

    public void renderFocusAreaWithDialog(
            FocusArea focusArea,
            FocusDialog focusDialog,
            FragmentManager fm
    ) {
        int[] location = new int[2];
        focusArea.getView().getLocationOnScreen(location);

        int[] selfLocation = new int[2];
        getLocationOnScreen(selfLocation);

        location[0] -= selfLocation[0];
        location[1] -= selfLocation[1];

        dialogWrapperLayout = new CoachMarkOverlay(getContext());
        dialogWrapperLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        int possibleWidth = getResources().getDisplayMetrics().widthPixels - selfLocation[0];
        int possibleHeight = getResources().getDisplayMetrics().heightPixels - selfLocation[1];

        coachmarWrapper = new CoachMarkOverlayWrapperDialog();
        coachmarWrapper.configureWrapper(selfLocation, getWidth(), getHeight(), dialogWrapperLayout);

        coachmarWrapper.show(fm, "cc_cw");

        // Record a copy of child(0) into contentCopy (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && contentCopy != null) {
            contentCopy.setPosition(0, 0, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

            // this custom layout should only have one child and we record its content
            // so we can make a copy of the area we want to focus on
            Canvas contentCopyRecordingCanvas = contentCopy.beginRecording();
            getChildAt(0).draw(contentCopyRecordingCanvas);
            contentCopy.endRecording();
        }

        dialogWrapperLayout.configuredDialog(focusArea, focusDialog, contentCopy, location);

        // Dismiss automatically if this view detaches (e.g., rotation)
        this.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(@NonNull View v) {}
            @Override public void onViewDetachedFromWindow(@NonNull View v) { hideTutorialComponents(); }
        });
    }

    public void hideTutorialComponents() {
        if (coachmarWrapper != null) {
            coachmarWrapper.dismiss();
        }

        coachmarWrapper = null;
    }

    @Override
    protected boolean drawChild(@NonNull Canvas canvas, View child, long drawingTime) {
        // child at 0 should always be the only user-added child to this custom view
        if (getChildAt(0).getId() == child.getId()) {
            // Update dialog background only when something actually changed in child(0)
            if (dialogWrapperLayout != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && contentCopy != null) {
                contentCopy.setPosition(0, 0, getWidth(), getHeight());

                Canvas contentCopyRecordingCanvas = contentCopy.beginRecording();
                boolean isInvalidatedIssued = super.drawChild(contentCopyRecordingCanvas, child, drawingTime);
                contentCopy.endRecording();

                // draw the recorded node onto this canvas
                canvas.drawRenderNode(contentCopy);

                // propagate to dialog/path view
                dialogWrapperLayout.updateBackground(contentCopy);

                return isInvalidatedIssued;
            }
        }

        // Default behavior if no focus area specified or pre-S
        return super.drawChild(canvas, child, drawingTime);
    }
}
