package com.hz.scantool.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import com.hz.scantool.R;

public class LoadListView extends ListView implements AbsListView.OnScrollListener,View.OnClickListener {

    private LayoutInflater mInflater;

    //检查是否最后一行
    private boolean isLastRow;

    private View mFooter;
    private Button btnLoadMore;

    //实现加载更多数据
    public OnLoadMoreListener moreListener;

    public void setLoadMoreListener(OnLoadMoreListener moreListener){
        this.moreListener = moreListener;
    }

    public LoadListView(Context context){
        super(context);
        initView();
    }

    public LoadListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LoadListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setLoadMoreTitle(String msg){
        btnLoadMore.setText(getResources().getString(R.string.footer_loadmore)+"("+msg+")");
    }

    private void initView(){
        mInflater = LayoutInflater.from(getContext());
        mFooter = mInflater.inflate(R.layout.listfooter,null);
        this.addFooterView(mFooter);
        mFooter.setVisibility(View.GONE);

        btnLoadMore = mFooter.findViewById(R.id.btnLoadMore);
        btnLoadMore.setOnClickListener(this);
        setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        //正在滚动时回调，回调2-3次，手指没抛则回调2次。scrollState = 2的这次不回调
        //回调顺序如下
        //第1次：scrollState = SCROLL_STATE_TOUCH_SCROLL(1) 正在滚动
        //第2次：scrollState = SCROLL_STATE_FLING(2) 手指做了抛的动作（手指离开屏幕前，用力滑了一下）
        //第3次：scrollState = SCROLL_STATE_IDLE(0) 停止滚动
        //当屏幕停止滚动时为0；当屏幕滚动且用户使用的触碰或手指还在屏幕上时为1；
        //由于用户的操作，屏幕产生惯性滑动时为2

        //当滚到最后一行且停止滚动时，执行加载
        if(isLastRow&&scrollState== OnScrollListener.SCROLL_STATE_IDLE){
            mFooter.setVisibility(View.VISIBLE);
            isLastRow = false;
//            if(moreListener!=null){
//                moreListener.LoadMore();
//            }
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //滚动时一直回调，直到停止滚动时才停止回调。单击时回调一次。
        //firstVisibleItem：当前能看见的第一个列表项ID（从0开始）
        //visibleItemCount：当前能看见的列表项个数（小半个也算）
        //totalItemCount：列表项共数
        //判断是否滚到最后一行
        if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
            isLastRow = true;
        }
    }

    @Override
    public void onClick(View view) {
        mFooter.setVisibility(View.VISIBLE);
        isLastRow = false;
        if(moreListener!=null){
            moreListener.LoadMore();
        }
    }

    public interface OnLoadMoreListener{
        void LoadMore();
    }
}
