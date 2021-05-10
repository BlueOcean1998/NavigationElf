package cn.zerokirby.api.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.*

/**
 * 生成验证码工具类
 */
class VerifyUtil {
    companion object {
        //随机码集
        private const val CODES =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

        //验证码个数
        private const val CODE_LENGTH = 4

        //字体大小
        private const val FONT_SIZE = 50

        //线条数
        private const val LINE_NUMBER = 5

        //padding，其中base的意思是初始值，而range是变化范围。数值根据自己想要的大小来设置
        private const val BASE_PADDING_LEFT = 10
        private const val RANGE_PADDING_LEFT = 100
        private const val BASE_PADDING_TOP = 75
        private const val RANGE_PADDING_TOP = 50

        //验证码默认宽高
        private const val DEFAULT_WIDTH = 360
        private const val DEFAULT_HEIGHT = 120

        //随机数
        private val random = Random()
    }

    /**
     * 获取生成的验证码
     *
     * @return 验证码
     */
    var code = ""//生成的验证码

    private var paddingLeft = 0
    private var paddingTop = 0

    /**
     * 生成验证码图片
     *
     * @return 位图
     */
    fun createBitmap(): Bitmap {
        paddingLeft = 0
        paddingTop = 0
        //创建指定格式，大小的位图//Config.ARGB_8888是一种色彩的存储方法
        val bp = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888)
        val c = Canvas(bp)
        code = createCode()
        //将画布填充为白色
        c.drawColor(Color.WHITE)
        //新建一个画笔
        val paint = Paint()
        //设置画笔抗锯齿
        paint.isAntiAlias = true
        paint.textSize = FONT_SIZE.toFloat()
        for (element in code) {
            randomTextStyle(paint)
            randomPadding()
            //这里的padding_left,padding_top是文字的基线
            c.drawText(element.toString(), paddingLeft.toFloat(), paddingTop.toFloat(), paint)
        }
        //画干扰线
        for (i in 0 until LINE_NUMBER) {
            drawLine(c, paint)
        }
        //保存一下画布
        c.save()
        c.restore()
        return bp
    }

    //生成验证码
    private fun createCode(): String {
        StringBuilder().run {
            //利用random生成随机下标，验证码个数，线条数，字体大小
            for (i in 0 until CODE_LENGTH) {
                append(CODES[random.nextInt(CODES.length)])
            }
            return toString()
        }
    }

    //随机文字样式，颜色，文字粗细与倾斜度
    private fun randomTextStyle(paint: Paint) {
        val color = randomColor()
        paint.color = color
        paint.isFakeBoldText = random.nextBoolean() //true为粗体，false为非粗体
        var skew = random.nextFloat() / 2
        //随机ture或者false来生成正数或者负数，来表示文字的倾斜度，负数右倾，正数左倾
        skew = if (random.nextBoolean()) skew else -skew
        paint.textSkewX = skew
    }

    //验证码位置随机
    private fun randomPadding() {
        //字体的随机位置
        paddingLeft += BASE_PADDING_LEFT + random.nextInt(RANGE_PADDING_LEFT)
        paddingTop = BASE_PADDING_TOP + random.nextInt(RANGE_PADDING_TOP)
    }

    //画干扰线
    private fun drawLine(canvas: Canvas, paint: Paint) {
        val color = randomColor()
        val startX = random.nextInt(DEFAULT_WIDTH).toFloat()
        val startY = random.nextInt(DEFAULT_HEIGHT).toFloat()
        val stopX = random.nextInt(DEFAULT_WIDTH).toFloat()
        val stopY = random.nextInt(DEFAULT_HEIGHT).toFloat()
        paint.strokeWidth = 1f
        paint.color = color
        canvas.drawLine(startX, startY, stopX, stopY, paint)
    }

    //生成随机颜色
    private fun randomColor(): Int {
        val red = random.nextInt(256)
        val green = random.nextInt(256)
        val blue = random.nextInt(256)
        return Color.rgb(red, green, blue)
    }
}