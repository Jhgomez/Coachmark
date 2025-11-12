package okik.tech.coachmark;


import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RecordingCanvas;
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

public class CoachMarkOverlay extends ConstraintLayout {
    public FocusArea focusArea = null;
    int[] emphasisViewLoc;
    private RenderNode contentCopy = null;
    private final RenderNode contentWithEffect;
    private final RenderNode focusedContent;

    {
        setBackgroundColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentWithEffect = new RenderNode("BlurredContent");
            focusedContent = new RenderNode("FocusContent");
        } else {
            contentWithEffect = null;
            focusedContent = null;
        }
    }

    public CoachMarkOverlay(Context context) {
        this(context, null);
    }

    public CoachMarkOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private void addSurroundingFocusAreaView() {
        // view at index 0
        BackgroundEffectRendererLayout focusSurrounding = new BackgroundEffectRendererLayout(getContext());
        focusSurrounding.setId(View.generateViewId());
        addView(focusSurrounding);
    }

    private void configureFocusAreaViewSurrounding(FocusArea fa, @Nullable RenderNode renderNode) {
        float focusViewWidth =
                fa.getView().getWidth()
                        + fa.getSurroundingThickness().getStart()
                        + fa.getSurroundingThickness().getEnd();

        float focusViewHeight =
                fa.getView().getHeight()
                        + fa.getSurroundingThickness().getTop()
                        + fa.getSurroundingThickness().getBottom();

        float focusViewXLoc = emphasisViewLoc[0] - fa.getSurroundingThickness().getStart();
        float focusViewYLoc = emphasisViewLoc[1] - fa.getSurroundingThickness().getTop();

        boolean isDrawnB4Start = focusViewXLoc < 0;
        boolean isDrawnB4Top = focusViewYLoc < 0;

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);

        int xConstraint = isDrawnB4Start ? ConstraintSet.END : ConstraintSet.START;
        int yConstraint = isDrawnB4Top ? ConstraintSet.BOTTOM : ConstraintSet.TOP;

        BackgroundEffectRendererLayout focusSurrounding =
                (BackgroundEffectRendererLayout) getChildAt(0);

//        focusSurrounding.setVisibility(View.VISIBLE);

        constraintSet.connect(focusSurrounding.getId(), yConstraint, getId(), ConstraintSet.TOP);
        constraintSet.connect(focusSurrounding.getId(), xConstraint, getId(), ConstraintSet.START);
        constraintSet.applyTo(this);

        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) focusSurrounding.getLayoutParams();

        if (isDrawnB4Start) {
            params.setMarginEnd((int) (-focusViewWidth - focusViewXLoc));
        } else {
            // marginStart = focusViewXLoc
            params.setMarginStart((int) (focusViewXLoc));
        }

        if (isDrawnB4Top) {
//             bottomMargin = (-referenceViewHeight - focusViewYLoc)
            params.bottomMargin = (int) (-focusViewHeight - focusViewYLoc);
        } else {
            // topMargin = focusViewYLoc
            params.topMargin = (int) (focusViewYLoc);
        }

        params.width = (int) (focusViewWidth);
        params.height = (int) (focusViewHeight);
