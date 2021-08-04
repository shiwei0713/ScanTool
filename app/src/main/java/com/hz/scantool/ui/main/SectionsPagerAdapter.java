package com.hz.scantool.ui.main;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.hz.scantool.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private int[] tabTitles = null;
    private final Context mContext;
    private int count;
    private int titleType;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    public void setItem(int tabCount,int tabTitleType){
        //初始化Tab页签数和类型
        this.count = tabCount;
        this.titleType = tabTitleType;

        //按照不同的导航按钮切换至不同的Tab页签
        switch (titleType){
            case 0:
                tabTitles = new int[]{R.string.tab_pro1};
                break;
            case 1:
                tabTitles = new int[]{R.string.tab_qc1, R.string.tab_qc2,R.string.tab_qc3,R.string.tab_qc4};
                break;
            case 2:
                tabTitles = new int[]{R.string.tab_stock1};
                break;
            case 3:
                tabTitles = new int[]{R.string.tab_pur1, R.string.tab_pur2,R.string.tab_pur3};
                break;
            case 4:
                tabTitles = new int[]{R.string.tab_stock2, R.string.tab_stock3,R.string.tab_stock4,R.string.tab_stock5};
                break;
            case 5:
                tabTitles = new int[]{R.string.tab_stock6, R.string.tab_stock7,R.string.tab_stock8};
                break;
            case 6:
                tabTitles = new int[]{R.string.tab_product1,R.string.tab_product2};
                break;
            case 7:
                tabTitles = new int[]{R.string.tab_query1};
                break;
            default:
                tabTitles = new int[]{R.string.tab_qc1, R.string.tab_qc2,R.string.tab_qc3,R.string.tab_qc4};
                break;
        }
    }

    @Override
    public Fragment getItem(int position) {
        //创建Fragment事例,并传入参数
        /*参数:
        1.position:传入页签索引
        2.titleType:传入页签类型
        3.whereCondition:传入查询条件
         */
        return PlaceholderFragment.newInstance(position + 1,titleType);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        //返回Tab页签名称
        return mContext.getResources().getString(tabTitles[position]);
    }

    @Override
    public int getCount() {
        // 显示Tab数
        return count;
    }
}