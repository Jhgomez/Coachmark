package okik.tech.coachmark;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RecordingCanvas;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.function.BiConsumer;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class RenderNodeBehindPathView extends View {

    private Paint paint = new Paint();
    @Nullable private RenderEffect renderEffect = null;

    private boolean setEffectOnBackgroundOnly = true;
    private boolean shouldClipPath = true;

    private final RenderNode blurNode;
    @Nullable private RenderNode backgroundViewRenderNode = null;
    @Nullable private Drawable fallBackDrawable = null;

    @Nullable private Path path = null;

    public RenderNodeBehindPathView(Context context) {
        this(context, null);
    }

    public RenderNodeBehindPathView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blurNode = new RenderNode("UnderlyingView");
        } else {
            blurNode = null;
        }
    }

    /** If no background to the effect holder was set then this won't change the view in any way. */
    public void setPathPaint(Paint paint) {
        this.paint = paint;
    }

    public void setBackgroundViewRenderNode(@Nullable RenderNode renderNode) {
        this.backgroundViewRenderNode = renderNode;
    }

    public void setBackgroundConfigs(
            RenderNode backgroundViewRenderNode,
            Path path,
            Paint pathPaint,
            boolean shouldClipPath,
            boolean setEffectOnBackgroundOnly,
            @Nullable RenderEffect renderEffect
    ) {
        this.setEffectOnBackgroundOnly = setEffectOnBackgroundOnly;
        this.backgroundViewRenderNode = backgroundViewRenderNode;
        this.path = path;
        this.shouldClipPath = shouldClipPath;
        setWillNotDraw(false);

        this.paint = pathPaint;
        this.renderEffect = renderEffect;

        // If we should NOT clip to background, the effect is applied to this View's drawing.
        if (!setEffectOnBackgroundOnly && backgroundViewRenderNode != null) {
            setRenderEffect(renderEffect);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (path != null) {
            if (shouldClipPath) {
                canvas.clipPath(path);
            }

            if (blurNode != null) {
                drawBackgroundRenderNode(canvas);
            }

            canvas.drawPath(path, paint);
        }

        // super.draw(canvas);
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

        if (setEffectOnBackgroundOnly) {
            blurNode.setRenderEffect(renderEffect);
        }

        // Position the recording canvas relative to this view / target
//        renderCanvasPositionCommand.accept(recordingCanvas, this);

        if (backgroundViewRenderNode != null) {
            recordingCanvas.drawRenderNode(backgroundViewRenderNode);
        }

        blurNode.endRecording();
    }

    public void setFallbackBackground(@Nullable Drawable frameClearDrawable) {
        this.fallBackDrawable = frameClearDrawable;
    }
}
