package okik.tech.coachmark;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RenderEffect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.WindowInsetsCompat;

import java.util.function.Supplier;

public final class FocusArea {

    private final View view;
    private final SurroundingThickness surroundingThickness;
    @Nullable private final RenderEffect surroundingThicknessEffect;
    private final Paint surroundingAreaPaint;
    private final InnerPadding surroundingAreaPadding;
    private final Supplier<Drawable> surroundingAreaBackgroundDrawableFactory;
    private final boolean shouldClipToBackground;
    @Nullable private final RenderEffect outerAreaEffect;
    private final int overlayColor;
    private final short overlayAlpha;

    private FocusArea(
            View view,
            SurroundingThickness surroundingThickness,
            @Nullable RenderEffect surroundingThicknessEffect,
            Paint surroundingAreaPaint,
            InnerPadding surroundingAreaPadding,
            Supplier<Drawable> surroundingAreaBackgroundDrawableFactory,
            boolean shouldClipToBackground,
            @Nullable RenderEffect outerAreaEffect,
            int overlayColor,
            short overlayAlpha
    ) {
        this.view = view;
        this.surroundingThickness = surroundingThickness;
        this.surroundingThicknessEffect = surroundingThicknessEffect;
        this.surroundingAreaPaint = surroundingAreaPaint;
        this.surroundingAreaPadding = surroundingAreaPadding;
        this.surroundingAreaBackgroundDrawableFactory = surroundingAreaBackgroundDrawableFactory;
        this.shouldClipToBackground = shouldClipToBackground;
        this.outerAreaEffect = outerAreaEffect;
        this.overlayColor = overlayColor;
        this.overlayAlpha = overlayAlpha;
    }

    public View getView() { return view; }
    public SurroundingThickness getSurroundingThickness() { return surroundingThickness; }
    @Nullable public RenderEffect getSurroundingThicknessEffect() { return surroundingThicknessEffect; }
    public Paint getSurroundingAreaPaint() { return surroundingAreaPaint; }
    public InnerPadding getSurroundingAreaPadding() { return surroundingAreaPadding; }
    public Supplier<Drawable> getSurroundingAreaBackgroundDrawableFactory() { return surroundingAreaBackgroundDrawableFactory; }
    public boolean getShouldClipToBackground() { return shouldClipToBackground; }
    @Nullable public RenderEffect getOuterAreaEffect() { return outerAreaEffect; }
    public int getOverlayColor() { return overlayColor; }
    public short getOverlayAlpha() { return overlayAlpha; }

    /** Kotlin data class inside FocusArea. */
    public static final class SurroundingThickness {
        private final float top;
        private final float bottom;
        private final float start;
        private final float end;

        public SurroundingThickness(float top, float bottom, float start, float end) {
            this.top = top;
            this.bottom = bottom;
            this.start = start;
            this.end = end;
        }

        public float getTop() { return top; }
        public float getBottom() { return bottom; }
        public float getStart() { return start; }
        public float getEnd() { return end; }
    }

    /**
     * Convenience to produce the background settings consumed by BackgroundEffectRendererLayout.
     * NOTE: BackgroundSettings is assumed to be in your project (same as in Kotlin).
     */
    public BackgroundSettings generateBackgroundSettings() {
        return new BackgroundSettings(
                surroundingThicknessEffect,
                shouldClipToBackground,
                surroundingAreaBackgroundDrawableFactory.get(),
                surroundingAreaPaint,
                surroundingAreaPadding
        );
    }

    public FocusDialog.Builder generateMatchingFocusDialog() {

        return new FocusDialog.Builder()
                .setPathViewPathPaint(surroundingAreaPaint)
                .setPathBackgroundRenderEffect(surroundingThicknessEffect);
    }

    public static final class Builder {
        private @Nullable View view;
        private @Nullable RenderEffect surroundingThicknessEffect;
        private @Nullable Paint surroundingAreaPaint;
        private @Nullable Supplier<Drawable> surroundingAreaBackgroundDrawableFactory;
        private boolean shouldClipToBackground = true;
        private @Nullable RenderEffect outerAreaEffect;
        private int overlayColor = Color.TRANSPARENT;
        private short overlayAlpha = 125;

        private short thickTop = 0;
        private short thickBottom = 0;
        private short thickStart = 0;
        private short thickEnd = 0;

        private short padTop = 0;
        private short padBottom = 0;
        private short padStart = 0;
        private short padEnd = 0;

