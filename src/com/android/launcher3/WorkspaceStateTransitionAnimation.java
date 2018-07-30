/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;

import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.util.Thunk;

import java.util.HashMap;

/**
 * A convenience class to update a view's visibility state after an alpha animation.
 */
class AlphaUpdateListener extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
    private static final float ALPHA_CUTOFF_THRESHOLD = 0.01f;

    private View mView;
    private boolean mAccessibilityEnabled;
    private boolean mCanceled = false;

    public AlphaUpdateListener(View v, boolean accessibilityEnabled) {
        mView = v;
        mAccessibilityEnabled = accessibilityEnabled;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator arg0) {
        updateVisibility(mView, mAccessibilityEnabled);
    }

    public static void updateVisibility(View view, boolean accessibilityEnabled) {
        // We want to avoid the extra layout pass by setting the views to GONE unless
        // accessibility is on, in which case not setting them to GONE causes a glitch.
        int invisibleState = accessibilityEnabled ? View.GONE : View.INVISIBLE;
        if (view.getAlpha() < ALPHA_CUTOFF_THRESHOLD && view.getVisibility() != invisibleState) {
            view.setVisibility(invisibleState);
        } else if (view.getAlpha() > ALPHA_CUTOFF_THRESHOLD
                && view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        mCanceled = true;
    }

    @Override
    public void onAnimationEnd(Animator arg0) {
        if (mCanceled) return;
        updateVisibility(mView, mAccessibilityEnabled);
    }

    @Override
    public void onAnimationStart(Animator arg0) {
        // We want the views to be visible for animation, so fade-in/out is visible
        mView.setVisibility(View.VISIBLE);
    }
}

/**
 * This interpolator emulates the rate at which the perceived scale of an object changes
 * as its distance from a camera increases. When this interpolator is applied to a scale
 * animation on a view, it evokes the sense that the object is shrinking due to moving away
 * from the camera.
 */
class ZInterpolator implements TimeInterpolator {
    private float focalLength;

    public ZInterpolator(float foc) {
        focalLength = foc;
    }

    public float getInterpolation(float input) {
        return (1.0f - focalLength / (focalLength + input)) /
                (1.0f - focalLength / (focalLength + 1.0f));
    }
}

/**
 * The exact reverse of ZInterpolator.
 */
class InverseZInterpolator implements TimeInterpolator {
    private ZInterpolator zInterpolator;
    public InverseZInterpolator(float foc) {
        zInterpolator = new ZInterpolator(foc);
    }
    public float getInterpolation(float input) {
        return 1 - zInterpolator.getInterpolation(1 - input);
    }
}

/**
 * InverseZInterpolator compounded with an ease-out.
 */
class ZoomInInterpolator implements TimeInterpolator {
    private final InverseZInterpolator inverseZInterpolator = new InverseZInterpolator(0.35f);
    private final DecelerateInterpolator decelerate = new DecelerateInterpolator(3.0f);

    public float getInterpolation(float input) {
        return decelerate.getInterpolation(inverseZInterpolator.getInterpolation(input));
    }
}

/**
 * Stores the transition states for convenience.
 */
class TransitionStates {

    // Raw states
    final boolean oldStateIsNormal;
    final boolean oldStateIsSpringLoaded;
    final boolean oldStateIsNormalHidden;
    final boolean oldStateIsOverviewHidden;
    final boolean oldStateIsOverview;
    final boolean oldStateIsFolderImport;

    final boolean stateIsNormal;
    final boolean stateIsSpringLoaded;
    final boolean stateIsNormalHidden;
    final boolean stateIsOverviewHidden;
    final boolean stateIsOverview;
    final boolean stateIsFolderImport;

    // Convenience members
    final boolean workspaceToAllApps;
    final boolean overviewToAllApps;
    final boolean allAppsToWorkspace;
    final boolean workspaceToOverview;
    final boolean overviewToWorkspace;
    final boolean workspaceToFolderImport;

