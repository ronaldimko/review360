package com.example.review360

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI

class SimplePanoramaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), SensorEventListener {

    var bitmap: Bitmap? = null
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    private var scale = 1f
    private var dragOffsetX = 0f

    private var lastX = 0f
    private var dragging = false

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private var yawRad: Float = 0f
    private var yawPixelsPerRad: Float = 1000f

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        rotationVector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sensorManager.unregisterListener(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val bm = bitmap
        if (bm == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val desiredW = MeasureSpec.getSize(widthMeasureSpec)
        val desiredH = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(desiredW, desiredH)

        scale = desiredH.toFloat() / bm.height.toFloat()
        val scaledW = bm.width * scale
        yawPixelsPerRad = (scaledW / (2f * PI).toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bm = bitmap ?: return
        val viewH = height.toFloat()

        val scaledW = bm.width * scale
        val scaledH = bm.height * scale

        // ✅ Инвертированное направление (естественное)
        val gyroOffset = yawRad * yawPixelsPerRad
        var total = dragOffsetX + gyroOffset

        if (scaledW > 0f) {
            while (total > scaledW) total -= scaledW
            while (total < -scaledW) total += scaledW
        }

        val m = Matrix()
        m.setScale(scale, scale)

        m.postTranslate(-total, (viewH - scaledH) / 2f)
        canvas.drawBitmap(bm, m, null)

        m.reset(); m.setScale(scale, scale)
        m.postTranslate(-total - scaledW, (viewH - scaledH) / 2f)
        canvas.drawBitmap(bm, m, null)

        m.reset(); m.setScale(scale, scale)
        m.postTranslate(-total + scaledW, (viewH - scaledH) / 2f)
        canvas.drawBitmap(bm, m, null)

        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                dragging = true
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (dragging) {
                    val dx = event.x - lastX
                    lastX = event.x
                    dragOffsetX -= dx
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dragging = false
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.onTouchEvent(event) || dragging
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotMat = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotMat, event.values)
            val orientations = FloatArray(3)
            SensorManager.getOrientation(rotMat, orientations)
            yawRad = orientations[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
