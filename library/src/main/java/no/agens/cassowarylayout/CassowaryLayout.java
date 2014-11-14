/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2014 Agens AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.agens.cassowarylayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;


import org.pybee.cassowary.Variable;

import no.agens.cassowarylayout.util.TimerUtil;

public class CassowaryLayout extends ViewGroup  {

    private String logTag;

    private CassowaryModel cassowaryModel;

    private ViewIdResolver viewIdResolver;

    public CassowaryLayout(Context context, ViewIdResolver viewIdResolver) {
        super(context);

        this.viewIdResolver = viewIdResolver;
        this.cassowaryModel = new CassowaryModel(context);
    }

    public CassowaryLayout(Context context, ViewIdResolver viewIdResolver, CassowaryModel cassowaryModel) {
        this(context, viewIdResolver);
        this.cassowaryModel = cassowaryModel;
    }

    public CassowaryLayout(Context context) {
        super(context);
        this.viewIdResolver = new DefaultViewIdResolver(getContext());
        this.cassowaryModel = new CassowaryModel(context);
    }

    public CassowaryLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.viewIdResolver = new DefaultViewIdResolver(getContext());
        cassowaryModel = new CassowaryModel(context);
        readConstraintsFromXml(attrs);
    }

    public CassowaryLayout(Context context, AttributeSet attrs,
                          int defStyle) {
        super(context, attrs, defStyle);
        this.viewIdResolver = new DefaultViewIdResolver(getContext());
        this.cassowaryModel = new CassowaryModel(context);
        readConstraintsFromXml(attrs);
    }


    public CassowaryModel getCassowaryModel() {
        return cassowaryModel;
    }


    private void updateIntrinsicWidthConstraints() {
        long timeBeforeSolve = System.currentTimeMillis();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() != GONE) {
                Node node = getNodeById(child.getId());
                if (node.hasIntrinsicWidth()) {
                    int childWidth = child.getMeasuredWidth();
                    log("child " + viewIdResolver.getViewNameById(child.getId()) + " intrinsic width " + childWidth);
                    if ((int)node.getIntrinsicWidth().value() != childWidth) {
                        node.setIntrinsicWidth(childWidth);
                    }

                }
            }
        }
        log("updateIntrinsicWidthConstraints took " +  TimerUtil.since(timeBeforeSolve));
    }

    private void updateIntrinsicHeightConstraints() {

       long timeBeforeSolve = System.currentTimeMillis();

       int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() != GONE) {
                Node node = getNodeById(child.getId());

                if (node.hasIntrinsicHeight()) {
                    int childHeight = child.getMeasuredHeight();
                    Variable intrinsicHeight = node.getIntrinsicHeight();
                    log("child " + viewIdResolver.getViewNameById(child.getId()) + " intrinsic height (measured)" + childHeight);
                    if ((int)intrinsicHeight.value() != childHeight) {
                        long timeBeforeGetMeasuredHeight = System.currentTimeMillis();

                        node.setIntrinsicHeight(childHeight);
                        log("node.setIntrinsicHeight took " +  TimerUtil.since(timeBeforeGetMeasuredHeight));
                    }
                }

            }

        }
        log("updateIntrinsicHeightConstraints took " +  TimerUtil.since(timeBeforeSolve));
    }

    protected void measureHeightBasedOnWidth(int widthMeasureSpec, int heightMeasureSpec) {
        long timeBeforeSolve = System.currentTimeMillis();

        final int size = getChildCount();

        for (int i = 0; i < size; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {


                Node node = getNodeById(child.getId());

                if (node.hasIntrinsicHeight()) {

                    int width = (int) node.getWidth().value();
                    log("measureHeightBasedOnWidth child " + viewIdResolver.getViewNameById(child.getId()) + " width " + width);

                    int childHeightSpec = MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.getMode(heightMeasureSpec));

                    int childWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);

                    log("child " + viewIdResolver.getViewNameById(child.getId()) + " parent measured width " +  MeasureSpec.getSize(childWidthSpec) + " mode " + MeasureSpec.getMode(childWidthSpec) );

                    measureChild(child, childWidthSpec, childHeightSpec);

                }
             }
        }
        log("measureHeightBasedOnWidth took " +  TimerUtil.since(timeBeforeSolve));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        log("onMesaure");

        long timeBeforeSolve = System.currentTimeMillis();


        int resolvedWidth = -1;
        int resolvedHeight = -1;

        int heightSpec = MeasureSpec.getMode(heightMeasureSpec);
        if (heightSpec == MeasureSpec.EXACTLY) {
            resolvedHeight = resolveSizeAndState(0, heightMeasureSpec, 0);
            cassowaryModel.getContainerNode().setVariableToValue(Node.HEIGHT, resolvedHeight - getPaddingTop() - getPaddingBottom());
        } else if (heightSpec == MeasureSpec.AT_MOST) {
            int maxHeight =  MeasureSpec.getSize(heightMeasureSpec);
            cassowaryModel.getContainerNode().setVariableToAtMost(Node.HEIGHT, maxHeight - getPaddingTop() - getPaddingBottom());
        }

        int widthSpec = MeasureSpec.getMode(widthMeasureSpec);
        if (widthSpec == MeasureSpec.EXACTLY) {
            resolvedWidth = resolveSizeAndState(0, widthMeasureSpec, 0);
            cassowaryModel.getContainerNode().setVariableToValue(Node.WIDTH, resolvedWidth - getPaddingLeft() - getPaddingRight());
        } else if (widthSpec == MeasureSpec.AT_MOST) {
            int maxWidth =  MeasureSpec.getSize(widthMeasureSpec);
            cassowaryModel.getContainerNode().setVariableToAtMost(Node.WIDTH, maxWidth - getPaddingLeft() - getPaddingRight());
        }


        measureChildren(widthMeasureSpec, heightMeasureSpec);

        updateIntrinsicWidthConstraints();

        cassowaryModel.solve();

        measureHeightBasedOnWidth(widthMeasureSpec, heightMeasureSpec);

        updateIntrinsicHeightConstraints();

        cassowaryModel.solve();

        if (widthSpec == MeasureSpec.AT_MOST || widthSpec == MeasureSpec.UNSPECIFIED) {
            resolvedWidth = (int) cassowaryModel.getContainerNode().getWidth().value() + getPaddingLeft() + getPaddingRight();
        }

        if (heightSpec == MeasureSpec.AT_MOST || heightSpec == MeasureSpec.UNSPECIFIED) {
            resolvedHeight = (int) cassowaryModel.getContainerNode().getHeight().value() + getPaddingTop() + getPaddingBottom();
        }

        setMeasuredDimension(resolvedWidth, resolvedHeight);

        log("onMeasure took " + TimerUtil.since(timeBeforeSolve));
    }

    /**
     * Returns a set of layout parameters with a width of
     * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT},
     * a height of {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}
     * and with the coordinates (0, 0).
     */
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t,
                            int r, int b) {

        long timeBeforeSolve = System.currentTimeMillis();

        cassowaryModel.solve();

        log(
                       " container height " + cassowaryModel.getContainerNode().getHeight().value() +
                       " container width " + cassowaryModel.getContainerNode().getWidth().value() +
                       " container center x " + cassowaryModel.getContainerNode().getCenterX().value() +
                       " container center y " + cassowaryModel.getContainerNode().getCenterY().value()
                );
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                CassowaryLayout.LayoutParams lp =
                        (CassowaryLayout.LayoutParams) child.getLayoutParams();

                int childId = child.getId();
                Node node = getNodeById(childId);

                int x = (int) node.getLeft().value() + getPaddingLeft();
                int y = (int) node.getTop().value() + getPaddingTop();

                int width = (int) node.getWidth().value();
                int height = (int) node.getHeight().value();
                log("child " + viewIdResolver.getViewNameById(child.getId())  + " x " + x + " y " + y + " width " + width + " height " + height);

                if (node.hasIntrinsicHeight()) {
                    log("child " + viewIdResolver.getViewNameById(child.getId())  + " intrinsic height " + node.getIntrinsicHeight().value());
                }

                if (node.hasVariable(Node.CENTERX)) {
                    log("child " + viewIdResolver.getViewNameById(child.getId())  + " centerX " + node.getVariable(Node.CENTERX).value());
                }

                if (node.hasVariable(Node.CENTERY)) {
                    log("child " + viewIdResolver.getViewNameById(child.getId())  + " centerY " + node.getVariable(Node.CENTERY).value());
                }

                child.layout(x, y,
                        x + /*child.getMeasuredWidth()*/ width ,
                        y + /*child.getMeasuredHeight() */+ height);

            }
        }
        log("onLayout - took " + TimerUtil.since(timeBeforeSolve));
    }

    public void setChildPositionsFromCassowaryModel() {
        long timeBeforeSolve = System.currentTimeMillis();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                int childId = child.getId();

                Node node = getNodeById(childId);

                int x = (int) node.getLeft().value() + getPaddingLeft();
                int y = (int) node.getTop().value() + getPaddingTop();

                child.setX(x);
                child.setY(y);

            }
        }
        log("setChildPositionsFromCassowaryModel - took " + TimerUtil.since(timeBeforeSolve));
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CassowaryLayout.LayoutParams(getContext(), attrs);
    }

    // Override to allow type-checking of CassowaryLayout.
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof CassowaryLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /**
     * Per-child layout information associated with AbsoluteLayout.
     * See
     * {@link android.R.styleable#AbsoluteLayout_Layout Absolute Layout Attributes}
     * for a list of all child view attributes that this class supports.
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {

        /**
         * The horizontal, or X, location of the child within the view group.
         */
        //public int x;
        /**
         * The vertical, or Y, location of the child within the view group.
         */
        //public int y;

        /**
         * Creates a new set of layout parameters with the specified width,
         * height and location.
         *
         * @param width the width, either {@link #MATCH_PARENT},
        {@link #WRAP_CONTENT} or a fixed size in pixels
         * @param height the height, either {@link #MATCH_PARENT},
        {@link #WRAP_CONTENT} or a fixed size in pixels
         * @param x the X location of the child
         * @param y the Y location of the child
         */
        public LayoutParams(int width, int height) {
            super(width, height);
            //this.x = x;
            //this.y = y;
        }

        /**
         * Creates a new set of layout parameters. The values are extracted from
         * the supplied attributes set and context. The XML attributes mapped
         * to this set of layout parameters are:
         *
         * <ul>
         *   <li><code>layout_x</code>: the X location of the child</li>
         *   <li><code>layout_y</code>: the Y location of the child</li>
         *   <li>All the XML attributes from
         *   {@link android.view.ViewGroup.LayoutParams}</li>
         * </ul>
         *
         * @param c the application environment
         * @param attrs the set of attributes from which to extract the layout
         *              parameters values
         */
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs,
                    R.styleable.AbsoluteLayout_Layout);
           /* x = a.getDimensionPixelOffset(
                    R.styleable.AbsoluteLayout_Layout_layout_x, 0);
            y = a.getDimensionPixelOffset(
                    R.styleable.AbsoluteLayout_Layout_layout_y, 0);*/
            a.recycle();
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public String debug(String output) {
           /* return output + "Absolute.LayoutParams={width="
                    + width + ", height=" + height
                    + " x=" + x + " y=" + y + "}";*/
            return "";
        }
    }


    private void readConstraintsFromXml(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CassowaryLayout,
                0, 0);

        try {
            CharSequence[] constraints = a.getTextArray(R.styleable.CassowaryLayout_constraints);

            if (constraints == null) {
                throw new RuntimeException("missing cassowary:constraints attribute in XML");
            }

            long timebefore = System.currentTimeMillis();
            cassowaryModel.addConstraints(constraints);
            log("addConstraints took " + TimerUtil.since(timebefore));
        } finally {
            a.recycle();
        }

    }

    public Node getNodeById(int id) {
        Node node = cassowaryModel.getNodeByName(viewIdResolver.getViewNameById(id));
        return node;
    }


    private void log(String message) {
        if (logTag == null) {
            try {
                logTag = "CassowaryLayout " + viewIdResolver.getViewNameById(getId());
            } catch (RuntimeException e) {
                logTag = "CassowaryLayout noid";
            }
        }
        Log.d(logTag, message);
    }
}

