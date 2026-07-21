/*
 * This is the source code of VortexGram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @Radolyn, 2023
 */

package com.radolyn.ayugram.ui.preferences;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.exteragram.messenger.preferences.BasePreferencesActivity;
import com.radolyn.ayugram.AyuConfig;
import com.radolyn.ayugram.AyuConstants;
import com.radolyn.ayugram.database.AyuData;
import com.radolyn.ayugram.ui.preferences.utils.AyuUi;
import org.telegram.messenger.*;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.*;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.RecyclerListView;

/**
 * VortexGram Preferences — Phase 1 subset.
 * Ghost-mode and AyuSync sections aren't ported yet; those land with their
 * respective network hooks in a later phase.
 */
public class VortexGramPreferencesActivity extends BasePreferencesActivity {

    private static final int TOGGLE_BUTTON_VIEW = 1000;

    private int spyHeaderRow;
    private int saveDeletedMessagesRow;
    private int saveMessagesHistoryRow;
    private int spyDivider1Row;
    private int messageSavingBtnRow;
    private int spyDivider2Row;

    private int qolHeaderRow;
    private int keepAliveServiceRow;
    private int disableAdsRow;
    private int localPremiumRow;
    private int antiKickRow;
    private int filtersRow;
    private int qolDividerRow;

    private int customizationHeaderRow;
    private int deletedMarkTextRow;
    private int editedMarkTextRow;
    private int customizationDividerRow;

    private int debugHeaderRow;
    private int WALModeRow;
    private int buttonsDividerRow;
    private int clearAyuDatabaseBtnRow;

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        spyHeaderRow = newRow();
        saveDeletedMessagesRow = newRow();
        saveMessagesHistoryRow = newRow();
        spyDivider1Row = newRow();
        messageSavingBtnRow = newRow();
        spyDivider2Row = newRow();

        qolHeaderRow = newRow();
        keepAliveServiceRow = newRow();
        disableAdsRow = newRow();
        localPremiumRow = newRow();
        antiKickRow = newRow();
        filtersRow = newRow();
        qolDividerRow = newRow();

        customizationHeaderRow = newRow();
        deletedMarkTextRow = newRow();
        editedMarkTextRow = newRow();
        customizationDividerRow = newRow();

