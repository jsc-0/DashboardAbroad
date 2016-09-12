package com.jsc0.dashboardabroad;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;

/**
 * 仪表盘View
 *
 * @author woxingxiao
 */
public class DashboardView extends View {

    private int mRadius; // 圆弧半径
    private int mStartAngle; // 起始角度
    private int mSweepAngle; // 绘制角度
    private int mBigSliceCount; // 大份数
    private int mArcColor; // 弧度颜色
    private int mMeasureTextSize; // 刻度字体大小
    private int mTextColor; // 字体颜色
    private int mHeaderTextSize; // 表头字体大小
    private int mPointerRadius; // 指针半径
    private int mCircleRadius; // 中心圆半径
    private int mMinValue; // 最小值
    private int mMaxValue; // 最大值
    private float mRealTimeValue = 0.0f; // 实时值;不能写在里面回报错
    private int mStripeWidth; // 色条宽度
    private StripeMode mStripeMode;
    private int mBigSliceRadius; // 刻度半径
    private int mNumMeaRadius; // 数字刻度半径
    private int mModeType;
    private int mBgColor; // 背景色

    private int mViewWidth; // 控件宽度
    private int mViewHeight; // 控件高度
    private float mCenterX;//中心x
    private float mCenterY;//中心y

    private float mBigSliceAngle; // 大刻度等分角度
    private float initAngle;//每次显示的角度
    private boolean textColorFlag = true;

    private Paint mPaintArc;//刻度参数，第二层圆线
    private Paint mPaintArcSlipAbroadGray;//最外层圆线
    private Paint mPaintArcSlip;//滑动填充的半圆粗
    private Paint mPaintArcSlipAbroad;//滑动填充的半圆细
    private Paint mPaintText;//刻度字参数
    private Paint mPaintPointer;//中心圆参数
    private Paint mPaintValue;//绘制读数

    private RectF mRectArc;
    private RectF mRectStripe;
    private Rect mRectMeasures;
    private Rect mRectRealText;
    private Path path;
    private RectF mRectArcAbroad;

    private String[] mGraduations = new String[] { "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月","12月", "18月" }; // 等分的刻度值
    private boolean initflag = false;// 滑动判断
    private DegreeCallback degreecallback;// 滑动回调
    private int angle;// 进度
    private int text;//获取的值
    private Context mContext;
    private int heightSize;
    private int widthSize;
    private int outerSpace = 10;//最外层间距
    private int internalSpacing  = outerSpace+20;//细线条和粗线条间距
    private boolean defaltSchedule  = false;//细线条和粗线条间距


    public DashboardView(Context context) {
        this(context, null);
        mContext = context;
    }

    public DashboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public DashboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public abstract interface DegreeCallback {
        public abstract void Degree(int degree);
    }

