package com.dengyun.jiawei.writingboardview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * @title
 * @desc Created by liujiawei on 2018/3/20.
 */

public class WritingBoardView extends View {
    private int paintColor;//画笔颜色
    private int paintWidth;// 画笔宽度
    private int eraserWidth;// 画笔宽度
    private int menuBgColor;// 画笔宽度
    private int boardColor;// 画布背景色
    private int defaultSize = 500;//默认画布尺寸
    private int menuHeight = 150;
    private int realWidth, realHeight;
    private Paint mPaint;//画图画笔
    private Paint menuPaint;//菜单画笔
    private Paint eraserPaint;//橡皮画笔
    private Path mPath;//画笔路径
    private Path eraserPath;//橡皮路径
    private Region globalRegion, leftMenuRegion, RightMenuRegion;// 绑定区域
    private RectF boardRectF;//画布形状
    private Context mContext;
    private int btnTag = 1;// btnTag ==1 的时候为画笔模式，btnTag =2的时候为橡皮擦模式
    private String leftBtnString = "橡皮擦";
    ;
    private String rightBtnString = "重置";
    ;
    private Path menuPath_eraser;// 橡皮菜单画笔路径
    private Path menuPath_reset;// 重置菜单画笔路径
    private RectF menuRectf_reset;//橡皮菜单画笔形状
    private RectF menuRectf_eraser;////重置菜单画笔形状

    public WritingBoardView(Context context) {
        this(context, null);
    }

    public WritingBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mPaint = new Paint();
        menuPaint = new Paint();
        eraserPaint = new Paint();
        mPath = new Path();
        eraserPath = new Path();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WritingBoardView);
        try {
            paintWidth = a.getDimensionPixelSize(R.styleable.WritingBoardView_paintSize, 20);
            eraserWidth = a.getDimensionPixelSize(R.styleable.WritingBoardView_eraserSize, 50);
            paintColor = a.getColor(R.styleable.WritingBoardView_paintColor, getResources().getColor(R.color.colorAccent));
            menuBgColor = a.getColor(R.styleable.WritingBoardView_menuBgColor, getResources().getColor(R.color.colorAccent));
            menuBgColor = a.getColor(R.styleable.WritingBoardView_menuBgColor, getResources().getColor(R.color.colorAccent));
            // 设置画笔模式下的画笔
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(paintColor);
            mPaint.setStrokeWidth(paintWidth);
            // 设置橡皮擦模式下的画笔
            eraserPaint.setAntiAlias(true);
            eraserPaint.setStyle(Paint.Style.STROKE);
            eraserPaint.setColor(Color.WHITE);
            eraserPaint.setStrokeWidth(eraserWidth);
            // 设置菜单字体
            menuPaint.setColor(menuBgColor);
            menuPaint.setAntiAlias(true);
            menuPaint.setStyle(Paint.Style.FILL);
        } finally {
            a.recycle();
        }

    }
    // 1 测量widthMeasureSpec 2，测量heightMeasureSpec
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureSize(1, widthMeasureSpec);
        measureSize(2, heightMeasureSpec);
        setMeasuredDimension(realWidth, realHeight);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        globalRegion = new Region(0, 0, w, h);
        leftMenuRegion = new Region();
        RightMenuRegion = new Region();

        menuPath_eraser = new Path();
        menuPath_reset = new Path();

        boardRectF = new RectF(0, 0, w, h);
        menuRectf_eraser = new RectF(10, 0, realWidth / 2 - 10, menuHeight);
        menuRectf_reset = new RectF(realWidth / 2 + 10, 0, realWidth - 10, menuHeight);
        //圆角矩形
        menuPath_eraser.addRoundRect(menuRectf_eraser, 10, 10, Path.Direction.CW);
        menuPath_reset.addRoundRect(menuRectf_reset, 10, 10, Path.Direction.CW);

    }

    private void measureSize(int type, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                if (type == 1) {
                    realWidth = defaultSize;
                } else {
                    realHeight = defaultSize;
                }
                break;

            case MeasureSpec.EXACTLY:
                if (type == 1) {
                    realWidth = specSize;
                } else {
                    realHeight = specSize;
                }
                break;
            case MeasureSpec.AT_MOST:
                if (type == 1) {
                    realWidth = Math.min(defaultSize, specSize);
                } else {
                    realHeight = Math.min(defaultSize, specSize);
                    ;
                }
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        eraserPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(boardRectF, eraserPaint);
        eraserPaint.setStyle(Paint.Style.STROKE);

        //绑定点击的区域
        leftMenuRegion.setPath(menuPath_eraser, globalRegion);
        RightMenuRegion.setPath(menuPath_reset, globalRegion);
        // 绘制按钮
        menuPaint.setColor(Color.GRAY);
        canvas.drawPath(menuPath_eraser, menuPaint);
        canvas.drawPath(menuPath_reset, menuPaint);

        menuPaint.setColor(Color.WHITE);
        menuPaint.setTextSize(100);
        // 测试字体
        Paint.FontMetrics fontMetrics = menuPaint.getFontMetrics();
        float fontWidth = menuPaint.measureText(leftBtnString);
        // 绘制按钮文字
        //按钮字体居中
        //文字绘制居中坐标 y  =  targetRect.centerY() - fm.descent+ (fm.descent - fm.ascent) / 2
        canvas.drawText(leftBtnString, (realWidth / 2 - fontWidth) / 2, menuRectf_eraser.centerY() + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent, menuPaint);
        fontWidth = menuPaint.measureText(rightBtnString);
        canvas.drawText(rightBtnString, realWidth / 2 + (realWidth / 2 - fontWidth) / 2, menuRectf_eraser.centerY() + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent, menuPaint);

        // 绘制画笔的路径
        canvas.drawPath(mPath, mPaint);
        // 绘制橡皮擦的路径
        canvas.drawPath(eraserPath, eraserPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int px = (int) x;
        int py = (int) y;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (leftMenuRegion.contains(px, py)) {
                } else if (RightMenuRegion.contains(px, py)) {

                } else {
                    if (btnTag == 1) {
                        mPath.moveTo(x, y);
                    } else {
                        eraserPath.moveTo(x, y);
                    }


                }

                break;
            case MotionEvent.ACTION_UP:
                // Region.contains(x,y) 方法来判断是否按钮被点击
                if (leftMenuRegion.contains(px, py)) {
                    Toast.makeText(mContext, "left", Toast.LENGTH_SHORT).show();
                    if (btnTag == 1) {
                        btnTag = 2;
                        leftBtnString = "画笔";
                    } else {
                        btnTag = 1;
                        leftBtnString = "橡皮擦";
                    }


                } else if (RightMenuRegion.contains(px, py)) {
                    Toast.makeText(mContext, "right", Toast.LENGTH_SHORT).show();
                    mPath.reset();
                    eraserPath.reset();

                } else {
                    if (btnTag == 1) {
                        mPath.lineTo(x, y);
                    } else {
                        eraserPath.lineTo(x, y);
                    }

                }


                break;
            case MotionEvent.ACTION_MOVE:
                if (leftMenuRegion.contains(px, py)) {
                    Toast.makeText(mContext, "切换成功", Toast.LENGTH_SHORT).show();
                } else if (RightMenuRegion.contains(px, py)) {
                    Toast.makeText(mContext, "重置成功", Toast.LENGTH_SHORT).show();
                } else {
                    if (btnTag == 1) {
                        mPath.lineTo(x, y);
                    } else {
                        eraserPath.lineTo(x, y);
                    }
                }


                break;
        }
        //通知重绘
        invalidate();
        return true;
    }
}