    public TransitionStates(final Workspace.State fromState, final Workspace.State toState) {
        oldStateIsNormal = (fromState == Workspace.State.NORMAL);
        oldStateIsSpringLoaded = (fromState == Workspace.State.SPRING_LOADED);
        oldStateIsNormalHidden = (fromState == Workspace.State.NORMAL_HIDDEN);
        oldStateIsOverviewHidden = (fromState == Workspace.State.OVERVIEW_HIDDEN);
        oldStateIsOverview = (fromState == Workspace.State.OVERVIEW);
        oldStateIsFolderImport = (fromState==Workspace.State.FOLDER_IMPORT);

        stateIsNormal = (toState == Workspace.State.NORMAL);
        stateIsSpringLoaded = (toState == Workspace.State.SPRING_LOADED);
        stateIsNormalHidden = (toState == Workspace.State.NORMAL_HIDDEN);
        stateIsOverviewHidden = (toState == Workspace.State.OVERVIEW_HIDDEN);
        stateIsOverview = (toState == Workspace.State.OVERVIEW);
        stateIsFolderImport = (toState == Workspace.State.FOLDER_IMPORT);

        workspaceToOverview = (oldStateIsNormal && stateIsOverview);
        workspaceToAllApps = (oldStateIsNormal && stateIsNormalHidden);
        overviewToWorkspace = (oldStateIsOverview && stateIsNormal);
        overviewToAllApps = (oldStateIsOverview && stateIsOverviewHidden);
        allAppsToWorkspace = (oldStateIsNormalHidden && stateIsNormal);
        workspaceToFolderImport = (oldStateIsFolderImport&&stateIsFolderImport);
    }
}

/**
 * Manages the animations between each of the workspace states.
 */
public class WorkspaceStateTransitionAnimation {

    public static final String TAG = "WorkspaceStateTransitionAnimation";
    public static final int SCROLL_TO_CURRENT_PAGE = -1;

    @Thunk static final int BACKGROUND_FADE_OUT_DURATION = 350;

    final @Thunk Launcher mLauncher;
    final @Thunk Workspace mWorkspace;

    @Thunk AnimatorSet mStateAnimator;

    @Thunk float mCurrentScale;
    @Thunk float mNewScale;

    @Thunk final ZoomInInterpolator mZoomInInterpolator = new ZoomInInterpolator();

    @Thunk float mSpringLoadedShrinkFactor;
    @Thunk float mOverviewModeShrinkFactor;
    @Thunk float mWorkspaceScrimAlpha;
    @Thunk int mAllAppsTransitionTime;
    @Thunk int mOverviewTransitionTime;
    @Thunk int mOverlayTransitionTime;
    @Thunk int mSpringLoadedTransitionTime;
    @Thunk boolean mWorkspaceFadeInAdjacentScreens;

    public WorkspaceStateTransitionAnimation(Launcher launcher, Workspace workspace) {
        mLauncher = launcher;
        mWorkspace = workspace;

        DeviceProfile grid = mLauncher.getDeviceProfile();
        Resources res = launcher.getResources();
        mAllAppsTransitionTime = res.getInteger(R.integer.config_allAppsTransitionTime);
        mOverviewTransitionTime = res.getInteger(R.integer.config_overviewTransitionTime);
        mOverlayTransitionTime = res.getInteger(R.integer.config_overlayTransitionTime);
        mSpringLoadedTransitionTime = mOverlayTransitionTime / 2;
        mSpringLoadedShrinkFactor = mLauncher.getDeviceProfile().workspaceSpringLoadShrinkFactor;

//        if(mLauncher.isLandscape){
//            mOverviewModeShrinkFactor = 0.77f;
//        }else {
            mOverviewModeShrinkFactor =
                    res.getInteger(R.integer.config_workspaceOverviewShrinkPercentage) / 100f;
//        }
        mWorkspaceScrimAlpha = res.getInteger(R.integer.config_workspaceScrimAlpha) / 100f;
        mWorkspaceFadeInAdjacentScreens = grid.shouldFadeAdjacentWorkspaceScreens();
    }

