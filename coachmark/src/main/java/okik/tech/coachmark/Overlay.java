package okik.tech.coachmark;


import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RecordingCanvas;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.ColorUtils;

/**
 * A ConstraintLayout that wraps a dialog view inside a rounded/backgroundable container.
 * Unlike MaterialCardView, this lets you apply background effects (blur, etc.) without
 * affecting the foreground content by drawing an effect-holding view behind it.
 */

class Overlay extends View {
    public FocusArea focusArea = null;
    int[] emphasisViewLoc;
    @Nullable private RenderNode contentCopy = null;
    @Nullable private RenderEffect renderEffect = null;
    private final RenderNode contentWithEffect;
    private int overlayColor = Color.TRANSPARENT;

    {
        setBackgroundColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentWithEffect = new RenderNode("BlurredContent");
        } else {
            contentWithEffect = null;
        }
    }

    public Overlay(Context context) {
        this(context, null);
    }

    public Overlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public void configureOverlay(
            @Nullable RenderNode contentCopy,
            @Nullable RenderEffect renderEffect,
            int color,
            short alpha
    ) {
        this.contentCopy = contentCopy;
        this.renderEffect = renderEffect;
        overlayColor = ColorUtils.setAlphaComponent(color, alpha);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                canvas.drawColor(overlayColor);
            }

            if (contentCopy != null) {
                // Create a version of the original view that lives in "contentCopy" and apply the effect
                if (contentWithEffect != null) {
                    contentWithEffect.setRenderEffect(renderEffect);
                    contentWithEffect.setPosition(0, 0, getWidth(), getHeight());

                    RecordingCanvas contentWithEffectRecordingCanvas = contentWithEffect.beginRecording();
                    contentWithEffectRecordingCanvas.drawRenderNode(contentCopy);
                    contentWithEffectRecordingCanvas.drawColor(overlayColor);
                    contentWithEffect.endRecording();

                    // Draw the applied effect to content on this layout's canvas
                    canvas.drawRenderNode(contentWithEffect);
                }
            }

        super.draw(canvas);
    }
}