    private void init(int widthSize) {

        mRadius = widthSize/2-10; // 圆弧半径
        mStartAngle = 180; // 起始角度
        mSweepAngle = 180; // 绘制角度
        mPointerRadius = mRadius / 3 * 2; // 指针半径
        mCircleRadius = mRadius / 17; // 中心圆半径
        mBigSliceCount = 12; // 大份数
        mArcColor = Color.parseColor("#cccccc"); // 弧度颜色
        mMeasureTextSize = spToPx(14); // 刻度字体大小
        mTextColor = Color.parseColor("#cccccc"); // 刻度字体颜色
        mHeaderTextSize = spToPx(14); // 表头字体大小
        mMinValue = 0; // 最小值
        mMaxValue = 12; // 最大值
        mStripeWidth = 20; // 色条宽度
        mStripeMode = StripeMode.NORMAL;
        mModeType = 2;
        mBgColor = Color.WHITE; // 背景色

        if (mSweepAngle > 360)
            throw new IllegalArgumentException("sweepAngle must less than 360 degree");

        mBigSliceRadius = mRadius-internalSpacing-mStripeWidth-30;//初始化刻度的长度
        mNumMeaRadius = mBigSliceRadius - dpToPx(8);//初始化字的长度

        mBigSliceAngle = mSweepAngle / (float) mBigSliceCount;

        switch (mModeType) {
            case 0:
                mStripeMode = StripeMode.NORMAL;
                break;
            case 1:
                mStripeMode = StripeMode.INNER;
                break;
            case 2:
                mStripeMode = StripeMode.OUTER;
                break;
        }

        int totalRadius;
        if (mStripeMode == StripeMode.OUTER) {
            totalRadius = mRadius+outerSpace;
        } else {
            totalRadius = mRadius;
        }

        mCenterX = mCenterY = 0.0f;
        if (mStartAngle <= 180 && mStartAngle + mSweepAngle >= 180) {
            mViewWidth = totalRadius * 2;
        } else {
            float[] point1 = getCoordinatePoint(totalRadius, mStartAngle);
            float[] point2 = getCoordinatePoint(totalRadius, mStartAngle + mSweepAngle);
            float max = Math.max(Math.abs(point1[0]), Math.abs(point2[0]));
            mViewWidth = (int) (max * 2);
        }
        if ((mStartAngle <= 90 && mStartAngle + mSweepAngle >= 90)
                || (mStartAngle <= 270 && mStartAngle + mSweepAngle >= 270)) {
            mViewHeight = totalRadius * 2;
        } else {
            float[] point1 = getCoordinatePoint(totalRadius, mStartAngle);
            float[] point2 = getCoordinatePoint(totalRadius, mStartAngle + mSweepAngle);
            float max = Math.max(Math.abs(point1[1]), Math.abs(point2[1]));
            mViewHeight = (int) (max * 2);
        }

        mCenterX = mViewWidth / 2.0f;
        mCenterY = mViewHeight / 2.0f;

        mPaintArc = new Paint();
        mPaintArc.setAntiAlias(true);
        mPaintArc.setColor(mArcColor);
        mPaintArc.setStyle(Paint.Style.STROKE);
        mPaintArc.setStrokeCap(Paint.Cap.ROUND);
        mPaintArc.setStrokeWidth(mStripeWidth);

        mPaintArcSlip = new Paint();
        mPaintArcSlip.setAntiAlias(true);
        mPaintArcSlip.setColor(Color.parseColor("#23a7f1"));
        mPaintArcSlip.setStyle(Paint.Style.STROKE);
        mPaintArcSlip.setStrokeCap(Paint.Cap.ROUND);
        mPaintArcSlip.setStrokeWidth(mStripeWidth);

        mPaintArcSlipAbroad = new Paint();
        mPaintArcSlipAbroad.setAntiAlias(true);
        mPaintArcSlipAbroad.setColor(Color.parseColor("#23a7f1"));
        mPaintArcSlipAbroad.setStyle(Paint.Style.STROKE);
        mPaintArcSlipAbroad.setStrokeCap(Paint.Cap.ROUND);
        mPaintArcSlipAbroad.setStrokeWidth(3);

        mPaintArcSlipAbroadGray = new Paint();
        mPaintArcSlipAbroadGray.setAntiAlias(true);
        mPaintArcSlipAbroadGray.setColor(Color.parseColor("#f1f1f1"));
        mPaintArcSlipAbroadGray.setStyle(Paint.Style.STROKE);
        mPaintArcSlipAbroadGray.setStrokeCap(Paint.Cap.ROUND);
        mPaintArcSlipAbroadGray.setStrokeWidth(3);

        mPaintText = new Paint();
        mPaintText.setAntiAlias(true);
        mPaintText.setColor(mTextColor);
        mPaintText.setStyle(Paint.Style.FILL);

        mPaintPointer = new Paint();
        mPaintPointer.setAntiAlias(true);

//        mRectArcAbroad = new RectF(mCenterX - mRadius - 20, mCenterY - mRadius - 20, mCenterX + mRadius + 20,mCenterY + mRadius + 20);
//        mRectArc = new RectF(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);

        mRectArcAbroad = new RectF(mCenterX - mRadius+outerSpace, mCenterY - mRadius + outerSpace, mCenterX+mRadius,mCenterY + mRadius + outerSpace);
        mRectArc = new RectF(mCenterX - mRadius+internalSpacing, mCenterY - mRadius + internalSpacing, mCenterX + mRadius+outerSpace-internalSpacing, mCenterY + mRadius+internalSpacing);

        int r = 0;
        if (mStripeWidth > 0) {
            if (mStripeMode == StripeMode.OUTER) {
                r = mRadius + dpToPx(1) + mStripeWidth / 2;
            } else if (mStripeMode == StripeMode.INNER) {
                r = mRadius + dpToPx(1) - mStripeWidth / 2;
            }
            mRectStripe = new RectF(mCenterX - r, mCenterY - r, mCenterX + r, mCenterY + r);
        }
        mRectMeasures = new Rect();
        mRectRealText = new Rect();
        path = new Path();

        mPaintValue = new Paint();
        mPaintValue.setAntiAlias(true);
        mPaintValue.setColor(mTextColor);
        mPaintValue.setStyle(Paint.Style.STROKE);
        mPaintValue.setTextAlign(Paint.Align.CENTER);
        mPaintValue.setTextSize(Math.max(mHeaderTextSize, mMeasureTextSize));
        mPaintValue.getTextBounds(trimFloat(mRealTimeValue), 0, trimFloat(mRealTimeValue).length(), mRectRealText);

        initAngle = getAngleFromResult(mRealTimeValue);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        heightSize = MeasureSpec.getSize(heightMeasureSpec);
        init(widthSize);
        if (widthMode == MeasureSpec.EXACTLY) {
            mViewWidth = dpToPx(widthSize);
        } else {
            if (widthMode == MeasureSpec.AT_MOST)
                mViewWidth = Math.min(mViewWidth, widthSize);
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            mViewHeight = dpToPx(heightSize);
        } else {
            int totalRadius;
            if (mStripeMode == StripeMode.OUTER) {
                totalRadius = mRadius + outerSpace;
            } else {
                totalRadius = mRadius;
            }
            if (mStartAngle >= 180 && mStartAngle + mSweepAngle <= 360) {
                mViewHeight = totalRadius + mCircleRadius + dpToPx(2) + dpToPx(25) + getPaddingTop()
                        + getPaddingBottom() + mRectRealText.height();
            } else {
                float[] point1 = getCoordinatePoint(totalRadius, mStartAngle);
                float[] point2 = getCoordinatePoint(totalRadius, mStartAngle + mSweepAngle);
                float maxY = Math.max(Math.abs(point1[1]) - mCenterY, Math.abs(point2[1]) - mCenterY);
                float f = mCircleRadius + dpToPx(2) + dpToPx(25) + mRectRealText.height();
                float max = Math.max(maxY, f);
                mViewHeight = (int) (max + totalRadius + getPaddingTop() + getPaddingBottom() + dpToPx(2) * 2);
            }
            if (widthMode == MeasureSpec.AT_MOST)
                mViewHeight = Math.min(mViewHeight, widthSize);
        }
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBgColor != 0)
            canvas.drawColor(mBgColor);
        drawDial(canvas);
        drawArc(canvas);
        drawPointer(canvas);
        if(!defaltSchedule){
            startAnimation();
        }
//        drawDot(canvas);
    }

    /**
     *  绘制刻度盘
     * @param canvas
     */
    private void drawDial(Canvas canvas) {
        mPaintArc.setStrokeWidth(dpToPx(1));
        for (int i = 0; i <= mBigSliceCount; i++) {
            // 绘制大刻度
            float angle = i * mBigSliceAngle + mStartAngle;//数量*单个的角度+起始角度
            float[] point1 = getCoordinatePoint(mRadius-internalSpacing, angle);//圆弧半径
            float[] point2 = getCoordinatePoint(mBigSliceRadius, angle);//刻度半径

            if(i<text+1){
                mPaintArc.setColor(Color.parseColor("#ff8096"));
            }else{
                mPaintArc.setColor(Color.parseColor("#cccccc"));
            }
            canvas.drawLine(point1[0], point1[1], point2[0], point2[1], mPaintArc);

//             绘制圆盘上的数字
            mPaintText.setTextSize(mMeasureTextSize);
            String number = mGraduations[i];
            mPaintText.getTextBounds(number, 0, number.length(), mRectMeasures);
            if (angle % 360 > 135 && angle % 360 < 215) {
                mPaintText.setTextAlign(Paint.Align.LEFT);
            } else if ((angle % 360 >= 0 && angle % 360 < 45) || (angle % 360 > 325 && angle % 360 <= 360)) {
                mPaintText.setTextAlign(Paint.Align.RIGHT);
            } else {
                mPaintText.setTextAlign(Paint.Align.CENTER);
            }
            float[] numberPoint = getCoordinatePoint(mNumMeaRadius, angle);
            if(i<text+1){
                mPaintText.setColor(Color.parseColor("#ff8096"));
            }else{
                mPaintText.setColor(Color.parseColor("#cccccc"));
            }
            if (i == 0 || i == mBigSliceCount) {
                canvas.drawText(number, numberPoint[0], numberPoint[1] + (mRectMeasures.height() / 2), mPaintText);
            } else {
                canvas.drawText(number, numberPoint[0], numberPoint[1] + mRectMeasures.height(), mPaintText);
            }
        }
    }

    /**
     * 绘制刻度盘的弧形、刻度盘线的弧形、刻度盘滑动的弧形和滑动线的弧形
     * @param canvas
     */
    private void drawArc(Canvas canvas) {
        mPaintArc.setStrokeWidth(dpToPx(10));
        mPaintArc.setColor(Color.parseColor("#f1f1f1"));
        if (mStripeMode == StripeMode.NORMAL) {
            canvas.drawArc(mRectArc, mStartAngle, mSweepAngle, false, mPaintArc);
            canvas.drawArc(mRectArcAbroad, mStartAngle, mSweepAngle, false, mPaintArcSlipAbroadGray);
            canvas.drawArc(mRectArc, 180, 5, false, mPaintArcSlip);//内圈
            canvas.drawArc(mRectArcAbroad, 180, 5, false, mPaintArcSlipAbroad);//外圈
            canvas.drawArc(mRectArc, mStartAngle, initAngle - 180, false, mPaintArcSlip);
            canvas.drawArc(mRectArcAbroad, mStartAngle, initAngle - 180, false, mPaintArcSlipAbroad);
        } else if (mStripeMode == StripeMode.OUTER) {
            canvas.drawArc(mRectArc, mStartAngle, mSweepAngle, false, mPaintArc);//内圈
            canvas.drawArc(mRectArcAbroad, mStartAngle, mSweepAngle, false, mPaintArcSlipAbroadGray);//外圈
            canvas.drawArc(mRectArc, 180, 5, false, mPaintArcSlip);//内圈
            canvas.drawArc(mRectArcAbroad, 180, 5, false, mPaintArcSlipAbroad);//外圈
            canvas.drawArc(mRectArc, mStartAngle, initAngle - 180, false, mPaintArcSlip);//内圈滑
            canvas.drawArc(mRectArcAbroad, mStartAngle, initAngle - 180, false, mPaintArcSlipAbroad);//外圈滑
        }
        canvas.save();
    }


    /**
     * 绘制指针
     * @param canvas
     */
    private void drawPointer(Canvas canvas) {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.animation_pointer);
        Bitmap ssbm = roateBitmap(bitmap);
        canvas.translate(mCenterX, mCenterY);
        canvas.rotate((float) (initAngle - 180));//旋转47度
        canvas.translate(-mCenterX, -mCenterY);
        canvas.drawBitmap(ssbm,mCenterX-ssbm.getWidth()+outerSpace+mCircleRadius,mCenterY-ssbm.getHeight()/2,mPaintPointer);
        canvas.restore();
        canvas.save();
    }

    private void startAnimation() {
        final ValueAnimator anim = new ValueAnimator();
        anim.setInterpolator(new BounceInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                defaltSchedule = true;
                float value = (float)animation.getAnimatedValue();
                setRealTimeValue(value);
                text = (int) value;
                invalidate();
            }
        });
        anim.setFloatValues(0, 11);
        anim.setRepeatCount(0);
        anim.setDuration(2000);
        anim.start();
    }

    /**
     * 绘制圆点
     * @param canvas
     */
    private void drawDot(Canvas canvas) {
        mPaintPointer.setStyle(Paint.Style.STROKE);
        mPaintPointer.setStrokeWidth(dpToPx(4));
        mPaintPointer.setColor(Color.parseColor("#f1f1f1"));
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadius + dpToPx(4), mPaintPointer);//第一圈灰
        mPaintPointer.setColor(Color.parseColor("#ff8069"));
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadius + dpToPx(2), mPaintPointer);//第二圈红