    public void snapToPageFromOverView(int whichPage) {
        mWorkspace.snapToPage(whichPage, mOverviewTransitionTime, mZoomInInterpolator);
    }

    public AnimatorSet getAnimationToState(Workspace.State fromState, Workspace.State toState,
            boolean animated, HashMap<View, Integer> layerViews) {
        AccessibilityManager am = (AccessibilityManager)
                mLauncher.getSystemService(Context.ACCESSIBILITY_SERVICE);
        final boolean accessibilityEnabled = am.isEnabled();
        TransitionStates states = new TransitionStates(fromState, toState);
        int workspaceDuration = getAnimationDuration(states);
        animateWorkspace(states, animated, workspaceDuration, layerViews,
                accessibilityEnabled);
        animateBackgroundGradient(states, animated, BACKGROUND_FADE_OUT_DURATION);
        return mStateAnimator;
    }

    public float getFinalScale() {
        return mNewScale;
    }

    /**
     * Returns the proper animation duration for a transition.
     */
    private int getAnimationDuration(TransitionStates states) {
        if (states.workspaceToAllApps || states.overviewToAllApps) {
            return mAllAppsTransitionTime;
        } else if (states.workspaceToOverview || states.overviewToWorkspace) {
            return mOverviewTransitionTime;
        } else if (mLauncher.mState == Launcher.State.WORKSPACE_SPRING_LOADED
                || states.oldStateIsNormal && states.stateIsSpringLoaded) {
            return mSpringLoadedTransitionTime;
        } else {
            return mOverlayTransitionTime;
        }
    }

