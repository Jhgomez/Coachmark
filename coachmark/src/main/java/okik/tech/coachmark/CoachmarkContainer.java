package okik.tech.coachmark;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RenderNode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CoachmarkContainer extends FrameLayout {
    @Nullable
    public CoachMarkOverlay dialogWrapperLayout = null;
    @Nullable
    private final RenderNode contentCopy;
    @Nullable
    private PopupWindow popup = null;

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

    public void renderFocusAreaWithDialog(FocusArea focusArea, FocusDialog focusDialog) {
        int[] location = new int[2];
        focusArea.getView().getLocationOnScreen(location);

        int[] selfLocation = new int[2];
        getLocationOnScreen(selfLocation);

        location[0] -= selfLocation[0];
        location[1] -= selfLocation[1];

        dialogWrapperLayout = new CoachMarkOverlay(getContext());

        popup = new PopupWindow(
                dialogWrapperLayout,
                getWidth(),
                getHeight(),
                true // closes on outside touch if true
        );

        popup.showAtLocation(this, Gravity.NO_GRAVITY, selfLocation[0], selfLocation[1]);
        // Record a copy of child(0) into contentCopy (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && contentCopy != null) {
            contentCopy.setPosition(0, 0, getWidth(), getHeight());

            // this custom layout should only have one child and we record its content
            // so we can make a copy of the area we want to focus on
            Canvas contentCopyRecordingCanvas = contentCopy.beginRecording();
            getChildAt(0).draw(contentCopyRecordingCanvas);
            contentCopy.endRecording();
        }

        dialogWrapperLayout.configuredDialog(focusArea, focusDialog, contentCopy, location);

        Overlay overlay = new Overlay(getContext());
        overlay.layout(0, 0, getWidth(), getHeight());
        overlay.configureOverlay(
                contentCopy,
                focusArea.getOuterAreaEffect(),
                focusArea.getOverlayColor(),
                focusArea.getOverlayAlpha()
        );
        getOverlay().add(overlay);

        // Dismiss automatically if this view detaches (e.g., rotation)
        View.OnAttachStateChangeListener oascl = new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(@NonNull View v) {}
            @Override public void onViewDetachedFromWindow(@NonNull View v) { hideTutorialComponents(); }
        };

        this.addOnAttachStateChangeListener(oascl);

        popup.setOnDismissListener(() -> {
            popup = null;
            getOverlay().remove(overlay);
            removeOnAttachStateChangeListener(oascl);
        });
    }

    public void hideTutorialComponents() {
        if (popup != null && popup.isShowing()) {
            popup.dismiss();
        }

        popup = null;
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
