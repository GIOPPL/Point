package com.gioppl.pplpoint;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by GIOPPL on 2017/8/2.
 */

public class MyPoint extends FrameLayout{
    private Context mContext;
//    private FrameLayout mFrameLayout;
    private Handler mHandler;

    private Paint mCirclePaint;//画红点
    private Paint mTextPaint;//画文本
    private Paint mBezierPaint;//贝塞尔曲线的画笔
    private Point mAnchor;//锚点
    private Point P0;
    private Point P1;
    private Point P2;
    private Point P3;
    private int CIRCLE_RADIUS_ONE=50;//第一个圆的半径,随手指移动
    private int CIRCLE_RADIUS_TWO=50;//第二个圆的半径，固定不动
    private Path mPath;//贝塞尔曲线
    //爆炸效果图片
    private ImageView im_pop;

    //圆的属性
    private float p5x=100;//第二个随手指移动的圆
    private float p5y=100;
    private float p6x=100;//第一个固定的圆
    private float p6y=100;
    //两个圆心的距离
    private double distance;
    //是否已经消失
    private boolean showView=true;
    private boolean showCircle=true;
    //圆的颜色
    private int circleColor;
    //消息数量
    private String circleText;
    //文本颜色
    private int textColor;
    //爆炸和弹回的临界值
    private int CRITICALITY;
    //文本的大小
    private float textSize;

    /**
     * 设置参数信息
     * @param radius
     * @param circleColor
     * @param textColor
     * @param text
     */
    public void setParameter(int radius,int circleColor,int textColor,String text){
        this.CIRCLE_RADIUS_ONE=this.CIRCLE_RADIUS_TWO=radius;
        this.circleColor=circleColor;
        this.textColor=textColor;
        this.circleText=text;
        mTextPaint.setColor(textColor);
        mCirclePaint.setColor(circleColor);
        mBezierPaint.setColor(circleColor);

    }



