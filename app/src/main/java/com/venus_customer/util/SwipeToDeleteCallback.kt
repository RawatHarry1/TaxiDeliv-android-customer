import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color

import com.venus_customer.R
import com.venus_customer.view.activity.walk_though.ui.home.AddedAddressAdapter

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.abs

class SwipeToShowDeleteCallback(
    private val adapter: AddedAddressAdapter,
    private val context: Context
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#f44336") // Red color
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private val swipeThreshold = context.resources.displayMetrics.density * 100 // 100 dp
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = context.resources.displayMetrics.density * 16 // 16 sp
        isAntiAlias = true
        typeface = ResourcesCompat.getFont(context, R.font.poppins_regular) ?: Typeface.DEFAULT_BOLD
    }
    private val textMargin = context.resources.displayMetrics.density * 16 // 16 dp
    private var isSwiped = false

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Remove item on full swipe
        if (isSwiped) {
            adapter.removeItem(viewHolder.absoluteAdapterPosition)
        } else {
            // Reveal "Remove" text
            adapter.setDeleteVisible(viewHolder.absoluteAdapterPosition, true)
        }
        isSwiped = false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val text = "Remove"

        if (dX < 0) {
            background.color = backgroundColor
            background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
            background.draw(c)

            val textWidth = textPaint.measureText(text)
            val textTop = itemView.top + (itemHeight - textPaint.textSize) / 2
            val textLeft = itemView.right - textMargin - textWidth
            val textRight = itemView.right - textMargin
            val textBottom = textTop + textPaint.textSize

            if (abs(dX) > itemView.width / 2) {
                c.drawText(text, itemView.right + dX / 2 - textWidth / 2, textTop + textPaint.textSize, textPaint)
            } else {
                c.drawText(text, textLeft, textTop + textPaint.textSize, textPaint)
            }

            if (abs(dX) >= itemView.width / 2) {
                isSwiped = true
            } else {
                isSwiped = false
            }

            itemView.translationX = dX
        } else {
            background.setBounds(0, 0, 0, 0)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && abs(dX) < swipeThreshold) {
            val itemView = viewHolder?.itemView
            if (itemView != null) {
                c.drawRect(
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat(),
                    clearPaint
                )
            }
        }
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 2f // Arbitrary large value to prevent ItemTouchHelper from thinking the swipe is completed
    }
}
