package com.chateasy.android.animator;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Administrator on 2022/1/31.
 */

public class SlideInOutBottomItemAnimator extends BaseItemAnimator {
    private float mOriginalY;
    private float mDeltaY;

    public SlideInOutBottomItemAnimator(RecyclerView recyclerView){
        super(recyclerView);
    }

    protected void animateRemovelmpl(final RecyclerView.ViewHolder viewHolder){
        final View view = viewHolder.itemView;

        final ViewPropertyAnimatorCompat animation = ViewCompat.animate(view);
        mRemoveAnimations.add(viewHolder);
        animation.setDuration(getRemoveDuration()).alpha(0).translationY(+mDeltaY).setListener(new VpaListenerAdapter(){
            @Override
            public void onAnimationStart(View view){
                dispatchMoveStarting(viewHolder);
            }

            @Override
            public void onAnimationEnd(View view){
                animation.setListener(null);
                ViewCompat.setAlpha(view, 1);
                ViewCompat.setTranslationY(view, +mDeltaY);
                dispatchRemoveFinished(viewHolder);
                mRemoveAnimations.remove(viewHolder);
                dispatchFinishedWhenDone();
            }
        }).start();
    }

    @Override
    protected void prepareAnimateAdd(RecyclerView.ViewHolder holder){
        retrieveItemPosition(holder);
        ViewCompat.setTranslationY(holder.itemView, +mDeltaY);
    }

    protected void animateAddlmpl(final RecyclerView.ViewHolder holder){
        final View view = holder.itemView;
        final ViewPropertyAnimatorCompat animation = ViewCompat.animate(view);
        mAddAnimations.add(holder);
        animation.translationY(0).alpha(1).setDuration(getAddDuration()).setListener(new VpaListenerAdapter(){
            @Override
            public void onAnimationStart(View view){
                dispatchAddStarting(holder);
            }

            @Override
            public void onAnimationCancel(View view){
                ViewCompat.setAlpha(view, 1);
                ViewCompat.setTranslationY(view, 0);
            }

            @Override
            public void onAnimationEnd(View view){
                animation.setListener(null);
                ViewCompat.setAlpha(view, 1);
                ViewCompat.setTranslationY(view, 0);
                dispatchAddFinished(holder);
                mAddAnimations.remove(holder);
                dispatchFinishedWhenDone();
            }
        }).start();
    }

    private void retrieveItemPosition(final RecyclerView.ViewHolder holder){
        mOriginalY = mRecyclerView.getLayoutManager().getDecoratedTop(holder.itemView);
        mDeltaY = mRecyclerView.getHeight() - mOriginalY;
    }
}