        public Builder setView(View view) {
            this.view = view;
            return this;
        }

        /** Values are DP (converted to PX internally). */
        public Builder setSurroundingThickness(short top, short bottom, short start, short end) {
            this.thickTop = top;
            this.thickBottom = bottom;
            this.thickStart = start;
            this.thickEnd = end;
            return this;
        }

        @RequiresApi(Build.VERSION_CODES.S)
        public Builder setSurroundingThicknessEffect(RenderEffect effect) {
            this.surroundingThicknessEffect = effect;
            return this;
        }

        @RequiresApi(Build.VERSION_CODES.S)
        public Builder setOuterAreaEffect(@Nullable RenderEffect outer) {
            this.outerAreaEffect = outer;
            return this;
        }

        public Builder setOuterAreaOverlayColor(int color) {
            this.overlayColor = color;
            return this;
        }

        /** 0..255 where 0 = transparent, 255 = opaque. */
        public Builder setOuterAreaOverlayAlpha(short alpha) {
            this.overlayAlpha = alpha;
            return this;
        }

        public Builder setSurroundingAreaPaint(Paint paint) {
            this.surroundingAreaPaint = paint;
            return this;
        }

        /**
         * Using paddings when clip-to-background is true may be slightly buggy at the moment.
         * Prefer different thickness values per edge in that case.
         */
        public Builder setSurroundingAreaPadding(short top, short bottom, short start, short end) {
            this.padTop = top;
            this.padBottom = bottom;
            this.padStart = start;
            this.padEnd = end;
            return this;
        }

        /**
         * Prefer runtime-created (shape) drawables. XML drawables can be trickier to configure for this use.
         */
        public Builder setSurroundingAreaBackgroundDrawableFactory(Supplier<Drawable> factory) {
            this.surroundingAreaBackgroundDrawableFactory = factory;
            return this;
        }

        public Builder setShouldClipToBackground(boolean shouldClipToBackground) {
            this.shouldClipToBackground = shouldClipToBackground;
            return this;
        }

        public FocusArea build() {
            if (view == null) throw new IllegalStateException("view can't be null");

//            if (viewLocation == null) {
//                viewLocation = new int[] { 0, 0 };
//                view.getLocationOnScreen(viewLocation);
//
//                WindowInsetsCompat insets = WindowInsetsCompat.toWindowInsetsCompat(view.getRootWindowInsets());
//
//                int navBarLeft = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars()).left;
//                viewLocation[0] -= navBarLeft;
//
//                int topBarHeight = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars()).top;
//
//                viewLocation[1] -= topBarHeight;
//            } else {
//                if (viewLocation.length != 2) {
//                    throw new IllegalStateException("Location can only contain two values, x and y");
//                }
//                // TODO Copy to avoid external mutations
//                viewLocation = new int[] { viewLocation[0], viewLocation[1] };
//            }

            SurroundingThickness thickness = new SurroundingThickness(
                    ViewUtils.dpToPx(thickTop, view.getContext()),
                    ViewUtils.dpToPx(thickBottom, view.getContext()),
                    ViewUtils.dpToPx(thickStart, view.getContext()),
                    ViewUtils.dpToPx(thickEnd, view.getContext())
            );

            InnerPadding padding = new InnerPadding(
                    ViewUtils.dpToPx(padTop, view.getContext()),
                    ViewUtils.dpToPx(padBottom, view.getContext()),
                    ViewUtils.dpToPx(padStart, view.getContext()),
                    ViewUtils.dpToPx(padEnd, view.getContext())
            );

            if (surroundingAreaBackgroundDrawableFactory == null) {
                surroundingAreaBackgroundDrawableFactory = () -> ViewUtils.dispatchDefaultDrawable(view.getContext());
            }

            if (surroundingAreaPaint == null) {
                surroundingAreaPaint = new Paint();
                surroundingAreaPaint.setColor(Color.WHITE);
                surroundingAreaPaint.setAlpha(170);
                surroundingAreaPaint.setAntiAlias(true);
                surroundingAreaPaint.setStyle(Paint.Style.FILL);
            }

            return new FocusArea(
                    view,
                    thickness,
                    surroundingThicknessEffect,
                    surroundingAreaPaint,
                    padding,
                    surroundingAreaBackgroundDrawableFactory,
                    shouldClipToBackground,
                    outerAreaEffect,
                    overlayColor,
                    overlayAlpha
            );
        }
    }
}
