// Copyright 2016 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.compositor.bottombar.contextualsearch;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.chromium.chrome.R;
import org.chromium.chrome.browser.compositor.bottombar.OverlayPanel;
import org.chromium.chrome.browser.compositor.bottombar.OverlayPanelAnimation;
import org.chromium.chrome.browser.compositor.bottombar.OverlayPanelTextViewInflater;
import org.chromium.chrome.browser.compositor.layouts.ChromeAnimation;
import org.chromium.ui.resources.dynamics.DynamicResourceLoader;

/**
 * Controls the Caption View that is shown at the bottom of the control and used
 * as a dynamic resource.
 */
public class ContextualSearchCaptionControl extends OverlayPanelTextViewInflater
        implements ChromeAnimation.Animatable<ContextualSearchCaptionControl.AnimationType> {
    private static final float ANIMATION_PERCENTAGE_ZERO = 0.f;
    private static final float ANIMATION_PERCENTAGE_COMPLETE = 1.f;

    /**
     * Animation properties.
     */
    protected enum AnimationType {
        APPEARANCE
    }

    /**
     * The caption View.
     */
    private TextView mCaption;

    /**
     * The caption visibility.
     */
    private boolean mIsVisible;

    /**
     * The caption animation percentage, which controls how and where to draw. It is
     * ANIMATION_PERCENTAGE_COMPLETE when the Contextual Search bar is peeking and
     * ANIMATION_PERCENTAGE_ZERO when it is expanded.
     */
    private float mAnimationPercentage = ANIMATION_PERCENTAGE_ZERO;

    /**
     * Whether a new snapshot has been captured by the system yet - this is false when we have
     * something to show, but cannot yet show it.
     */
    private boolean mDidCapture;

    /**
     * @param panel             The panel.
     * @param context           The Android Context used to inflate the View.
     * @param container         The container View used to inflate the View.
     * @param resourceLoader    The resource loader that will handle the snapshot capturing.
     */
    public ContextualSearchCaptionControl(OverlayPanel panel, Context context, ViewGroup container,
            DynamicResourceLoader resourceLoader) {
        super(panel, R.layout.contextual_search_caption_view, R.id.contextual_search_caption_view,
                context, container, resourceLoader);
    }

    /**
     * Sets the caption to display in the bottom of the control.
     * @param caption The string displayed as a caption to help explain results,
     *        e.g. a Quick Answer.
     */
    public void setCaption(String caption) {
        // If the caption is visible it has already been set. Return early rather than changing it.
        if (mIsVisible) return;

        mDidCapture = false;

        inflate();

        mCaption.setText(sanitizeText(caption));

        invalidate();
        show();
    }

    /**
     * Hides the caption.
     */
    public void hide() {
        mIsVisible = false;
        mAnimationPercentage = ANIMATION_PERCENTAGE_ZERO;
    }

    /**
     * Shows the caption.
     */
    private void show() {
        mIsVisible = true;
    }

    /**
     * Controls whether the caption is visible and can be rendered.
     * The caption must be visible in order to draw it and take a snapshot.
     * Even though the caption is visible the user might not be able to see it due to a
     * completely transparent opacity associated with an animation percentage of zero.
     * @return Whether the caption is visible or not.
     */
    public boolean getIsVisible() {
        return mIsVisible;
    }

    /**
     * Gets the animation percentage which controls the drawing of the caption and how high to
     * position it in the Bar.
     * @return The current percentage ranging from 0.0 to 1.0.
     */
    public float getAnimationPercentage() {
        // If we don't yet have a snapshot captured, stay at zero.  See crbug.com/608914.
        if (!mDidCapture) return ANIMATION_PERCENTAGE_ZERO;

        return mAnimationPercentage;
    }

    /**
     * @return The text currently showing in the caption view.
     */
    public CharSequence getCaptionText() {
        return mCaption.getText();
    }

    //========================================================================================
    // OverlayPanelTextViewInflater overrides
    //========================================================================================

    @Override
    protected TextView getTextView() {
        return mCaption;
    }

    //========================================================================================
    // OverlayPanelInflater overrides
    //========================================================================================

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View view = getView();
        mCaption = (TextView) view.findViewById(R.id.contextual_search_caption);
    }

    @Override
    protected void onCaptureEnd() {
        super.onCaptureEnd();
        mDidCapture = true;

        animateTransitionIn();
    }

    // ============================================================================================
    // Search Caption Animation
    // ============================================================================================

    private void animateTransitionIn() {
        mOverlayPanel.addToAnimation(this, AnimationType.APPEARANCE, ANIMATION_PERCENTAGE_ZERO,
                ANIMATION_PERCENTAGE_COMPLETE, OverlayPanelAnimation.MAXIMUM_ANIMATION_DURATION_MS,
                0);
    }

    @Override
    public void setProperty(AnimationType type, float value) {
        if (type == AnimationType.APPEARANCE) {
            mAnimationPercentage = value;
        }
    }

    @Override
    public void onPropertyAnimationFinished(AnimationType prop) {}
}
