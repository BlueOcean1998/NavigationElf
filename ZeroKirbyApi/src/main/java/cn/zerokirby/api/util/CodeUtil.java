package cn.zerokirby.api.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Random;

/**
 * 生成验证码工具类
 */
public class CodeUtil {

    //随机码集
    private final static char[] CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm',
            'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };
    //验证码个数
    private final static int CODE_LENGTH = 4;
    //字体大小
    private final static int FONT_SIZE = 50;
    //线条数
    private final static int LINE_NUMBER = 5;
    //padding，其中base的意思是初始值，而range是变化范围。数值根据自己想要的大小来设置
    private final static int BASE_PADDING_LEFT = 10, RANGE_PADDING_LEFT = 100, BASE_PADDING_TOP = 75, RANGE_PADDING_TOP = 50;
    //验证码默认宽高
    private final static int DEFAULT_WIDTH = 360, DEFAULT_HEIGHT = 120;
    //随机数
    private final static Random random = new Random();

    private String code;//生成的验证码
    private int padding_left, padding_top;

    /**
     * 生成验证码图片
     *
     * @return 位图
     */
    public Bitmap createBitmap() {
        padding_left = 0;
        padding_top = 0;
        //创建指定格式，大小的位图//Config.ARGB_8888是一种色彩的存储方法
        Bitmap bp = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bp);

        code = createCode();
        //将画布填充为白色
        c.drawColor(android.graphics.Color.WHITE);
        //新建一个画笔
        Paint paint = new Paint();
        //设置画笔抗锯齿
        paint.setAntiAlias(true);
        paint.setTextSize(FONT_SIZE);

        for (int i = 0; i < code.length(); i++) {
            randomTextStyle(paint);
            randomPadding();
            //这里的padding_left,padding_top是文字的基线
            c.drawText(code.charAt(i) + "", padding_left, padding_top, paint);
        }
        //画干扰线
        for (int i = 0; i < LINE_NUMBER; i++) {
            drawLine(c, paint);
        }
        //保存一下画布
        c.save();
        c.restore();
        return bp;
    }

    /**
     * 获取生成的验证码
     *
     * @return 验证码
     */
    public String getCode() {
        return code;
    }

    //生成验证码
    private String createCode() {
        StringBuilder sb = new StringBuilder();
        //利用random生成随机下标
        //验证码个数，线条数，字体大小
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    //随机文字样式，颜色，文字粗细与倾斜度
    private void randomTextStyle(Paint paint) {
        int color = randomColor();
        paint.setColor(color);
        paint.setFakeBoldText(random.nextBoolean());//true为粗体，false为非粗体
        float skew = random.nextFloat() / 2;
        //随机ture或者false来生成正数或者负数，来表示文字的倾斜度，负数右倾，正数左倾
        skew = random.nextBoolean() ? skew : -skew;
        paint.setTextSkewX(skew);
    }

    //验证码位置随机
    private void randomPadding() {
        //字体的随机位置
        padding_left += BASE_PADDING_LEFT + random.nextInt(RANGE_PADDING_LEFT);
        padding_top = BASE_PADDING_TOP + random.nextInt(RANGE_PADDING_TOP);
    }

    //画干扰线
    private void drawLine(Canvas canvas, Paint paint) {
        int color = randomColor();
        int startX = random.nextInt(DEFAULT_WIDTH);
        int startY = random.nextInt(DEFAULT_HEIGHT);
        int stopX = random.nextInt(DEFAULT_WIDTH);
        int stopY = random.nextInt(DEFAULT_HEIGHT);
        paint.setStrokeWidth(1);
        paint.setColor(color);
        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }

    //生成随机颜色
    private int randomColor() {
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return android.graphics.Color.rgb(red, green, blue);
    }

}