//         绘制三角形指针
        mPaintPointer.setStyle(Paint.Style.FILL);
        mPaintPointer.setColor(Color.parseColor("#ff8069"));
        float[] point1 = getCoordinatePoint(mCircleRadius / 2, initAngle + 90);
        float[] point2 = getCoordinatePoint(mCircleRadius / 2, initAngle - 90);
        float[] point3 = getCoordinatePoint(mPointerRadius, initAngle);
        path.moveTo(point1[0], point1[1]);
        path.lineTo(point2[0], point2[1]);
        path.lineTo(point3[0], point3[1]);
        path.close();
        canvas.drawPath(path, mPaintPointer);
//         绘制三角形指针底部的圆弧效果
        canvas.drawCircle((point1[0] + point2[0]) / 2, (point1[1] + point2[1]) / 2, mCircleRadius / 2,mPaintPointer);

//         绘制中心点的圆
        mPaintPointer.setStyle(Paint.Style.FILL);
        mPaintPointer.setColor(Color.parseColor("#ffffff"));
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mPaintPointer);
        canvas.save();
//         绘制读数
         canvas.drawText(trimFloat(mRealTimeValue), mCenterX,
         mCenterY + mCircleRadius + dpToPx(2) + dpToPx(25), mPaintValue);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    /**
     * 旋转图片
     *
     * @param bitmap
     * @return
     */
    private Bitmap roateBitmap(Bitmap bitmap) {
        // 获得图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) mPointerRadius+mCircleRadius*2) /width;
        float scaleHeight = ((float) mPointerRadius+mCircleRadius*2) /width;
        // 取得想要缩放的matrix参数
        Matrix matrix1 = new Matrix();
        matrix1.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height,matrix1, true);