    //设置第二个圆的位置
    private void setP5(float p5x,float p5y){
        this.p5x=p5x;
        this.p5y=p5y;
    }
    //设置锚点的位置
    private void setAnchor(int x,int y){
        mAnchor.x=x;
        mAnchor.y=y;
    }
    //设置固定圆的半径
    private void setDistance(float dx, float dy) {
        distance=Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));
        CIRCLE_RADIUS_TWO= CIRCLE_RADIUS_ONE-(int) (distance/15);
    }

    public MyPoint(Context context) {
        super(context,null);
        this.mContext=context;
        init();
    }

    public MyPoint(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
        initConfig(attrs);
        init();

    }
    public MyPoint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        init();
        initConfig(attrs);
    }

    public MyPoint(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext=context;
        init();
        initConfig(attrs);
    }

    private void initConfig(AttributeSet attrs) {
        //获取配置文件中的数据
        TypedArray typeArray =mContext.obtainStyledAttributes(attrs,R.styleable.MyPoint);
        CIRCLE_RADIUS_ONE=CIRCLE_RADIUS_TWO= (int) typeArray.getInteger(R.styleable.MyPoint_circle_radius,50);
        circleColor=typeArray.getColor(R.styleable.MyPoint_circle_color,Color.RED);
        circleText=typeArray.getString(R.styleable.MyPoint_circle_text);
        textColor=typeArray.getColor(R.styleable.MyPoint_text_color,Color.WHITE);
        CRITICALITY=CIRCLE_RADIUS_TWO*10;
        double size=1.0;
        for (int i=0;i<circleText.length();i++){//加上此循环可以动态的变其中的字的大小
            size+=0.1;
        }
        textSize= (float) (CIRCLE_RADIUS_TWO/size);
    }


    private void init() {
        mHandler = new Handler(mContext.getMainLooper());
        P0=new Point();
        P1=new Point();
        P2=new Point();
        P3=new Point();
        mBezierPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mBezierPaint.setColor(circleColor);
        mPath=new Path();
        mAnchor=new Point();
        mCirclePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(circleColor);
        mTextPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
        //设置爆炸效果
        im_pop=new ImageView(getContext());
        LayoutParams params=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        im_pop.setLayoutParams(params);
        im_pop.setVisibility(INVISIBLE);
        im_pop.setBackgroundResource(R.drawable.pop);
        this.addView(im_pop);

    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (showCircle){
            canvas.drawCircle(p5x,p5y,CIRCLE_RADIUS_ONE,mCirclePaint);//画第一个圆,动圆
        }
        if (showView){
            canvas.drawCircle(p6x,p6y,CIRCLE_RADIUS_TWO,mCirclePaint);//画第一个圆，定圆
            canvas.drawText(circleText+"",p5x,p5y+10,mTextPaint);//画消息的数量
            calculateP0_P4();
            //画贝塞尔曲线
            mPath.reset();
            mPath.moveTo(P0.x,P0.y);
            mPath.quadTo(mAnchor.x,mAnchor.y,P2.x,P2.y);
            mPath.lineTo(P3.x,P3.y);
            mPath.quadTo(mAnchor.x,mAnchor.y,P1.x,P1.y);
            mPath.close();
            canvas.drawPath(mPath,mBezierPaint);
        }
    }
    //计算P0-P4的坐标
    private void calculateP0_P4() {
        float dx= (int) (p5x-p6x);
        float dy= (int) (p5y-p6y);
        if (dy==0)
            dy=0.001f;
        setDistance(dx,dy);
        double a=Math.atan(dx/dy);
        //第一个点的坐标
        P0.x= (int) (p6x+CIRCLE_RADIUS_TWO*Math.cos(a));
        P0.y= (int) (p6y-CIRCLE_RADIUS_TWO*Math.sin(a));
        //第二个点的坐标
        P1.x= (int) (p6x-CIRCLE_RADIUS_TWO*Math.cos(a));
        P1.y= (int) (p6y+CIRCLE_RADIUS_TWO*Math.sin(a));
        //第三个点
        P2.x= (int) (p5x+CIRCLE_RADIUS_ONE*Math.cos(a));
        P2.y= (int) (p5y-CIRCLE_RADIUS_ONE*Math.sin(a));
        //第四个点
        P3.x= (int) (p5x-CIRCLE_RADIUS_ONE*Math.cos(a));
        P3.y= (int) (p5y+CIRCLE_RADIUS_ONE*Math.sin(a));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x=event.getX();//手指移动的x坐标
        float y=event.getY();//手指移动的y的坐标
        int dis= (int) Math.sqrt(Math.pow(x,2)+Math.pow(y,2));
        if (dis>CRITICALITY){
            startPop(x,y);
            return true;
        }
        setP5(x,y);//设置p5点
        setAnchor((int) (x+p6x)/2,(int) (y+p6y)/2);//设置锚点坐标
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN: //手指按下
                break;
            case MotionEvent.ACTION_UP://手指抬起
                if(distance<CRITICALITY){//如果在200以内，返回
                    setP5(100,100);

                }
                break;
            case MotionEvent.ACTION_MOVE://手指移动
                if(distance>CRITICALITY){
                    startPop(x,y);//如果大于定义的距离，调用爆炸方法
                }
        }
        postInvalidate();//刷新界面
        return true;
    }

    private void startPop(float x, float y) {
        showView=false;//不在画圆和贝塞尔曲线
        im_pop.setX(x-im_pop.getWidth()/2);//爆炸显示
        im_pop.setY(y-im_pop.getHeight()/2);
        im_pop.setVisibility(VISIBLE);
        AnimationDrawable drawable=(AnimationDrawable)im_pop.getBackground();
        drawable.start();
        showCircle=false;
        // 播放结束后，删除该bubbleLayout
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MyPoint.this.removeView(im_pop);
            }
        }, 501);
    }
}