    /**
     * Starts a transition animation for the workspace.
     */
    private void animateWorkspace(final TransitionStates states, final boolean animated,
                                  final int duration, final HashMap<View, Integer> layerViews,
                                  final boolean accessibilityEnabled) {
        // Cancel existing workspace animations and create a new animator set if requested
        cancelAnimation();
        if (animated) {
            mStateAnimator = LauncherAnimUtils.createAnimatorSet();
        }

        final ViewGroup overviewPanel = mLauncher.getOverviewPanel();

        // Update the workspace state
        float finalBackgroundAlpha = (states.stateIsOverview) ?
                1.0f : 0f;// remove states.stateIsSpringLoaded ||
        float finalHotseatAlpha = (states.stateIsNormal || states.stateIsSpringLoaded ||
                (FeatureFlags.LAUNCHER3_ALL_APPS_PULL_UP && states.stateIsNormalHidden)) ? 1f : 0f;
        float finalOverviewPanelAlpha = states.stateIsOverview ? 1f : 0f;
        float finalOverviewPanelTranslationY = (states.stateIsOverviewHidden) ? (float) overviewPanel.getHeight()/2 : 0f;// add for overviewPanel anima
        float finalQsbAlpha = (states.stateIsNormal ||
                (FeatureFlags.LAUNCHER3_ALL_APPS_PULL_UP && states.stateIsNormalHidden)) ? 1f : 0f;

        float finalWorkspaceTranslationY = 0;
        if (states.stateIsOverview || states.stateIsOverviewHidden) {
            finalWorkspaceTranslationY = mWorkspace.getOverviewModeTranslationYNew();// modify getOverviewModeTranslationY to getOverviewModeTranslationYNew
        } else if (states.stateIsSpringLoaded) {
            if (mLauncher.isLandscape) {
                finalWorkspaceTranslationY = 0;
            } else {
                finalWorkspaceTranslationY = mWorkspace.getOverviewModeTranslationYNew();// modify getSpringLoadedTranslationY to getOverviewModeTranslationYNew
            }
        }

        // add for pageIndicatorCube
        float finalPageIndicatorCubeAlpha = (states.stateIsSpringLoaded) ? 1f : 0f;
        float finalPageIndicatorCubeScale = (states.stateIsSpringLoaded) ? 1f : 0.4f;
        float finalPageIndicatorAlpha = ((states.stateIsNormal && !states.oldStateIsFolderImport) || states.stateIsOverviewHidden || states.stateIsOverview
                || states.stateIsFolderImport||mLauncher.isExitImportModeInHomeKey()) ? 1f : 0f;
        float finalPageIndicatorTranslationY = (states.stateIsOverview) ?(finalWorkspaceTranslationY*1.20f) : 0f;
        if(states.oldStateIsOverviewHidden && states.stateIsOverview && overviewPanel.getTranslationY()<0.1){
            overviewPanel.setAlpha(0);
            int ty = overviewPanel.getHeight()/2;//getOverviewModeButtonBarHeight
            if(ty<=0){
                DeviceProfile grid = mLauncher.getDeviceProfile();
                ty = grid.getOverviewModeButtonBarHeight();
            }
            overviewPanel.setTranslationY(ty);
        }
        float finalAlineButtonAlpa = (states.stateIsOverview || states.stateIsOverviewHidden)?1:0;
//         add end

        final int childCount = mWorkspace.getChildCount();
        final int customPageCount = mWorkspace.numCustomPages();

        mNewScale = 1.0f;

        if (states.oldStateIsOverview) {
            mWorkspace.disableFreeScroll();
        } else if (states.stateIsOverview) {
            mWorkspace.enableFreeScroll();
        }

        if (!states.stateIsNormal) {
            if (states.stateIsSpringLoaded) {
                mNewScale = mSpringLoadedShrinkFactor;
            } else if (states.stateIsOverview || states.stateIsOverviewHidden) {
                mNewScale = mOverviewModeShrinkFactor;
            }
        }
        //M: begin
/*        if (toPage == SCROLL_TO_CURRENT_PAGE) {
            toPage = mWorkspace.getPageNearestToCenterOfScreen();
        }*/

       /* if(!states.oldStateIsFolderImport)
            mWorkspace.snapToPage(-1, duration, mZoomInInterpolator);*/
        //M: end
        int toPage = mWorkspace.getPageNearestToCenterOfScreen();
        // TODO: Animate the celllayout alpha instead of the pages.
        for (int i = 0; i < childCount; i++) {
            final CellLayout cl = (CellLayout) mWorkspace.getChildAt(i);
            float initialAlpha = cl.getShortcutsAndWidgets().getAlpha();
            float finalAlpha;
            // add start
            float initialTranslationY = cl.getShortcutsAndWidgets().getTranslationY();
            float finalTranslationY = 0;
            float initialScale = cl.getShortcutsAndWidgets().getScaleY();
            float finalScale = 1;
            float finalHomebuttonAlpa = 0;
            if (mLauncher.isLandscape && (states.stateIsOverview || states.stateIsOverviewHidden)) {
                finalTranslationY = cl.getContentTranslationY();
                finalScale = CellLayout.CELLLAYOUT_CONTENT_SCALE;
                finalHomebuttonAlpa = 1;
            }
            // add end

            if (states.stateIsOverviewHidden) {
                finalAlpha = 1f;// modify 0 to 1
            } else if(states.stateIsNormalHidden) {
                finalAlpha = (FeatureFlags.LAUNCHER3_ALL_APPS_PULL_UP &&
                        i == mWorkspace.getNextPage()) ? 1 : 0;
            } else if (states.stateIsNormal && mWorkspaceFadeInAdjacentScreens) {
                finalAlpha = (i == toPage || i < customPageCount) ? 1f : 0f;
            } else {
                finalAlpha = 1f;
            }

            // If we are animating to/from the small state, then hide the side pages and fade the
            // current page in
            if (!FeatureFlags.LAUNCHER3_ALL_APPS_PULL_UP && !mWorkspace.isSwitchingState()) {
                if (states.workspaceToAllApps || states.allAppsToWorkspace) {
                    boolean isCurrentPage = (i == toPage);
                    if (states.allAppsToWorkspace && isCurrentPage) {
                        initialAlpha = 0f;
                    } else if (!isCurrentPage) {
                        initialAlpha = finalAlpha = 0f;
                    }
                    cl.setShortcutAndWidgetAlpha(initialAlpha);
                }
            }

            if (animated) {
                float oldBackgroundAlpha = cl.getBackgroundAlpha();
                if (initialAlpha != finalAlpha || initialTranslationY != finalTranslationY || initialScale != finalScale) {
                    LauncherViewPropertyAnimator alphaAnim =
                            new LauncherViewPropertyAnimator(cl.getShortcutsAndWidgets());
     					//  modify for bug2696 start
     					 if(!states.oldStateIsSpringLoaded && !states.stateIsSpringLoaded){
                           alphaAnim.alpha(finalAlpha);
     					  }
						 //alphaAnim.alpha(finalAlpha)
     					//  modify for bug2696 end
                      alphaAnim.translationY(finalTranslationY)// add for celllayout topbar
                            .scaleY(finalScale)// add for celllayout topbar
                            .scaleX(finalScale)// add for celllayout topbar
                            .setDuration(duration)
                            .setInterpolator(mZoomInInterpolator);

                    mStateAnimator.play(alphaAnim);
                }
                // add for celllayout topbar
                LauncherViewPropertyAnimator homebuttonAnima =
                        new LauncherViewPropertyAnimator(cl.getmHomeButton());
                homebuttonAnima.alpha(finalHomebuttonAlpa)
                        .setDuration(duration)
                        .setInterpolator(mZoomInInterpolator);
                homebuttonAnima.addListener(new AlphaUpdateListener(cl.getmHomeButton(),
                        true));
                mStateAnimator.play(homebuttonAnima);
                // add end
                if (oldBackgroundAlpha != 0 || finalBackgroundAlpha != 0) {
                    ValueAnimator bgAnim = ObjectAnimator.ofFloat(cl, "backgroundAlpha",
                            oldBackgroundAlpha, finalBackgroundAlpha);
                    bgAnim.setInterpolator(mZoomInInterpolator);
                    bgAnim.setDuration(duration);
                    mStateAnimator.play(bgAnim);
                }
            } else {
                cl.setBackgroundAlpha(finalBackgroundAlpha);
                cl.setShortcutAndWidgetAlpha(finalAlpha);
                // add for celllayout topbar
                cl.getShortcutsAndWidgets().setTranslationY(finalTranslationY);
                cl.getShortcutsAndWidgets().setScaleY(finalScale);
                cl.getShortcutsAndWidgets().setScaleX(finalScale);
                cl.getmHomeButton().setTranslationY(finalTranslationY);
                cl.getmHomeButton().setAlpha(finalHomebuttonAlpa);
                cl.getmHomeButton().setVisibility(finalHomebuttonAlpa == 0f ? View.GONE : View.VISIBLE);
                // add end
            }

            if (Workspace.isQsbContainerPage(i)) {
                if (animated) {
                    Animator anim = mWorkspace.mQsbAlphaController
                            .animateAlphaAtIndex(finalAlpha, Workspace.QSB_ALPHA_INDEX_PAGE_SCROLL);
                    anim.setDuration(duration);
                    anim.setInterpolator(mZoomInInterpolator);
                    mStateAnimator.play(anim);
                } else {
                    mWorkspace.mQsbAlphaController.setAlphaAtIndex(
                            finalAlpha, Workspace.QSB_ALPHA_INDEX_PAGE_SCROLL);
                }
            }
        }

        final View qsbContainer = mLauncher.getQsbContainer();
        // add for pageindicator animal
        final View pageIndicatorOrig = mWorkspace.getPageIndicator();
        final View pageIndicatorCube = mWorkspace.getmPageIndicatorCube();
        // add end

        Animator qsbAlphaAnimation = mWorkspace.mQsbAlphaController
                .animateAlphaAtIndex(finalQsbAlpha, Workspace.QSB_ALPHA_INDEX_STATE_CHANGE);

        if (animated) {
            LauncherViewPropertyAnimator scale = new LauncherViewPropertyAnimator(mWorkspace);
            scale.scaleX(mNewScale)
                    .scaleY(mNewScale)
                    .translationY(finalWorkspaceTranslationY)
                    .setDuration(duration)
                    .setInterpolator(mZoomInInterpolator);
            mStateAnimator.play(scale);
            Animator hotseatAlpha = mWorkspace.createHotseatAlphaAnimator(finalHotseatAlpha);

            LauncherViewPropertyAnimator overviewPanelAlpha =
                    new LauncherViewPropertyAnimator(overviewPanel).alpha(finalOverviewPanelAlpha)
                            .translationY(finalOverviewPanelTranslationY);// add translationY for overview anima
            overviewPanelAlpha.addListener(new AlphaUpdateListener(overviewPanel,
                    accessibilityEnabled));

            // For animation optimization, we may need to provide the Launcher transition
            // with a set of views on which to force build and manage layers in certain scenarios.
            layerViews.put(overviewPanel, LauncherStateTransitionAnimation.BUILD_AND_SET_LAYER);
            layerViews.put(qsbContainer, LauncherStateTransitionAnimation.BUILD_AND_SET_LAYER);
            layerViews.put(mLauncher.getHotseat(),
                    LauncherStateTransitionAnimation.BUILD_AND_SET_LAYER);
            layerViews.put(mWorkspace.getPageIndicator(),
                    LauncherStateTransitionAnimation.BUILD_AND_SET_LAYER);

            if (states.workspaceToOverview) {
                hotseatAlpha.setInterpolator(new DecelerateInterpolator(2));
                overviewPanelAlpha.setInterpolator(null);
            } else if (states.overviewToWorkspace) {
                hotseatAlpha.setInterpolator(null);
                overviewPanelAlpha.setInterpolator(new DecelerateInterpolator(2));
            }

            // add deal page indicator
            Animator pageIndicatorOrigAnima = null;
            if (pageIndicatorOrig != null) {
                pageIndicatorOrigAnima = new LauncherViewPropertyAnimator(pageIndicatorOrig)
                        .alpha(finalPageIndicatorAlpha)
                        .translationY(finalPageIndicatorTranslationY).withLayer();
                pageIndicatorOrigAnima.addListener(new AlphaUpdateListener(pageIndicatorOrig,
                        accessibilityEnabled));  // remove
            } else {
                // create a dummy animation so we don't need to do null checks later
                pageIndicatorOrigAnima = ValueAnimator.ofFloat(0, 0);
            }

            Animator pageIndicatorCubeAnima;
            if (pageIndicatorCube != null) {
                pageIndicatorCubeAnima = new LauncherViewPropertyAnimator(pageIndicatorCube)
                        .alpha(finalPageIndicatorCubeAlpha).withLayer()
                        .scaleX(finalPageIndicatorCubeScale)
                        .scaleY(finalPageIndicatorCubeScale);
                pageIndicatorCubeAnima.addListener(new AlphaUpdateListener(pageIndicatorCube,
                        accessibilityEnabled));
            } else {
                // create a dummy animation so we don't need to do null checks later
                pageIndicatorCubeAnima = ValueAnimator.ofFloat(0, 0);
            }
            // add end

            // add for aline icon
            Animator alineIconAnima;
            View alineIcon = mLauncher.getmAlineButton();
            if(alineIcon != null ) {
                alineIconAnima = new LauncherViewPropertyAnimator(alineIcon)
                        .alpha(finalAlineButtonAlpa).withLayer();
                alineIconAnima.addListener(new AlphaUpdateListener(alineIcon,
                        true));
                alineIconAnima.setDuration(duration);
                mStateAnimator.play(alineIconAnima);
            }
            // add end

            overviewPanelAlpha.setDuration(duration);
            hotseatAlpha.setDuration(duration);
            qsbAlphaAnimation.setDuration(duration);

            // add for pageIndicator anima
            mStateAnimator.play(pageIndicatorOrigAnima);
            mStateAnimator.play(pageIndicatorCubeAnima);
            // add end

            // modify to play overviewPanelAlpha after 0.3*duration
            //mPageIndicatorDiagitAnimator.play(overviewPanelAlpha);
            if (states.stateIsOverview) {
                mStateAnimator.play(overviewPanelAlpha).after((long) (duration * 0.3));
            } else {
                mStateAnimator.play(overviewPanelAlpha);
            }
            // modify end

            // add for invisiable hotseat in  special situation
            if(!mLauncher.isSuccessAddIcon()) {
                mStateAnimator.play(hotseatAlpha);
            }
            // modify end
            mStateAnimator.play(qsbAlphaAnimation);
            mStateAnimator.addListener(new AnimatorListenerAdapter() {
                boolean canceled = false;
                @Override
                public void onAnimationCancel(Animator animation) {
                    canceled = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mStateAnimator = null;
                    if (canceled) return;
                    if (accessibilityEnabled && overviewPanel.getVisibility() == View.VISIBLE) {
                        overviewPanel.getChildAt(0).performAccessibilityAction(
                                AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                    }
                }
            });
        } else {
            overviewPanel.setAlpha(finalOverviewPanelAlpha);
            AlphaUpdateListener.updateVisibility(overviewPanel, accessibilityEnabled);

            qsbAlphaAnimation.end();
            mWorkspace.createHotseatAlphaAnimator(finalHotseatAlpha).end();
            mWorkspace.updateCustomContentVisibility();
            mWorkspace.setScaleX(mNewScale);
            mWorkspace.setScaleY(mNewScale);
            mWorkspace.setTranslationY(finalWorkspaceTranslationY);

            if (accessibilityEnabled && overviewPanel.getVisibility() == View.VISIBLE) {
                overviewPanel.getChildAt(0).performAccessibilityAction(
                        AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
            }

            View alineIcon = mLauncher.getmAlineButton();
            // add start
            if (alineIcon != null) {
                alineIcon.setAlpha(finalAlineButtonAlpa);
                alineIcon.setVisibility(finalAlineButtonAlpa == 0f ? View.GONE : View.VISIBLE);
            }
            // add end
        }
    }

    /**
     * Animates the background scrim. Add to the state animator to prevent jankiness.
     *
     * @param states the current and final workspace states
     * @param animated whether or not to set the background alpha immediately
     * @duration duration of the animation
     */
    private void animateBackgroundGradient(TransitionStates states,
            boolean animated, int duration) {

        final DragLayer dragLayer = mLauncher.getDragLayer();
        final float startAlpha = dragLayer.getBackgroundAlpha();
        float finalAlpha = states.stateIsNormal || states.stateIsNormalHidden ?
                0 : mWorkspaceScrimAlpha;

        if (finalAlpha != startAlpha) {
            if (animated) {
                // These properties refer to the background protection gradient used for AllApps
                // and Widget tray.
                ValueAnimator bgFadeOutAnimation = ValueAnimator.ofFloat(startAlpha, finalAlpha);
                bgFadeOutAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        dragLayer.setBackgroundAlpha(
                                ((Float)animation.getAnimatedValue()).floatValue());
                    }
                });
                bgFadeOutAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
                bgFadeOutAnimation.setDuration(duration);
                mStateAnimator.play(bgFadeOutAnimation);
            } else {
                dragLayer.setBackgroundAlpha(finalAlpha);
            }
        }
    }

    /**
     * Cancels the current animation.
     */
    private void cancelAnimation() {
        if (mStateAnimator != null) {
            mStateAnimator.setDuration(0);
            mStateAnimator.cancel();
        }
        mStateAnimator = null;
    }
}