        debugHeaderRow = newRow();
        WALModeRow = newRow();
        buttonsDividerRow = newRow();
        clearAyuDatabaseBtnRow = newRow();
    }

    private void toggleLocalPremium() {
        boolean newState = !AyuConfig.localPremium;

        AyuConfig.editor.putBoolean("localPremium", AyuConfig.localPremium = newState).apply();
        listAdapter.notifyItemChanged(localPremiumRow, AyuConfig.localPremium);

        getMessagesController().updatePremium(AyuConfig.localPremium);
        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.currentUserPremiumStatusChanged);
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.premiumStatusChangedGlobal);

        getMediaDataController().loadPremiumPromo(false);
        getMediaDataController().loadReactions(false, null);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == saveDeletedMessagesRow) {
            AyuConfig.editor.putBoolean("saveDeletedMessages", AyuConfig.saveDeletedMessages ^= true).apply();
            ((TextCheckCell) view).setChecked(AyuConfig.saveDeletedMessages);
        } else if (position == saveMessagesHistoryRow) {
            AyuConfig.editor.putBoolean("saveMessagesHistory", AyuConfig.saveMessagesHistory ^= true).apply();
            ((TextCheckCell) view).setChecked(AyuConfig.saveMessagesHistory);
        } else if (position == messageSavingBtnRow) {
            presentFragment(new MessageSavingPreferencesActivity());
        } else if (position == keepAliveServiceRow) {
            AyuConfig.editor.putBoolean("keepAliveService", AyuConfig.keepAliveService ^= true).apply();
            ((TextCheckCell) view).setChecked(AyuConfig.keepAliveService);
        } else if (position == disableAdsRow) {
            AyuConfig.editor.putBoolean("disableAds", AyuConfig.disableAds ^= true).apply();
            ((TextCheckCell) view).setChecked(AyuConfig.disableAds);
        } else if (position == localPremiumRow) {
            toggleLocalPremium();
        } else if (position == antiKickRow) {
            AyuConfig.editor.putBoolean("antiKick", AyuConfig.antiKick ^= true).apply();
            ((TextCheckCell) view).setChecked(AyuConfig.antiKick);
        } else if (position == filtersRow) {
            NotificationsCheckCell checkCell = (NotificationsCheckCell) view;
            if (LocaleController.isRTL && x <= AndroidUtilities.dp(76) || !LocaleController.isRTL && x >= view.getMeasuredWidth() - AndroidUtilities.dp(76)) {
                AyuConfig.editor.putBoolean("regexFiltersEnabled", AyuConfig.regexFiltersEnabled ^= true).apply();
                checkCell.setChecked(AyuConfig.regexFiltersEnabled, 0);
            } else {
                presentFragment(new RegexFiltersPreferencesActivity());
            }
        } else if (position == deletedMarkTextRow) {
            AyuUi.spawnEditBox(
                    getParentActivity(),
                    ((TextCell) view),
                    LocaleController.getString(R.string.DeletedMarkText),
                    AyuConfig::getDeletedMark,
                    "deletedMarkText",
                    AyuConstants.DEFAULT_DELETED_MARK
            );
        } else if (position == editedMarkTextRow) {
            AyuUi.spawnEditBox(
                    getParentActivity(),
                    ((TextCell) view),
                    LocaleController.getString(R.string.EditedMarkText),
                    AyuConfig::getEditedMark,
                    "editedMarkText",
                    LocaleController.getString("EditedMessage", R.string.EditedMessage) // don't remove key
            );
        } else if (position == WALModeRow) {
            AyuConfig.editor.putBoolean("WALMode", AyuConfig.WALMode ^= true).apply();
            ((TextCheckCell) view).setChecked(AyuConfig.WALMode);
        } else if (position == clearAyuDatabaseBtnRow) {
            AyuData.clean();

            ((TextCell) view).setValue("…", true);

            BulletinFactory.of(this).createSimpleBulletin(R.raw.info, LocaleController.getString(R.string.ClearAyuDatabaseNotification)).show();
        }
    }

    @Override
    protected String getTitle() {
        return LocaleController.getString(R.string.AyuPreferences);
    }

    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new ListAdapter(context);
    }

    private class ListAdapter extends BaseListAdapter {

        public ListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean payload) {
            switch (holder.getItemViewType()) {
                case 1:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 2:
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == messageSavingBtnRow) {
                        textCell.setText(LocaleController.getString(R.string.MessageSavingBtn), false);
                    } else if (position == deletedMarkTextRow) {
                        textCell.setTextAndValue(LocaleController.getString(R.string.DeletedMarkText), AyuConfig.getDeletedMark(), true);
                    } else if (position == editedMarkTextRow) {
                        textCell.setTextAndValue(LocaleController.getString(R.string.EditedMarkText), AyuConfig.getEditedMark(), true);
                    } else if (position == clearAyuDatabaseBtnRow) {
                        java.io.File file = ApplicationLoader.applicationContext.getDatabasePath(AyuConstants.AYU_DATABASE);
                        long size = file.exists() ? file.length() : 0;

                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ClearAyuDatabase), AndroidUtilities.formatFileSize(size), R.drawable.msg_delete, false);
                        textCell.setColors(Theme.key_text_RedBold, Theme.key_text_RedBold);
                    }
                    break;
                case 3:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == spyHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.SpyEssentialsHeader));
                    } else if (position == qolHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.QoLTogglesHeader));
                    } else if (position == customizationHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.CustomizationHeader));
                    } else if (position == debugHeaderRow) {
                        headerCell.setText(LocaleController.getString("SettingsDebug", R.string.SettingsDebug));
                    }
                    break;
                case 5:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == saveDeletedMessagesRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.SaveDeletedMessages), AyuConfig.saveDeletedMessages, true);
                    } else if (position == saveMessagesHistoryRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.SaveMessagesHistory), AyuConfig.saveMessagesHistory, false);
                    } else if (position == keepAliveServiceRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.KeepAliveService), AyuConfig.keepAliveService, true);
                    } else if (position == disableAdsRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.DisableAds), AyuConfig.disableAds, true);
                    } else if (position == localPremiumRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.LocalPremium) + " β", AyuConfig.localPremium, true);
                    } else if (position == antiKickRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.AntiKick), AyuConfig.antiKick, true);
                    } else if (position == WALModeRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.WALMode), AyuConfig.WALMode, false);
                    }
                    break;
                case TOGGLE_BUTTON_VIEW:
                    NotificationsCheckCell notificationsCheckCell = (NotificationsCheckCell) holder.itemView;
                    if (position == filtersRow) {
                        int count = AyuConfig.getRegexFilters().size();
                        notificationsCheckCell.setTextAndValueAndCheck(LocaleController.getString(R.string.RegexFilters), count + " " + LocaleController.getString(R.string.RegexFiltersAmount), AyuConfig.regexFiltersEnabled, false);
                    }
                    break;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TOGGLE_BUTTON_VIEW) {
                NotificationsCheckCell view = new NotificationsCheckCell(mContext);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                return new RecyclerListView.Holder(view);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

        @Override
        public int getItemViewType(int position) {
            if (
                    position == spyDivider1Row ||
                            position == spyDivider2Row ||
                            position == qolDividerRow ||
                            position == customizationDividerRow ||
                            position == buttonsDividerRow
            ) {
                return 1;
            } else if (
                    position == messageSavingBtnRow ||
                            position == deletedMarkTextRow ||
                            position == editedMarkTextRow ||
                            position == clearAyuDatabaseBtnRow
            ) {
                return 2;
            } else if (
                    position == spyHeaderRow ||
                            position == qolHeaderRow ||
                            position == customizationHeaderRow ||
                            position == debugHeaderRow
            ) {
                return 3;
            } else if (
                    position == filtersRow
            ) {
                return TOGGLE_BUTTON_VIEW;
            }
            return 5;
        }
    }
}
