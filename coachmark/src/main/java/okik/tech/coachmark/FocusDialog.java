package okik.tech.coachmark;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RenderEffect;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Java port of FocusDialog.
 */
public final class FocusDialog {

    private final Paint pathViewPathPaint;
    private final View DialogView;
    @Nullable private final RenderEffect pathBackgroundRenderEffect;
    private final @Nullable PathViewPathGeneratorCommand pathViewPathGeneratorCommand;
    private final DialogConstraintsCommand dialogConstraintsCommand;

    private FocusDialog(
            Paint originBackgroundPaint,
            View dialogView,
            @Nullable RenderEffect backgroundRenderEffect,
            @Nullable PathViewPathGeneratorCommand pathViewPathGeneratorCommand,
            DialogConstraintsCommand dialogConstraintsCommand
    ) {
        this.pathViewPathPaint = originBackgroundPaint;
        this.DialogView = dialogView;
        this.pathBackgroundRenderEffect = backgroundRenderEffect;
        this.pathViewPathGeneratorCommand = pathViewPathGeneratorCommand;
        this.dialogConstraintsCommand = dialogConstraintsCommand;
    }
    public @Nullable PathViewPathGeneratorCommand getPathViewPathGeneratorCommand() { return pathViewPathGeneratorCommand; }
    public DialogConstraintsCommand getDialogConstraintsCommand() { return dialogConstraintsCommand; }
    public Paint getPathViewPathPaint() { return pathViewPathPaint; }
    public View getDialogView() { return DialogView; }
    @Nullable public RenderEffect getPathBackgroundRenderEffect() { return pathBackgroundRenderEffect; }

    // --- Builder ---
    public static final class Builder {
        private @Nullable Paint pathViewPathPaint;
        private @Nullable View view;
        private @Nullable RenderEffect pathBackgroundRenderEffect;
        private @Nullable PathViewPathGeneratorCommand pathViewPathGeneratorCommand;
        private DialogConstraintsCommand dialogConstraintsCommand;

        public Builder setPathViewPathGeneratorCommand(@Nullable PathViewPathGeneratorCommand command) {
            this.pathViewPathGeneratorCommand = command;
            return this;
        }

        public Builder setDialogConstraintsCommand(DialogConstraintsCommand command) {
            this.dialogConstraintsCommand = command;
            return this;
        }

        public Builder setPathViewPathPaint(Paint pathViewPathPaint) {
            this.pathViewPathPaint = pathViewPathPaint;
            return this;
        }

        public Builder setDialogView(View view) {
            this.view = view;
            return this;
        }

        public Builder setPathBackgroundRenderEffect(@Nullable RenderEffect pathBackgroundRenderEffect) {
            this.pathBackgroundRenderEffect = pathBackgroundRenderEffect;
            return this;
        }

        public FocusDialog build() {
            if (view == null) {
                throw new IllegalStateException("A view has to be defined");
            }

            // Default paint if none
            if (pathViewPathPaint == null) {
                pathViewPathPaint = new Paint();
                pathViewPathPaint.setColor(Color.WHITE);
                pathViewPathPaint.setAlpha(170);
                pathViewPathPaint.setAntiAlias(true);
                pathViewPathPaint.setStyle(Paint.Style.FILL);
            }

            if (dialogConstraintsCommand == null) {
                throw new IllegalStateException("You have to explicitly set constraints for you dialog, set dialogConstraintsCommand value in FocusDialog.Builder, use methods like constraintDialogToBottom in DialogWrapperLayout");
            }

            return new FocusDialog(
                    pathViewPathPaint,
                    view,
                    pathBackgroundRenderEffect,
                    pathViewPathGeneratorCommand,
                    dialogConstraintsCommand
            );
        }
    }


    public interface PathViewPathGeneratorCommand {
        @Nullable Path generate(View focusView, View dialog);
    }

    public interface DialogConstraintsCommand {
        void execute(DialogWrapperLayout constraintLayout, View focusView, View dialog);
    }

}
