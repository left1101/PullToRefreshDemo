package com.pvkeep.wjdh.pulltorefreshdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * 首页的刷新控制view Xiexr
 */
public class RefreshableView extends LinearLayout {

    private static final String TAG = "RefreshableView";
    private Scroller scroller;
    private View refreshView;
    private ImageView refreshIndicatorView;
    private int refreshTargetTop;
    private ProgressBar bar;
    private TextView downTextView;
    private TextView timeTextView;

    private RefreshListener refreshListener;

    private String downTextString;
    private String releaseTextString;
    private String doingTextString;

    private Long refreshTime = null;
    private int lastX;
    private int lastY;
    // 拉动标记
    private boolean isDragging = false;
    // 是否可刷新标记
    private boolean isRefreshEnabled = true;
    // 在刷新中标记
    private boolean isRefreshing = false;

    private Context mContext;

    public RefreshableView(Context context) {
        super(context);
        mContext = context;
    }

    public RefreshableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        refreshTargetTop = - Utils.dip2px(context, 60);
        init();
    }

    private void init() {
        //滑动对象
        scroller = new Scroller(mContext);
        //刷新视图顶端的的view
        refreshView = LayoutInflater.from(mContext).inflate(R.layout.xlistview_header, null);
        //指示器view
        refreshIndicatorView = (ImageView) refreshView.findViewById(R.id.xlistview_header_arrow);
        //刷新bar
        bar = (ProgressBar) refreshView.findViewById(R.id.xlistview_header_progressbar);
        //下拉显示text
        downTextView = (TextView) refreshView.findViewById(R.id.xlistview_header_hint_textview);
        //下拉显示时间
        timeTextView = (TextView) refreshView.findViewById(R.id.xlistview_header_time);

        LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, - refreshTargetTop);
        lp.topMargin = refreshTargetTop;
        lp.gravity = Gravity.CENTER;
        addView(refreshView, lp);
        downTextString = "下拉刷新";
        releaseTextString = "释放开始刷新";
        doingTextString = "正在刷新";
    }

    /**
     * 刷新
     * @param time
     */
    private void setRefreshText(String time) {
        //timeTextView.setText(time);
    }

    /**
     * 触摸事件的处理
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y= (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录下y坐标
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "ACTION_MOVE");
                //y移动坐标
                int m = y - lastY;
                if(((m < 6) && (m > -1)) || (!isDragging )){
                    doMovement(m);
                }
                //记录下此刻y坐标
                this.lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "ACTION_UP");
                fling();
                break;
        }
        return true;
    }

    /**
     * 事件拦截，该方法一般和ontouchEvent 一起用
     * @see android.view.ViewGroup#onInterceptTouchEvent(MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int action = e.getAction();
        int y= (int) e.getRawY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                //y移动坐标
                int m = y - lastY;
                //记录下此刻y坐标
                this.lastY = y;
                if(m > 6 &&  canScroll()){
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return false;
    }

    /**
     *
     */
    @Override
    public void computeScroll() {
        if(scroller.computeScrollOffset()){
            int i = this.scroller.getCurrY();
            LayoutParams lp = (LayoutParams)this.refreshView.getLayoutParams();
            int k = Math.max(i, refreshTargetTop);
            lp.topMargin = k;
            this.refreshView.setLayoutParams(lp);
            this.refreshView.invalidate();
            invalidate();
        }
    }

    /**
     * up事件处理
     */
    private void fling() {
        LayoutParams lp = (LayoutParams) refreshView.getLayoutParams();
        Log.i(TAG, "fling()" + lp.topMargin);
        if(lp.topMargin > 0){//拉到了触发可刷新事件
            if (!isRefreshing){
                refresh();
            }
        }else{
            returnInitState();
        }
    }

    private void returnInitState() {
        LayoutParams lp = (LayoutParams)this.refreshView.getLayoutParams();
        int i = lp.topMargin;
        scroller.startScroll(0, i, 0, refreshTargetTop);
        invalidate();
    }

    private void refresh() {
        LayoutParams lp = (LayoutParams)this.refreshView.getLayoutParams();
        int i = lp.topMargin;
        refreshIndicatorView.setVisibility(View.GONE);
        bar.setVisibility(View.VISIBLE);
        downTextView.setText(doingTextString);
        scroller.startScroll(0, i, 0, 0 - i);
        invalidate();
        if(refreshListener !=null){
            refreshListener.onRefresh(this);
            isRefreshing = true;
        }
    }

    /**
     * 下拉move事件处理
     * @param moveY
     */
    private void doMovement(int moveY) {
        LayoutParams lp = (LayoutParams) refreshView.getLayoutParams();
        if(moveY > 0){
            //获取view的上边距
            float f1 =lp.topMargin;
            float f2 = moveY * 0.3F;
            int i = (int)(f1 + f2);
            //修改上边距
            lp.topMargin = i;
            //修改后刷新
            refreshView.setLayoutParams(lp);
            refreshView.invalidate();
            invalidate();
        }
        timeTextView.setVisibility(View.VISIBLE);
        if(refreshTime!= null){
            setRefreshTime(refreshTime);
        }
        downTextView.setVisibility(View.VISIBLE);

        refreshIndicatorView.setVisibility(View.VISIBLE);
        bar.setVisibility(View.GONE);
        if(lp.topMargin >  0){
            downTextView.setText(releaseTextString);
            refreshIndicatorView.setImageResource(R.mipmap.xlistview_arrow);
        }else{
            downTextView.setText(downTextString);
            refreshIndicatorView.setImageResource(R.mipmap.xlistview_arrow);
        }
    }

    public void setRefreshEnabled(boolean b) {
        this.isRefreshEnabled = b;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    /**
     * 刷新时间
     * @param
     */
    private void setRefreshTime(Long time) {
        timeTextView.setText(String.valueOf(time));
    }

    /**
     * 结束刷新事件
     */
    public void finishRefresh(){
        Log.i(TAG, "执行了=====finishRefresh");
        LayoutParams lp= (LayoutParams)this.refreshView.getLayoutParams();
        int i = lp.topMargin;
        refreshIndicatorView.setVisibility(View.VISIBLE);
        //timeTextView.setVisibility(View.VISIBLE);
        timeTextView.setText(Utils.getDateAndTime());
        scroller.startScroll(0, i, 0, refreshTargetTop);
        invalidate();
        isRefreshing = false;
    }

    private boolean canScroll() {
        View childView;
        if(getChildCount()>1){
            childView = this.getChildAt(2);
            if(childView instanceof ListView){
                int top =((ListView)childView).getChildAt(0).getTop();
                int pad =((ListView)childView).getListPaddingTop();
                if((Math.abs(top - pad)) < 3 && ((ListView) childView).getFirstVisiblePosition() == 0){
                    return true;
                }else{
                    return false;
                }
            }else if(childView instanceof ScrollView){
                if(((ScrollView)childView).getScrollY() == 0){
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }

    public boolean isRefresh(){
        if (isRefreshing){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 刷新监听接口
     * @author Nono
     */
    public interface RefreshListener{
        void onRefresh(RefreshableView view);
    }

}