//        Bitmap bitmap2;
//        Matrix matrix = new Matrix();
        // matrix.postTranslate(arcPoint.x - drawableWidth / 2, arcPoint.y
        // - drawableHeight / 2);
//        matrix.postRotate(90);
        // matrix.setRotate((float) degree-90, arcPoint.x, arcPoint.y);
//        bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return newbm;
    }

    /**
     * 依圆心坐标，半径，扇形角度，计算出扇形终射线与圆弧交叉点的xy坐标
     */
    public float[] getCoordinatePoint(int radius, float cirAngle) {
        float[] point = new float[2];

        double arcAngle = Math.toRadians(cirAngle); // 将角度转换为弧度
        if (cirAngle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);//中心x+余弦*半径
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);//中心y+正弦*半径
        } else if (cirAngle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (cirAngle > 90 && cirAngle < 180) {
            arcAngle = Math.PI * (180 - cirAngle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (cirAngle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (cirAngle > 180 && cirAngle < 270) {
            arcAngle = Math.PI * (cirAngle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (cirAngle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - cirAngle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }

    /**
     * 通过数值得到角度位置
     */
    private float getAngleFromResult(float result) {
        if (result > mMaxValue)
            return mMaxValue;
        return mSweepAngle * (result - mMinValue) / (mMaxValue - mMinValue) + mStartAngle;
    }

    public void setCallback(DegreeCallback callback) {
        this.degreecallback = callback;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        initflag = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moved(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                moved(x, y);
                break;
            case MotionEvent.ACTION_UP:
                initflag = false;
                moved(x, y);
                break;
            case MotionEvent.ACTION_CANCEL:
                initflag = false;
                moved(x, y);
                break;
        }
        return true;
    }

    /**
     * Moved.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     *
     */
    private void moved(float x, float y) {
        float distance = (float) Math.sqrt(Math.pow((x - mCenterX), 2) + Math.pow((y - mCenterY), 2));
        // IS_PRESSED = true;
        if (distance < (mRadius * 2 + 20)) {
            float degrees = (float) ((float) ((Math.toDegrees(Math.atan2(mCenterX - x, y - mCenterY)) + 360.0)) % 360.0);

            if (degrees < 0) {
                degrees += 2 * Math.PI;
            }
            if (degrees <= 30 || degrees >= 330) {
                return;
            }
            setAngle(Math.round(degrees-60));
            invalidate();
        }

    }

    /**
     * Set the angle.
     *
     * @param angle
     *            the new angle
     */
    public void setAngle(int angle) {
        if (angle > 0) {
            int divideEqually = 180 / 12;
            double result = (angle-divideEqually)/ divideEqually;
            text = (int) result;
        } else {
            text = 0;
        }
        if (text > 12) {
            text = 12;
        }
        if (this.degreecallback != null) {
            this.degreecallback.Degree(text);
        }
        this.setRealTimeValue(text);
        this.angle = angle;
    }

    /**
     * Get the angle.
     *
     * @return the angle
     */
    public int getAngle() {
        return angle;
    }

    /**
     * The listener interface for receiving onSeekChange events. The class that
     * is interested in processing a onSeekChange event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>setSeekBarChangeListener(OnSeekChangeListener)<code> method. When
     * the onSeekChange event occurs, that object's appropriate method is
     * invoked.
     */
    public interface OnSeekChangeListener {

        /**
         * On progress change.
         *
         * @param view
         *            the view
         * @param newProgress
         *            the new progress
         */
        public void onProgressChange(DashboardView view, int newProgress);
    }

    /**
     * The listener to listen for changes
     */
    private OnSeekChangeListener mListener = new OnSeekChangeListener() {

        @Override
        public void onProgressChange(DashboardView view, int newProgress) {

        }
    };

    /**
     * float类型如果小数点后为零则显示整数否则保留
     */
    public static String trimFloat(float value) {
        if (Math.round(value) - value == 0) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int radius) {
//        mRadius = dpToPx(radius);
        mRadius = radius;
        init(widthSize);
    }

    public int getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(int startAngle) {
        mStartAngle = startAngle;
        init(widthSize);
    }

    public int getSweepAngle() {
        return mSweepAngle;
    }

    public void setSweepAngle(int sweepAngle) {
        mSweepAngle = sweepAngle;
        init(widthSize);
    }

    public int getBigSliceCount() {
        return mBigSliceCount;
    }

    public void setBigSliceCount(int bigSliceCount) {
        mBigSliceCount = bigSliceCount;
        init(widthSize);
    }

    public int getArcColor() {
        return mArcColor;
    }

    public void setArcColor(int arcColor) {
        mArcColor = arcColor;
        if (textColorFlag)
            mTextColor = mArcColor;
        init(widthSize);
    }

    public int getMeasureTextSize() {
        return mMeasureTextSize;
    }

    public void setMeasureTextSize(int measureTextSize) {
        mMeasureTextSize = spToPx(measureTextSize);
        init(widthSize);
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        textColorFlag = false;
        init(widthSize);
    }


    public int getHeaderTextSize() {
        return mHeaderTextSize;
    }

    public void setHeaderTextSize(int headerTextSize) {
        mHeaderTextSize = spToPx(headerTextSize);
        init(widthSize);
    }

    public int getPointerRadius() {
        return mPointerRadius;
    }

    public void setPointerRadius(int pointerRadius) {
        mPointerRadius = dpToPx(pointerRadius);
        init(widthSize);
    }

    public int getCircleRadius() {
        return mCircleRadius;
    }

    public void setCircleRadius(int circleRadius) {
        mCircleRadius = dpToPx(circleRadius);
        init(widthSize);
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
        init(widthSize);
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;
        init(widthSize);
    }

    public float getRealTimeValue() {
        return mRealTimeValue;
    }

    public void setRealTimeValue(float realTimeValue) {
        mRealTimeValue = realTimeValue;
        init(widthSize);
    }

    public int getStripeWidth() {
        return mStripeWidth;
    }

    public void setStripeWidth(int stripeWidth) {
        mStripeWidth = dpToPx(stripeWidth);
        init(widthSize);
    }

    public StripeMode getStripeMode() {
        return mStripeMode;
    }

    public void setStripeMode(StripeMode mStripeMode) {
        this.mStripeMode = mStripeMode;
        switch (mStripeMode) {
            case NORMAL:
                mModeType = 0;
                break;
            case INNER:
                mModeType = 1;
                break;
            case OUTER:
                mModeType = 2;
                break;
        }
        init(widthSize);
    }

    public int getBigSliceRadius() {
        return mBigSliceRadius;
    }

    public void setBigSliceRadius(int bigSliceRadius) {
        mBigSliceRadius = dpToPx(bigSliceRadius);
        init(widthSize);
    }

    public int getNumMeaRadius() {
        return mNumMeaRadius;
    }

    public void setNumMeaRadius(int numMeaRadius) {
        mNumMeaRadius = dpToPx(numMeaRadius);
        init(widthSize);
    }

    public enum StripeMode {
        NORMAL, INNER, OUTER
    }

    public int getBgColor() {
        return mBgColor;
    }

    public void setBgColor(int mBgColor) {
        this.mBgColor = mBgColor;
        init(widthSize);
    }

    /**
     * Sets the seek bar change listener.
     *
     * @param listener
     *            the new seek bar change listener
     */
    public void setSeekBarChangeListener(OnSeekChangeListener listener) {
        mListener = listener;
    }

    /**
     * Gets the seek bar change listener.
     *
     * @return the seek bar change listener
     */
    public OnSeekChangeListener getSeekBarChangeListener() {
        return mListener;
    }
}