//        focusSurrounding.setLayoutParams(params);

        BackgroundSettings backgroundSettings = fa.generateBackgroundSettings();

        focusSurrounding.setBackgroundConfigs(backgroundSettings, renderNode);

        if (getContext() instanceof Activity) {
            Activity act = (Activity) getContext();
            focusSurrounding.setFallbackBackground(
                    act.getWindow().getDecorView().getBackground()
            );
        }
    }

    public void configuredDialog(
            FocusArea fa,
            FocusDialog fd,
            @Nullable RenderNode renderNode,
            int[] emphasisViewLoc
    ) {
        contentCopy = renderNode;
        focusArea = fa;
        this.emphasisViewLoc = emphasisViewLoc;

        if (fd.getDialogView() instanceof BackgroundEffectRendererLayout) {
            BackgroundEffectRendererLayout dialog = (BackgroundEffectRendererLayout) fd.getDialogView();
            dialog.setBackgroundRenderNode(renderNode);

            if (getContext() instanceof Activity) {
                Activity act = (Activity) getContext();
                dialog.setFallbackBackground(act.getWindow().getDecorView().getBackground());
            }
        }

        if (getChildCount() == 0) {
            if (getId() == View.NO_ID) setId(View.generateViewId());

            addSurroundingFocusAreaView();

            RenderNodeBehindPathView bridgeView = new RenderNodeBehindPathView(getContext());
            bridgeView.setId(View.generateViewId());

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);

            constraintSet.connect(bridgeView.getId(), ConstraintSet.TOP, getId(), ConstraintSet.TOP);
            constraintSet.connect(bridgeView.getId(), ConstraintSet.START, getId(), ConstraintSet.START);
            constraintSet.applyTo(this);

            // bridge view has to fill entire screen to be able to draw the path in the required location
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            bridgeView.setLayoutParams(lp);

            if (getContext() instanceof Activity) {
                bridgeView.setFallbackBackground(((Activity) getContext()).getWindow().getDecorView().getBackground());
            }

            addView(bridgeView);

            if (fd.getDialogView().getId() == View.NO_ID) {
                fd.getDialogView().setId(View.generateViewId());
            }
            addView(fd.getDialogView());
        }

        configureFocusAreaViewSurrounding(fa, renderNode);

        final ViewTreeObserver.OnPreDrawListener dialogPreDrawListener =
                getOnPreDrawListener(fd, renderNode);

        fd.getDialogView().getViewTreeObserver().addOnPreDrawListener(dialogPreDrawListener);

        this.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                fd.getDialogView().getViewTreeObserver().removeOnPreDrawListener(dialogPreDrawListener);
            }
        });

        View focusView = getChildAt(0);
        fd.getDialogConstraintsCommand().execute(this, focusView, fd.getDialogView());
    }

    private ViewTreeObserver.OnPreDrawListener getOnPreDrawListener(FocusDialog fd, RenderNode renderNode) {
        ViewTreeObserver.OnPreDrawListener dialogPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                View focusView = getChildAt(0);

                if (fd.getPathViewPathGeneratorCommand() != null) {

                    Path path = fd.getPathViewPathGeneratorCommand().generate(focusView, fd.getDialogView());

                    RenderNodeBehindPathView bridgeView = (RenderNodeBehindPathView) getChildAt(1);

                    bridgeView.setBackgroundConfigs(
                            renderNode,
                            path,
                            fd.getPathViewPathPaint(),
                            true,
                            true,
                            fd.getPathBackgroundRenderEffect()
                    );

                    bridgeView.setVisibility(VISIBLE);

                    fd.getDialogView().getViewTreeObserver().removeOnPreDrawListener(this);
                }

                return true;
            }
        };

        fd.getDialogView().getViewTreeObserver().addOnPreDrawListener(dialogPreDrawListener);
        return dialogPreDrawListener;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (focusArea != null) {
            FocusArea fa = focusArea;
            int overlayColor = ColorUtils.setAlphaComponent(fa.getOverlayColor(), fa.getOverlayAlpha());

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                canvas.drawColor(overlayColor);
            }

            if (contentCopy != null) {
                // Create a version of the original view that lives in "contentCopy" and apply the effect
                if (contentWithEffect != null) {
                    contentWithEffect.setRenderEffect(fa.getOuterAreaEffect());
                    contentWithEffect.setPosition(0, 0, getWidth(), getHeight());

                    RecordingCanvas contentWithEffectRecordingCanvas = contentWithEffect.beginRecording();
                    contentWithEffectRecordingCanvas.drawRenderNode(contentCopy);
                    contentWithEffectRecordingCanvas.drawColor(overlayColor);
                    contentWithEffect.endRecording();

                    // Draw the applied effect to content on this layout's canvas
                    canvas.drawRenderNode(contentWithEffect);
                }

                // Make a copy of original content but only of the specified view and requested surrounding area
                if (emphasisViewLoc.length == 2) {
                    int focusWidth = fa.getView().getWidth();
                    int focusHeight = fa.getView().getHeight();

                    float translationX = (float) emphasisViewLoc[0];
                    float translationY = (float) emphasisViewLoc[1];

                    float canvasTranslationX = - (float) emphasisViewLoc[0];
                    float canvasTranslationY = - (float) emphasisViewLoc[1];

                    focusedContent.setPosition(0, 0, focusWidth, focusHeight);
                    focusedContent.setTranslationX(translationX);
                    focusedContent.setTranslationY(translationY);

                    RecordingCanvas focusAreaRecordingCanvas = focusedContent.beginRecording();
                    focusAreaRecordingCanvas.translate(canvasTranslationX, canvasTranslationY);
                    focusAreaRecordingCanvas.drawRenderNode(contentCopy);
                    focusedContent.endRecording();

                    // Note: the focus area is not drawn here yet; it will be drawn above its surrounding area,
                    // which itself must render above the background overlay.
                }
            }
        }

        super.draw(canvas);
    }

    public void updateBackground(RenderNode renderNode) {
        BackgroundEffectRendererLayout focusAreaView =
                (BackgroundEffectRendererLayout) getChildAt(0);
        focusAreaView.setBackgroundRenderNode(renderNode);
        focusAreaView.invalidate();

        RenderNodeBehindPathView bridgeView =
                (RenderNodeBehindPathView) getChildAt(1);
        bridgeView.setBackgroundViewRenderNode(renderNode);
        bridgeView.invalidate();

        BackgroundEffectRendererLayout dialogView =
                (BackgroundEffectRendererLayout) getChildAt(2);
        dialogView.setBackgroundRenderNode(renderNode);
        dialogView.invalidate();

        invalidate();
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // since we are not using render nodes in this case, we have to wait until the
            // last child is draw to then be able to draw the view we want to focus on and
            // then move the canvas to match its location on the screen, if we move the canvas
            // before this, then children draw after moving canvas will be affected
            if (getChildAt(2).getId() == child.getId()) {
                if (focusArea != null) {
                    boolean hasSurrounding =
                            focusArea.getSurroundingThickness().getTop() > 0
                                    || focusArea.getSurroundingThickness().getBottom() > 0
                                    || focusArea.getSurroundingThickness().getStart() > 0
                                    || focusArea.getSurroundingThickness().getEnd() > 0;

                    if (hasSurrounding) {
                        boolean isInvalidateIssued = super.drawChild(canvas, child, drawingTime);

                        canvas.translate(
                                emphasisViewLoc[0],
                                emphasisViewLoc[1]
                        );

                        focusArea.getView().draw(canvas);
                        return isInvalidateIssued;
                    } else {
                        focusArea.getView().draw(canvas);
                        return false;
                    }
                }
            }
        } else {
            if (getChildAt(0).getId() == child.getId()) {
                if (focusArea != null) {
                    boolean hasSurrounding =
                            focusArea.getSurroundingThickness().getTop() > 0
                                    || focusArea.getSurroundingThickness().getBottom() > 0
                                    || focusArea.getSurroundingThickness().getStart() > 0
                                    || focusArea.getSurroundingThickness().getEnd() > 0;

                    if (hasSurrounding) {
                        boolean isInvalidateIssued = super.drawChild(canvas, child, drawingTime);
                        canvas.drawRenderNode(focusedContent);
                        return isInvalidateIssued;
                    } else {
                        canvas.drawRenderNode(focusedContent);
                        return false;
                    }
                }
            }
        }

        // If no focus area has been specified, just render the child normally
        boolean invalidated = super.drawChild(canvas, child, drawingTime);
        return invalidated;
    }

    public static void constraintDialogToBottom(
            ConstraintLayout constraintLayout,
            View focusView,
            View dialogView,
            double dialogXMarginPx,
            double dialogYMarginPx,
            boolean centerDialogOnMainAxis
    ) {
        ConstraintSet dialogCs = new ConstraintSet();
        dialogCs.clone(constraintLayout);

        if (centerDialogOnMainAxis) {
            dialogCs.connect(dialogView.getId(), ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT);
            dialogCs.connect(dialogView.getId(), ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT);
            dialogCs.connect(dialogView.getId(), ConstraintSet.TOP, focusView.getId(), ConstraintSet.BOTTOM);
            dialogCs.connect(dialogView.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM);
            dialogCs.setVerticalBias(dialogView.getId(), 0f);

            dialogCs.applyTo(constraintLayout);
        } else {
            dialogCs.connect(dialogView.getId(), ConstraintSet.LEFT, focusView.getId(), ConstraintSet.LEFT);
            dialogCs.connect(dialogView.getId(), ConstraintSet.TOP, focusView.getId(), ConstraintSet.BOTTOM);
            dialogCs.connect(dialogView.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM);
            dialogCs.setVerticalBias(dialogView.getId(), 0f);

            dialogCs.applyTo(constraintLayout);
        }

        ((MarginLayoutParams)dialogView.getLayoutParams()).setMarginStart((int) dialogXMarginPx);
        ((MarginLayoutParams)dialogView.getLayoutParams()).topMargin = (int) dialogYMarginPx;
    }

    public static void constraintDialogToTop(
            ConstraintLayout constraintLayout,
            View focusView,
            View dialogView,
            double dialogXMarginPx,
            double dialogYMarginPx,
            boolean centerDialogOnMainAxis
    ) {
        ConstraintSet dialogCs = new ConstraintSet();
        dialogCs.clone(constraintLayout);

        if (centerDialogOnMainAxis) {
            dialogCs.connect(dialogView.getId(), ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT);
            dialogCs.connect(dialogView.getId(), ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT);
            dialogCs.connect(dialogView.getId(), ConstraintSet.BOTTOM, focusView.getId(), ConstraintSet.TOP);
            dialogCs.connect(dialogView.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP);
            dialogCs.setVerticalBias(dialogView.getId(), 1f);

            dialogCs.applyTo(constraintLayout);
        } else {
            dialogCs.connect(dialogView.getId(), ConstraintSet.LEFT, focusView.getId(), ConstraintSet.LEFT);
            dialogCs.connect(dialogView.getId(), ConstraintSet.BOTTOM, focusView.getId(), ConstraintSet.TOP);
            dialogCs.connect(dialogView.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP);
            dialogCs.setVerticalBias(dialogView.getId(), 1f);

            dialogCs.applyTo(constraintLayout);
        }

        ((MarginLayoutParams)dialogView.getLayoutParams()).setMarginStart((int) dialogXMarginPx);
        ((MarginLayoutParams)dialogView.getLayoutParams()).bottomMargin = (int) dialogYMarginPx;
    }

    public static void constraintDialogToStart(
            ConstraintLayout constraintLayout,
            View focusView,
            View dialogView,
            double dialogXMarginPx,
            double dialogYMarginPx,
            boolean centerDialogOnMainAxis
    ) {
        ConstraintSet dialogCs = new ConstraintSet();
        dialogCs.clone(constraintLayout);

        if (centerDialogOnMainAxis) {
            dialogCs.connect(dialogView.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP);
            dialogCs.connect(dialogView.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM);
            dialogCs.setHorizontalBias(dialogView.getId(), 1f);
            dialogCs.connect(dialogView.getId(), ConstraintSet.RIGHT, focusView.getId(), ConstraintSet.LEFT);
            dialogCs.connect(dialogView.getId(), ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT);

            dialogCs.applyTo(constraintLayout);
        } else {
            dialogCs.connect(dialogView.getId(), ConstraintSet.TOP, focusView.getId(), ConstraintSet.TOP);
            dialogCs.setVerticalBias(dialogView.getId(), 1f);
            dialogCs.connect(dialogView.getId(), ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT);
            dialogCs.connect(dialogView.getId(), ConstraintSet.RIGHT, focusView.getId(), ConstraintSet.LEFT);

            dialogCs.applyTo(constraintLayout);
        }

        ((MarginLayoutParams)dialogView.getLayoutParams()).setMarginEnd((int) dialogXMarginPx);
        ((MarginLayoutParams)dialogView.getLayoutParams()).topMargin = (int) dialogYMarginPx;
    }

    public static void constraintDialogToEnd(
            ConstraintLayout constraintLayout,
            View focusView,
            View dialogView,
            double dialogXMarginPx,
            double dialogYMarginPx,
            boolean centerDialogOnMainAxis
    ) {
        ConstraintSet dialogCs = new ConstraintSet();
        dialogCs.clone(constraintLayout);

        if (centerDialogOnMainAxis) {
            dialogCs.connect(dialogView.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP);
            dialogCs.connect(dialogView.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM);
            dialogCs.setHorizontalBias(dialogView.getId(), 0f);
            dialogCs.connect(dialogView.getId(), ConstraintSet.LEFT, focusView.getId(), ConstraintSet.RIGHT);
            dialogCs.connect(dialogView.getId(), ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT);

            dialogCs.applyTo(constraintLayout);
        } else {
            dialogCs.connect(dialogView.getId(), ConstraintSet.TOP, focusView.getId(), ConstraintSet.TOP);
            dialogCs.setHorizontalBias(dialogView.getId(), 0f);
            dialogCs.connect(dialogView.getId(), ConstraintSet.LEFT, focusView.getId(), ConstraintSet.RIGHT);
            dialogCs.connect(dialogView.getId(), ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT);

            dialogCs.applyTo(constraintLayout);
        }

        ((MarginLayoutParams)dialogView.getLayoutParams()).setMarginStart((int) dialogXMarginPx);
        ((MarginLayoutParams)dialogView.getLayoutParams()).topMargin = (int) dialogYMarginPx;
    }

    public static Path drawPathToBottomDialog(
            View focusView,
            View dialogView,
            double originOffsetPercent,
            double destinationOffsetPercent,
            double triangleSpacingPx
    ) {
        int[] parentLocation = new int[2];

        ViewParent parent = focusView.getParent();

        if (parent instanceof View) {
            ((View)parent).getLocationOnScreen(parentLocation);
        }

        int[] focusViewLocation = new int[2];
        focusView.getLocationOnScreen(focusViewLocation);
        focusViewLocation[0] -= parentLocation[0];
        focusViewLocation[1] -= parentLocation[1];

        int[] dialogViewLocation = new int[2];
        dialogView.getLocationOnScreen(dialogViewLocation);
        dialogViewLocation[0] -= parentLocation[0];
        dialogViewLocation[1] -= parentLocation[1];

        double startX = focusViewLocation[0] + focusView.getWidth() * originOffsetPercent;
        int startY = focusViewLocation[1] + focusView.getHeight();

        Path path = new Path();

        path.moveTo((float) startX, startY);

        double firstVertexX = dialogViewLocation[0] + dialogView.getWidth() * destinationOffsetPercent;
        int firstVertexY = dialogViewLocation[1];

        path.lineTo((float) firstVertexX, (float) firstVertexY);

        double secondVertexX = firstVertexX + triangleSpacingPx;

        path.lineTo((float) secondVertexX, (float) firstVertexY);
        path.close();

        return path;
    }

    public static Path drawPathToTopDialog(
            View focusView,
            View dialogView,
            double originOffsetPercent,
            double destinationOffsetPercent,
            double triangleSpacingPx
    ) {
        int[] parentLocation = new int[2];

        ViewParent parent = focusView.getParent();

        if (parent instanceof View) {
            ((View)parent).getLocationOnScreen(parentLocation);
        }

        int[] focusViewLocation = new int[2];
        focusView.getLocationOnScreen(focusViewLocation);
        focusViewLocation[0] -= parentLocation[0];
        focusViewLocation[1] -= parentLocation[1];

        int[] dialogViewLocation = new int[2];
        dialogView.getLocationOnScreen(dialogViewLocation);
        dialogViewLocation[0] -= parentLocation[0];
        dialogViewLocation[1] -= parentLocation[1];

        double startX = focusViewLocation[0] + focusView.getWidth() * originOffsetPercent;
        int startY = focusViewLocation[1];

        Path path = new Path();

        path.moveTo((float) startX, startY);

        double firstVertexX = dialogViewLocation[0] + dialogView.getWidth() * destinationOffsetPercent;
        int firstVertexY = dialogViewLocation[1] + dialogView.getHeight();

        path.lineTo((float) firstVertexX, (float) firstVertexY);

        double secondVertexX = firstVertexX + triangleSpacingPx;

        path.lineTo((float) secondVertexX, (float) firstVertexY);
        path.close();

        return path;
    }

    public static Path drawPathToStartDialog(
            View focusView,
            View dialogView,
            double originOffsetPercent,
            double destinationOffsetPercent,
            double triangleSpacingPx
    ) {
        int[] parentLocation = new int[2];

        ViewParent parent = focusView.getParent();

        if (parent instanceof View) {
            ((View)parent).getLocationOnScreen(parentLocation);
        }

        int[] focusViewLocation = new int[2];
        focusView.getLocationOnScreen(focusViewLocation);
        focusViewLocation[0] -= parentLocation[0];
        focusViewLocation[1] -= parentLocation[1];

        int[] dialogViewLocation = new int[2];
        dialogView.getLocationOnScreen(dialogViewLocation);
        dialogViewLocation[0] -= parentLocation[0];
        dialogViewLocation[1] -= parentLocation[1];

        int startX = focusViewLocation[0];
        double startY = focusViewLocation[1] + focusView.getHeight() * originOffsetPercent;

        Path path = new Path();

        path.moveTo(startX, (float) startY);

        int firstVertexX = dialogViewLocation[0] + dialogView.getWidth();
        double firstVertexY = dialogViewLocation[1] + dialogView.getHeight()  * destinationOffsetPercent;

        path.lineTo(firstVertexX, (float) firstVertexY);

        double secondVertexY = firstVertexY + triangleSpacingPx;

        path.lineTo(firstVertexX, (float) secondVertexY);
        path.close();

        return path;
    }

    public static Path drawPathToEndDialog(
            View focusView,
            View dialogView,
            double originOffsetPercent,
            double destinationOffsetPercent,
            double triangleSpacingPx
    ) {
        int[] parentLocation = new int[2];

        ViewParent parent = focusView.getParent();

        if (parent instanceof View) {
            ((View)parent).getLocationOnScreen(parentLocation);
        }

        int[] focusViewLocation = new int[2];
        focusView.getLocationOnScreen(focusViewLocation);
        focusViewLocation[0] -= parentLocation[0];
        focusViewLocation[1] -= parentLocation[1];

        int[] dialogViewLocation = new int[2];
        dialogView.getLocationOnScreen(dialogViewLocation);
        dialogViewLocation[0] -= parentLocation[0];
        dialogViewLocation[1] -= parentLocation[1];

        int startX = focusViewLocation[0] + focusView.getWidth();
        double startY = focusViewLocation[1] + focusView.getHeight() * originOffsetPercent;

        Path path = new Path();

        path.moveTo(startX, (float) startY);

        int firstVertexX = dialogViewLocation[0];
        double firstVertexY = dialogViewLocation[1] + dialogView.getHeight()  * destinationOffsetPercent;

        path.lineTo(firstVertexX, (float) firstVertexY);

        double secondVertexY = firstVertexY + triangleSpacingPx;

        path.lineTo(firstVertexX, (float) secondVertexY);
        path.close();

        return path;
    }
}
