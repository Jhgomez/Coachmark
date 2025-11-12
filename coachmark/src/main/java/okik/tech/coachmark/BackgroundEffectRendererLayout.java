package okik.tech.coachmark;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RecordingCanvas;
import android.graphics.RenderNode;
import android.graphics.RenderEffect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import kotlin.Unit;


public class BackgroundEffectRendererLayout extends FrameLayout {

    private Paint paint = new Paint();
    @Nullable private BackgroundSettings backgroundSettings = null;

    private final RenderNode blurNode;
    @Nullable public RenderNode backgroundViewRenderNode = null;
    @Nullable private Drawable fallBackDrawable = null;

    public BackgroundEffectRendererLayout(Context context) {
        super(context);
    }

    public BackgroundEffectRendererLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blurNode = new RenderNode("BlurView node");
        } else {
            blurNode = null;
        }
    }

    public void clonePaintToBackgroundDrawable(Paint source) {
        this.paint.setAlpha(source.getAlpha());
        this.paint.setStyle(source.getStyle());
        this.paint.setStrokeWidth(source.getStrokeWidth());
        this.paint.setAntiAlias(true);
        this.paint.setColor(source.getColor());
    }

    /**
     * Configure this FrameLayout's background drawable (the "helper view" in the original docs).
     * If the drawable is a ShapeDrawable, its Paint is used to draw the overlay.
     */
    public void configureDrawableAsBackground(Drawable drawable) {
        setBackground(drawable);
        if (drawable instanceof ShapeDrawable) {
            this.paint = ((ShapeDrawable) drawable).getPaint();
        }
    }

    public void clipToBackground() {
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                Drawable bg = getBackground();
                if (bg != null) {
                    bg.getOutline(outline);
                    outline.setAlpha(1f);
                }
            }
        });
    }

    /**
     * Applies padding to the background drawable only (not to this view's content).
     * Caller is responsible for applying content padding to this container if needed.
     */
    public void setPaddingToBackgroundDrawable(int top, int bottom, int start, int end) {
        Drawable backgroundDrawable = getBackground();
        setBackground(null);

        if (backgroundDrawable instanceof InsetDrawable) {
            InsetDrawable inset = (InsetDrawable) backgroundDrawable;
            setBackground(new InsetDrawable(inset.getDrawable(), start, top, end, bottom));
        } else if (backgroundDrawable != null) {
            setBackground(new InsetDrawable(backgroundDrawable, start, top, end, bottom));
        }
    }

    public void setBackgroundConfigs(
            BackgroundSettings backgroundSettings,
            RenderNode backgroundViewRenderNode
    ) {
        setBackgroundRenderNode(backgroundViewRenderNode);
        setBackgroundConfigs(backgroundSettings);
    }

    public void setBackgroundConfigs(BackgroundSettings backgroundSettings) {
        this.backgroundSettings = backgroundSettings;
        setWillNotDraw(false);

        configureDrawableAsBackground(backgroundSettings.getBackgroundDrawable());

        setPaddingToBackgroundDrawable(
                (int) backgroundSettings.getPadding().getTop(),
                (int) backgroundSettings.getPadding().getBottom(),
                (int) backgroundSettings.getPadding().getStart(),
                (int) backgroundSettings.getPadding().getEnd()
        );

        if (backgroundSettings.getShouldClipToBackground()) {
            clipToBackground();
        }

        clonePaintToBackgroundDrawable(backgroundSettings.getBackgroundOverlayPaint());

        // If we are NOT clipping to background, apply the effect to this view’s drawing.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S
                && !backgroundSettings.getShouldClipToBackground()) {
            setRenderEffect(backgroundSettings.getRenderEffect());
        }
    }

    public void setBackgroundRenderNode(RenderNode backgroundViewRenderNode) {
        this.backgroundViewRenderNode = backgroundViewRenderNode;
    }

    @Override
    public void draw(Canvas canvas) {
        if (backgroundSettings != null && backgroundViewRenderNode != null) {
            drawBackgroundRenderNode(canvas);
        }
        super.draw(canvas);
    }

    private void drawBackgroundRenderNode(Canvas canvas) {
        blurNode.setPosition(0, 0, getWidth(), getHeight());
        recordBackgroundViews();
        // Draw on the system canvas
        canvas.drawRenderNode(blurNode);
    }

    private void recordBackgroundViews() {
        RecordingCanvas recordingCanvas = blurNode.beginRecording();

        if (fallBackDrawable != null) {
            fallBackDrawable.draw(recordingCanvas);
        }

        if (backgroundSettings != null && backgroundSettings.getShouldClipToBackground()) {
            blurNode.setRenderEffect(backgroundSettings.getRenderEffect());
        }

        recordingCanvas.translate(-getLeft(), -getTop());

        if (backgroundViewRenderNode != null) {
            recordingCanvas.drawRenderNode(backgroundViewRenderNode);
        }

        blurNode.endRecording();
    }

    public void setFallbackBackground(@Nullable Drawable frameClearDrawable) {
        this.fallBackDrawable = frameClearDrawable;
    }
}
