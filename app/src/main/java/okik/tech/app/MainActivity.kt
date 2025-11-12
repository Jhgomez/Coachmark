package okik.tech.app

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout.LayoutParams
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import okik.tech.app.databinding.ActivityMainBinding
import okik.tech.app.databinding.DialogContentBinding
import okik.tech.coachmark.BackgroundEffectRendererLayout
import okik.tech.coachmark.CoachMarkOverlay
import okik.tech.coachmark.FocusArea
import okik.tech.coachmark.ViewUtils.dpToPx


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recycler.layoutManager = manager

        val adapter = MyAdapter(listOf(1,2,3,4,5,6,7,8,9))
        binding.recycler.adapter = adapter

        binding.block.setBackgroundColor(Color.GREEN)

        var count = 0
        binding.buttonFirst.setOnClickListener {
//            if (count == 0) {
//                count += 1
            val aView = binding.recycler.layoutManager?.findViewByPosition(3)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {

            }

            if (aView != null) {
                val paint = Paint()
                paint.color = Color.WHITE
                paint.alpha = 0
                paint.isAntiAlias = true
                paint.style = Paint.Style.FILL
                paint.strokeWidth = 16f

                val focusArea = FocusArea.Builder()
                    .setView(aView) // binding.thete ?:
                    .setOuterAreaEffect(
                        RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP)
                    )
                    .setOuterAreaOverlayColor(Color.BLACK)
                    .setOuterAreaOverlayAlpha(110)
//                    .setSurroundingAreaPadding(10, 10, 10, 10)
//                    .setSurroundingAreaBackgroundDrawableFactory(
//                        {
//                            val roundShape = OvalShape()
//
//                            return@setSurroundingAreaBackgroundDrawableFactory ShapeDrawable(roundShape)
//                        }
//                    )
                    .setSurroundingThicknessEffect(
                        RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP)
                    )
                    .setSurroundingAreaPaint(paint)
                    .setShouldClipToBackground(true)
                    .setSurroundingThickness(50, 50, 50, 50)
                    .build()

                val dialog = getDialog(focusArea)

                val focusDialog = focusArea
                    .generateMatchingFocusDialog()
                    .setDialogView(dialog)
                    .setDialogConstraintsCommand { cl, focusView, dialog ->
                        CoachMarkOverlay.constraintDialogToBottom(
                            cl,
                            focusView,
                            dialog,
                            0.0,
                            dpToPx(400, focusView.context).toDouble(),
                            false
                        )
                    }
                    .setPathViewPathGeneratorCommand { fv, dialog ->
                        CoachMarkOverlay.drawPathToBottomDialog(
                            fv,
                            dialog,
                            0.1,
                            0.1,
                            dpToPx(120,fv.context).toDouble()
                        )
                    }
                    .build()


//                    binding.root.renderFocusArea(focusArea)
                binding.tutorial.renderFocusAreaWithDialog(focusArea, focusDialog)

//                val bsb = BottomSheetBehavior.from(binding.bottomchit)
//                bsb.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//                    override fun onStateChanged(
//                        bottomSheet: View,
//                        newState: Int
//                    ) {
//
//                    }
//
//                    override fun onSlide(
//                        bottomSheet: View,
//                        slideOffset: Float
//                    ) {
//
//                    }
//                })
//
//                bsb.state = BottomSheetBehavior.STATE_COLLAPSED
            }
//            } else {
//                count -= 1
//                binding.root.hideTutorialComponents()
//            }

            val colors = arrayOf(Color.MAGENTA, Color.WHITE, Color.RED, Color.GREEN, Color.YELLOW,
                Color.BLUE)

            val thread = Thread({
                Thread.sleep(2000)

                val dim = dpToPx(120, this).toInt()

                val view = View(this)
                view.id = View.generateViewId()
                view.layoutParams = ConstraintLayout.LayoutParams(dim, dim)

                binding.clTtdl.post {
                    binding.clTtdl.addView(view)

                    val cs = ConstraintSet()
                    cs.clone(binding.clTtdl)

                    cs.connect(view.id, ConstraintSet.BOTTOM, binding.clTtdl.id, ConstraintSet.BOTTOM)
                    cs.connect(view.id, ConstraintSet.LEFT, binding.clTtdl.id, ConstraintSet.LEFT)

                    cs.applyTo(binding.clTtdl)

                    (view.layoutParams as MarginLayoutParams).marginStart = dpToPx(16, this).toInt()
                    (view.layoutParams as MarginLayoutParams).bottomMargin = dpToPx(32, this).toInt()

                    view.setBackgroundColor(colors.random())
                }
            })

            thread.start()
        }
    }

    private fun getDialog(
        focusArea: FocusArea
    ): BackgroundEffectRendererLayout {
        val dialog = BackgroundEffectRendererLayout(this)

        dialog.layoutParams = ConstraintLayout.LayoutParams(700, 530)

        val content = DialogContentBinding.inflate(LayoutInflater.from(this))

        content.root.layoutParams = LayoutParams(700, 530)

        (content.root.layoutParams as MarginLayoutParams).setMargins(0, 0, 0, 0)
//
//        content.root.setPadding(
//            focusArea.surroundingAreaPadding.start.toInt(),
//            focusArea.surroundingAreaPadding.top.toInt(),
//            focusArea.surroundingAreaPadding.end.toInt(),
//            focusArea.surroundingAreaPadding.bottom.toInt()
//        )

        dialog.setFallbackBackground(window.decorView.background)

        dialog.addView(content.root)

        val backgroundSettings = focusArea.generateBackgroundSettings()

        dialog.setBackgroundConfigs(backgroundSettings)


//        val insets =
//            ViewCompat.getRootWindowInsets(referenceView)
//
//        val topInset: Int
//
//        if (insets != null) {
//            topInset = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars()).top
//        } else {
//            topInset = 0
//        }

        content.tb.setOnClickListener {
            binding.tutorial.hideTutorialComponents()
        }

        return dialog
    }
}