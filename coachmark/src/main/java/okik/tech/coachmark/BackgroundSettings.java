package okik.tech.coachmark;

import android.graphics.Paint;
import android.graphics.RecordingCanvas;
import android.graphics.RenderEffect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * This class is intended to be used to configure "BackgroundEffectRenderLayout" instances
 */
public final class BackgroundSettings {

    @Nullable
    private final RenderEffect renderEffect;
    private final boolean shouldClipToBackground;
    private final Drawable backgroundDrawable;
    private final Paint backgroundOverlayPaint;
    private final InnerPadding padding;

    public BackgroundSettings(
            @Nullable RenderEffect renderEffect,
            boolean shouldClipToBackground,
            Drawable backgroundDrawable,
            Paint backgroundOverlayPaint,
            InnerPadding padding
    ) {
        this.renderEffect = renderEffect;
        this.shouldClipToBackground = shouldClipToBackground;
        this.backgroundDrawable = Objects.requireNonNull(backgroundDrawable, "backgroundDrawable");
        this.backgroundOverlayPaint = Objects.requireNonNull(backgroundOverlayPaint, "backgroundOverlayPaint");
        this.padding = Objects.requireNonNull(padding, "padding");
    }

    @Nullable
    public RenderEffect getRenderEffect() {
        return renderEffect;
    }

    public boolean getShouldClipToBackground() {
        return shouldClipToBackground;
    }

    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    public Paint getBackgroundOverlayPaint() {
        return backgroundOverlayPaint;
    }

    public InnerPadding getPadding() {
        return padding;
    }
}
