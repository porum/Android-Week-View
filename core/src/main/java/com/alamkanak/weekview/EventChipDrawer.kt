package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.StaticLayout

internal class EventChipDrawer(
    private val viewState: ViewState
) {

    private val dragShadow: Int by lazy {
        Color.parseColor("#757575")
    }

    private val backgroundPaint = Paint()
    private val borderPaint = Paint()

    internal fun draw(
        eventChip: EventChip,
        canvas: Canvas,
        textLayout: StaticLayout?
    ) = with(canvas) {
        val item = eventChip.item
        val bounds = eventChip.bounds
        val cornerRadius = (item.style.cornerRadius ?: viewState.eventCornerRadius).toFloat()

        val isBeingDragged = item.id == viewState.dragState?.eventId
        updateBackgroundPaint(item, isBeingDragged, backgroundPaint)
        drawRoundRect(bounds, cornerRadius, cornerRadius, backgroundPaint)

        val borderWidth = item.style.borderWidth
        if (borderWidth != null && borderWidth > 0) {
            updateBorderPaint(item, borderPaint)
            val borderBounds = bounds.insetBy(borderWidth / 2f)
            drawRoundRect(borderBounds, cornerRadius, cornerRadius, borderPaint)
        }

        if (item.isMultiDay && item.isNotAllDay) {
            drawCornersForMultiDayEvents(eventChip, cornerRadius)
        }

        if (textLayout != null) {
            drawEventTitle(eventChip, textLayout)
        }
    }

    private fun Canvas.drawCornersForMultiDayEvents(
        eventChip: EventChip,
        cornerRadius: Float
    ) {
        val item = eventChip.item
        val bounds = eventChip.bounds

        val isBeingDragged = item.id == viewState.dragState?.eventId
        updateBackgroundPaint(item, isBeingDragged, backgroundPaint)

        if (eventChip.startsOnEarlierDay) {
            val topRect = RectF(bounds)
            topRect.bottom = topRect.top + cornerRadius
            drawRect(topRect, backgroundPaint)
        }

        if (eventChip.endsOnLaterDay) {
            val bottomRect = RectF(bounds)
            bottomRect.top = bottomRect.bottom - cornerRadius
            drawRect(bottomRect, backgroundPaint)
        }

        if (item.style.borderWidth != null) {
            drawMultiDayBorderStroke(eventChip, cornerRadius)
        }
    }

    private fun Canvas.drawMultiDayBorderStroke(
        eventChip: EventChip,
        cornerRadius: Float
    ) {
        val item = eventChip.item
        val bounds = eventChip.bounds

        val borderWidth = item.style.borderWidth ?: 0
        val borderStart = bounds.left + borderWidth / 2
        val borderEnd = bounds.right - borderWidth / 2

        updateBorderPaint(item, backgroundPaint)

        if (eventChip.startsOnEarlierDay) {
            drawVerticalLine(
                horizontalOffset = borderStart,
                startY = bounds.top,
                endY = bounds.top + cornerRadius,
                paint = backgroundPaint
            )

            drawVerticalLine(
                horizontalOffset = borderEnd,
                startY = bounds.top,
                endY = bounds.top + cornerRadius,
                paint = backgroundPaint
            )
        }

        if (eventChip.endsOnLaterDay) {
            drawVerticalLine(
                horizontalOffset = borderStart,
                startY = bounds.bottom - cornerRadius,
                endY = bounds.bottom,
                paint = backgroundPaint
            )

            drawVerticalLine(
                horizontalOffset = borderEnd,
                startY = bounds.bottom - cornerRadius,
                endY = bounds.bottom,
                paint = backgroundPaint
            )
        }
    }

    private fun Canvas.drawEventTitle(
        eventChip: EventChip,
        textLayout: StaticLayout
    ) {
        val bounds = eventChip.bounds

        val horizontalOffset = if (viewState.isLtr) {
            bounds.left + viewState.eventPaddingHorizontal
        } else {
            bounds.right - viewState.eventPaddingHorizontal
        }

        val verticalOffset = if (eventChip.item.isAllDay) {
            (bounds.height() - textLayout.height) / 2f
        } else {
            viewState.eventPaddingVertical.toFloat()
        }

        withTranslation(x = horizontalOffset, y = bounds.top + verticalOffset) {
            draw(textLayout)
        }
    }

    private fun updateBackgroundPaint(
        item: WeekViewItem,
        isBeingDragged: Boolean,
        paint: Paint
    ) = with(paint) {
        color = item.style.backgroundColor ?: viewState.defaultEventColor
        isAntiAlias = true
        strokeWidth = 0f
        style = Paint.Style.FILL

        if (isBeingDragged) {
            setShadowLayer(12f, 0f, 0f, dragShadow)
        } else {
            clearShadowLayer()
        }
    }

    private fun updateBorderPaint(
        item: WeekViewItem,
        paint: Paint
    ) = with(paint) {
        color = item.style.borderColor ?: viewState.defaultEventColor
        isAntiAlias = true
        strokeWidth = item.style.borderWidth?.toFloat() ?: 0f
        style = Paint.Style.STROKE
    }
}
