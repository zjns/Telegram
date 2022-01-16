package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.LanguageCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class CustomizeActivity extends BaseFragment {
    ListAdapter listAdapter;
    RecyclerListView listView;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("Customize", R.string.Customize));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }
        });

        return fragmentView;
    }

    static class ListAdapter extends RecyclerListView.SelectionAdapter {

        Context mContext;

        ListAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = new CustomizeSettings(mContext);
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            CustomizeSettings customizeSettings = (CustomizeSettings) holder.itemView;
            customizeSettings.updateHeight();
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }
    }

    static class CustomizeSettings extends LinearLayout {
        private SharedPreferences preferences;

        private final TextCheckCell disableBitcoinCheck;
        private final TextCheckCell allowForwardCheck;
        private final TextCheckCell allowScreenshotCheck;
        private final TextCheckCell disableTypingCheckCheck;
        private final TextCheckCell preferMessagesCheck;

        private SharedPreferences.OnSharedPreferenceChangeListener listener;

        CustomizeSettings(Context context) {
            super(context);
            setOrientation(VERTICAL);

            preferences = MessagesController.getGlobalMainSettings();

            disableBitcoinCheck = new TextCheckCell(context);
            disableBitcoinCheck.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));
            disableBitcoinCheck.setTextAndCheck(
                    LocaleController.getString("CustomizeDisableBitcoinMsg", R.string.CustomizeDisableBitcoinMsg),
                    getBooleanValue("disable_bitcoin_message", false),
                    true
            );
            disableBitcoinCheck.setOnClickListener(e -> {
                setBooleanValue("disable_bitcoin_message", !getBooleanValue("disable_bitcoin_message", false));
            });
            addView(disableBitcoinCheck, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            allowForwardCheck = new TextCheckCell(context);
            allowForwardCheck.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));
            allowForwardCheck.setTextAndCheck(
                    LocaleController.getString("CustomizeAllowForward", R.string.CustomizeAllowForward),
                    getBooleanValue("allow_chat_forward", false),
                    true
            );
            allowForwardCheck.setOnClickListener(e -> {
                setBooleanValue("allow_chat_forward", !getBooleanValue("allow_chat_forward", false));
            });
            addView(allowForwardCheck, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            allowScreenshotCheck = new TextCheckCell(context);
            allowScreenshotCheck.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));
            allowScreenshotCheck.setTextAndCheck(
                    LocaleController.getString("CustomizeAllowScreenshot", R.string.CustomizeAllowScreenshot),
                    getBooleanValue("allow_screenshot", false),
                    true
            );
            allowScreenshotCheck.setOnClickListener(e -> {
                setBooleanValue("allow_screenshot", !getBooleanValue("allow_screenshot", false));
            });
            addView(allowScreenshotCheck, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            disableTypingCheckCheck = new TextCheckCell(context);
            disableTypingCheckCheck.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));
            disableTypingCheckCheck.setTextAndCheck(
                    LocaleController.getString("CustomizeDisableTypingCheck", R.string.CustomizeDisableTypingCheck),
                    getBooleanValue("disable_typing_check", false),
                    false
            );
            disableTypingCheckCheck.setOnClickListener(e -> {
                setBooleanValue("disable_typing_check", !getBooleanValue("disable_typing_check", false));
            });
            addView(disableTypingCheckCheck, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            preferMessagesCheck = new TextCheckCell(context);
            preferMessagesCheck.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));
            preferMessagesCheck.setTextAndCheck(
                    LocaleController.getString("CustomizePreferMessages", R.string.CustomizePreferMessages),
                    getBooleanValue("prefer_messages", false),
                    false
            );
            preferMessagesCheck.setOnClickListener(e -> {
                setBooleanValue("prefer_messages", !getBooleanValue("prefer_messages", false));
            });
            addView(preferMessagesCheck, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            updateHeight();
        }

        private boolean getBooleanValue(String key, boolean defValue) {
            return preferences.getBoolean(key, defValue);
        }

        private void setBooleanValue(String key, boolean value) {
            preferences.edit().putBoolean(key, value).apply();
        }

        @Override
        protected void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            updateHeight();
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            updateHeight();
            super.onLayout(changed, l, t, r, b);
        }

        void updateHeight() {
            int newHeight = height();
            if (getLayoutParams() == null)
                setLayoutParams(new RecyclerView.LayoutParams(LayoutHelper.MATCH_PARENT, newHeight));
            else if (getLayoutParams().height != newHeight) {
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) getLayoutParams();
                lp.height = newHeight;
                setLayoutParams(lp);
            }
        }

        void update() {
            disableBitcoinCheck.setChecked(getBooleanValue("disable_bitcoin_message", false));
            allowForwardCheck.setChecked(getBooleanValue("allow_chat_forward", false));
            allowScreenshotCheck.setChecked(getBooleanValue("allow_screenshot", false));
            disableTypingCheckCheck.setChecked(getBooleanValue("disable_typing_check", false));
            preferMessagesCheck.setChecked(getBooleanValue("prefer_messages", false));
        }

        int height() {
            return Math.max(AndroidUtilities.dp(50), disableBitcoinCheck.getMeasuredHeight()) * 5;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            updateHeight();
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            update();
            preferences.registerOnSharedPreferenceChangeListener(listener = (sharedPreferences, s) -> {
                preferences = sharedPreferences;
                update();
            });
            updateHeight();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            preferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{LanguageCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LanguageCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LanguageCell.class}, new String[]{"textView2"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LanguageCell.class}, new String[]{"checkImage"}, null, null, null, Theme.key_featuredStickers_addedIcon));

        return themeDescriptions;
    }
}
