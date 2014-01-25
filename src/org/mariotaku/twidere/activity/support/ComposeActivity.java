/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity.support;

import static android.os.Environment.getExternalStorageState;
import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.model.ParcelableLocation.isValidLocation;
import static org.mariotaku.twidere.util.ParseUtils.parseString;
import static org.mariotaku.twidere.util.ThemeUtils.getActionBarBackground;
import static org.mariotaku.twidere.util.ThemeUtils.getComposeThemeResource;
import static org.mariotaku.twidere.util.ThemeUtils.getUserThemeColor;
import static org.mariotaku.twidere.util.ThemeUtils.getWindowContentOverlayForCompose;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserColor;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.copyStream;
import static org.mariotaku.twidere.util.Utils.getAccountColors;
import static org.mariotaku.twidere.util.Utils.getAccountIds;
import static org.mariotaku.twidere.util.Utils.getAccountName;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;
import static org.mariotaku.twidere.util.Utils.getDisplayName;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.getShareStatus;
import static org.mariotaku.twidere.util.Utils.getStatusBackground;
import static org.mariotaku.twidere.util.Utils.getStatusTypeIconRes;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.openImageDirectly;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;
import static org.mariotaku.twidere.util.Utils.showMenuItemToast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.LongSparseArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.scvngr.levelup.views.gallery.AdapterView;
import com.scvngr.levelup.views.gallery.AdapterView.OnItemClickListener;
import com.scvngr.levelup.views.gallery.AdapterView.OnItemLongClickListener;
import com.scvngr.levelup.views.gallery.Gallery;
import com.twitter.Extractor;
import com.twitter.Validator;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.CroutonStyle;

import org.mariotaku.menucomponent.widget.MenuBar;
import org.mariotaku.menucomponent.widget.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.BaseArrayAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.DraftItem;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.provider.TweetStore.CacheFiles;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.ComposeTextCountView;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

public class ComposeActivity extends BaseSupportDialogActivity implements TextWatcher, LocationListener,
		OnMenuItemClickListener, OnClickListener, OnEditorActionListener, OnItemClickListener, OnItemLongClickListener,
		OnLongClickListener {

	private static final String FAKE_IMAGE_LINK = "https://www.example.com/fake_image.jpg";

	private static final String EXTRA_IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";

	private static final String EXTRA_SHOULD_SAVE_ACCOUNTS = "should_save_accounts";

	private static final String EXTRA_ORIGINAL_TEXT = "original_text";

	private final Validator mValidator = new Validator();
	private final Extractor mExtractor = new Extractor();

	private AsyncTwitterWrapper mTwitterWrapper;
	private LocationManager mLocationManager;
	private SharedPreferencesWrapper mPreferences;
	private ParcelableLocation mRecentLocation;

	private ContentResolver mResolver;
	private ImageLoaderWrapper mImageLoader;
	private AsyncTask<Void, Void, ?> mTask;
	private PopupMenu mPopupMenu;
	private TextView mTitleView, mSubtitleView;
	private ImageView mImageThumbnailPreview;

	private MenuBar mBottomMenuBar, mActionMenuBar;
	private IColorLabelView mColorIndicator;
	private EditText mEditText;
	private ProgressBar mProgress;
	private Gallery mAccountSelector;
	private View mAccountSelectorDivider, mBottomSendDivider;
	private View mBottomMenuContainer;
	private AccountSelectorAdapter mAccountSelectorAdapter;

	private boolean mIsPossiblySensitive, mShouldSaveAccounts;

	private long[] mAccountIds, mSendAccountIds;

	private int mMediaType;

	private Uri mMediaUri, mTempPhotoUri;
	private boolean mImageUploaderUsed, mTweetShortenerUsed;
	private ParcelableStatus mInReplyToStatus;

	private ParcelableUser mMentionUser;
	private DraftItem mDraftItem;
	private long mInReplyToStatusId;
	private String mOriginalText;
	private View mSendView, mBottomSendView;

	@Override
	public void afterTextChanged(final Editable s) {

	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

	}

	@Override
	public Resources getResources() {
		return getThemedResources();
	}

	@Override
	public int getThemeColor() {
		return ThemeUtils.getUserThemeColor(this);
	}

	@Override
	public int getThemeResourceId() {
		return getComposeThemeResource(this);
	}

	public boolean handleMenuItem(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_TAKE_PHOTO: {
				if (mMediaType != ATTACHED_IMAGE_TYPE_PHOTO) {
					takePhoto();
				} else {
					new DeleteImageTask(this).execute();
				}
				break;
			}
			case MENU_ADD_IMAGE: {
				if (mMediaType != ATTACHED_IMAGE_TYPE_IMAGE) {
					pickImage();
				} else {
					new DeleteImageTask(this).execute();
				}
				break;
			}
			case MENU_ADD_LOCATION: {
				final boolean attach_location = mPreferences.getBoolean(KEY_ATTACH_LOCATION, false);
				if (!attach_location) {
					getLocation();
				} else {
					mLocationManager.removeUpdates(this);
				}
				mPreferences.edit().putBoolean(KEY_ATTACH_LOCATION, !attach_location).commit();
				setMenu();
				updateTextCount();
				break;
			}
			case MENU_DRAFTS: {
				startActivity(new Intent(INTENT_ACTION_DRAFTS));
				break;
			}
			case MENU_DELETE: {
				new DeleteImageTask(this).execute();
				break;
			}
			case MENU_IMAGE: {
				openImageDirectly(this, ParseUtils.parseString(mMediaUri));
				break;
			}
			case MENU_TOGGLE_SENSITIVE: {
				if (!hasMedia()) return false;
				mIsPossiblySensitive = !mIsPossiblySensitive;
				setMenu();
				updateTextCount();
				break;
			}
			case MENU_VIEW: {
				if (mInReplyToStatus == null) return false;
				final DialogFragment fragment = new ViewStatusDialogFragment();
				final Bundle args = new Bundle();
				args.putParcelable(EXTRA_STATUS, mInReplyToStatus);
				fragment.setArguments(args);
				fragment.show(getSupportFragmentManager(), "view_status");
				break;
			}
			default: {
				final Intent intent = item.getIntent();
				if (intent != null) {
					try {
						final String action = intent.getAction();
						if (INTENT_ACTION_EXTENSION_COMPOSE.equals(action)) {
							intent.putExtra(EXTRA_TEXT, ParseUtils.parseString(mEditText.getText()));
							intent.putExtra(EXTRA_ACCOUNT_IDS, mSendAccountIds);
							if (mSendAccountIds != null && mSendAccountIds.length > 0) {
								final long account_id = mSendAccountIds[0];
								intent.putExtra(EXTRA_NAME, getAccountName(this, account_id));
								intent.putExtra(EXTRA_SCREEN_NAME, getAccountScreenName(this, account_id));
							}
							if (mInReplyToStatusId > 0) {
								intent.putExtra(EXTRA_IN_REPLY_TO_ID, mInReplyToStatusId);
							}
							if (mInReplyToStatus != null) {
								intent.putExtra(EXTRA_IN_REPLY_TO_NAME, mInReplyToStatus.user_name);
								intent.putExtra(EXTRA_IN_REPLY_TO_SCREEN_NAME, mInReplyToStatus.user_screen_name);
							}
							startActivityForResult(intent, REQUEST_EXTENSION_COMPOSE);
						} else if (INTENT_ACTION_EXTENSION_EDIT_IMAGE.equals(action)) {
							final ComponentName cmp = intent.getComponent();
							if (cmp == null || !hasMedia()) return false;
							final String name = new File(mMediaUri.getPath()).getName();
							final Uri data = Uri.withAppendedPath(CacheFiles.CONTENT_URI, Uri.encode(name));
							intent.setData(data);
							grantUriPermission(cmp.getPackageName(), data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
							startActivityForResult(intent, REQUEST_EDIT_IMAGE);
						} else {
							startActivity(intent);
						}
					} catch (final ActivityNotFoundException e) {
						Log.w(LOGTAG, e);
						return false;
					}
				}
				break;
			}
		}
		return true;
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		switch (requestCode) {
			case REQUEST_TAKE_PHOTO: {
				if (resultCode == Activity.RESULT_OK) {
					mTask = new CopyImageTask(this, mMediaUri, mTempPhotoUri, createTempImageUri(),
							ATTACHED_IMAGE_TYPE_PHOTO).execute();
				}
				break;
			}
			case REQUEST_PICK_IMAGE: {
				if (resultCode == Activity.RESULT_OK) {
					final Uri src = intent.getData();
					mTask = new CopyImageTask(this, mMediaUri, src, createTempImageUri(), ATTACHED_IMAGE_TYPE_IMAGE)
							.execute();
				}
				break;
			}
			case REQUEST_EDIT_IMAGE: {
				if (resultCode == Activity.RESULT_OK) {
					final Uri uri = intent.getData();
					if (uri != null) {
						mMediaUri = uri;
						reloadAttachedImageThumbnail();
					} else {
						break;
					}
					setMenu();
					updateTextCount();
				}
				break;
			}
			case REQUEST_EXTENSION_COMPOSE: {
				if (resultCode == Activity.RESULT_OK) {
					final String text = intent.getStringExtra(EXTRA_TEXT);
					final String append = intent.getStringExtra(EXTRA_APPEND_TEXT);
					final Uri imageUri = intent.getParcelableExtra(EXTRA_IMAGE_URI);
					if (text != null) {
						mEditText.setText(text);
					} else if (append != null) {
						mEditText.append(append);
					}
					if (imageUri != null) {
						mMediaUri = imageUri;
						reloadAttachedImageThumbnail();
					}
					setMenu();
					updateTextCount();
				}
				break;
			}
		}

	}

	@Override
	public void onBackPressed() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) return;
		final String option = mPreferences.getString(KEY_COMPOSE_QUIT_ACTION, VALUE_COMPOSE_QUIT_ACTION_ASK);
		final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
		final boolean textChanged = text != null && !text.isEmpty() && !text.equals(mOriginalText);
		final boolean isEditingDraft = INTENT_ACTION_EDIT_DRAFT.equals(getIntent().getAction());
		if (VALUE_COMPOSE_QUIT_ACTION_DISCARD.equals(option)) {
			mTask = new DiscardTweetTask(this).execute();
		} else if (textChanged || hasMedia() || isEditingDraft) {
			if (VALUE_COMPOSE_QUIT_ACTION_SAVE.equals(option)) {
				saveToDrafts();
				Toast.makeText(this, R.string.status_saved_to_draft, Toast.LENGTH_SHORT).show();
				finish();
			} else {
				new UnsavedTweetDialogFragment().show(getSupportFragmentManager(), "unsaved_tweet");
			}
		} else {
			mTask = new DiscardTweetTask(this).execute();
		}
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.close: {
				onBackPressed();
				break;
			}
			case R.id.send: {
				updateStatus();
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		findViewById(R.id.close).setOnClickListener(this);
		mColorIndicator = (IColorLabelView) findViewById(R.id.accounts_color);
		mEditText = (EditText) findViewById(R.id.edit_text);
		mTitleView = (TextView) findViewById(R.id.actionbar_title);
		mSubtitleView = (TextView) findViewById(R.id.actionbar_subtitle);
		mImageThumbnailPreview = (ImageView) findViewById(R.id.image_thumbnail_preview);
		mBottomMenuBar = (MenuBar) findViewById(R.id.bottom_menu);
		mBottomMenuContainer = findViewById(R.id.bottom_menu_container);
		mActionMenuBar = (MenuBar) findViewById(R.id.action_menu);
		mProgress = (ProgressBar) findViewById(R.id.actionbar_progress_indeterminate);
		mAccountSelectorDivider = findViewById(R.id.account_selector_divider);
		mBottomSendDivider = findViewById(R.id.bottom_send_divider);
		mAccountSelector = (Gallery) findViewById(R.id.account_selector);
		final View composeActionBar = findViewById(R.id.compose_actionbar);
		final View composeBottomBar = findViewById(R.id.compose_bottombar);
		mSendView = composeActionBar.findViewById(R.id.send);
		mBottomSendView = composeBottomBar.findViewById(R.id.send);
		ViewAccessor.setBackground(findViewById(R.id.compose_content), getWindowContentOverlayForCompose(this));
		ViewAccessor.setBackground(composeActionBar, getActionBarBackground(this, getCurrentThemeResourceId()));
	}

	@Override
	public boolean onEditorAction(final TextView view, final int actionId, final KeyEvent event) {
		if (event == null) return false;
		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_ENTER: {
				updateStatus();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (isSingleAccount()) return;
		final boolean selected = !view.isActivated();
		final Account account = mAccountSelectorAdapter.getItem(position);
		final long[] prevSelectedIds = mAccountSelectorAdapter.getSelectedAccountIds();
		if (prevSelectedIds.length == 1 && prevSelectedIds[0] == account.account_id) {
			Toast.makeText(this, R.string.empty_account_selection_disallowed, Toast.LENGTH_SHORT).show();
			return;
		}
		mAccountSelectorAdapter.setAccountSelected(account.account_id, selected);
		mSendAccountIds = mAccountSelectorAdapter.getSelectedAccountIds();
		updateAccountSelection();
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final Account account = mAccountSelectorAdapter.getItem(position);
		final String displayName = getDisplayName(this, account.account_id, account.name, account.screen_name);
		showMenuItemToast(view, displayName, true);
		return true;
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (mRecentLocation == null) {
			mRecentLocation = location != null ? new ParcelableLocation(location) : null;
			setProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public boolean onLongClick(final View v) {
		switch (v.getId()) {
			case R.id.send: {
				final boolean bottomSendButton = mPreferences.getBoolean(KEY_BOTTOM_SEND_BUTTON, false);
				showMenuItemToast(v, getString(R.string.send), bottomSendButton);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		return handleMenuItem(item);
	}

	@Override
	public void onProviderDisabled(final String provider) {
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putLongArray(EXTRA_ACCOUNT_IDS, mSendAccountIds);
		outState.putInt(EXTRA_ATTACHED_IMAGE_TYPE, mMediaType);
		outState.putParcelable(EXTRA_IMAGE_URI, mMediaUri);
		outState.putBoolean(EXTRA_IS_POSSIBLY_SENSITIVE, mIsPossiblySensitive);
		outState.putParcelable(EXTRA_STATUS, mInReplyToStatus);
		outState.putLong(EXTRA_STATUS_ID, mInReplyToStatusId);
		outState.putParcelable(EXTRA_USER, mMentionUser);
		outState.putParcelable(EXTRA_DRAFT, mDraftItem);
		outState.putBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS, mShouldSaveAccounts);
		outState.putString(EXTRA_ORIGINAL_TEXT, mOriginalText);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {

	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		setMenu();
		updateTextCount();
	}

	public void saveToDrafts() {
		final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
		final ParcelableStatusUpdate.Builder builder = new ParcelableStatusUpdate.Builder();
		builder.accountIds(mSendAccountIds);
		builder.text(text);
		builder.inReplyToStatusId(mInReplyToStatusId);
		builder.location(mRecentLocation);
		builder.isPossiblySensitive(mIsPossiblySensitive);
		if (hasMedia()) {
			builder.media(mMediaUri, mMediaType);
		}
		final ContentValues values = ContentValuesCreator.makeStatusDraftContentValues(builder.build());
		mResolver.insert(Drafts.CONTENT_URI, values);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mPreferences = SharedPreferencesWrapper.getInstance(this, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mTwitterWrapper = getTwidereApplication().getTwitterWrapper();
		mResolver = getContentResolver();
		mImageLoader = getTwidereApplication().getImageLoaderWrapper();
		setContentView(R.layout.compose);
		setProgressBarIndeterminateVisibility(false);
		setFinishOnTouchOutside(false);
		mAccountIds = getAccountIds(this);
		if (mAccountIds.length <= 0) {
			final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
			intent.setClass(this, SignInActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		mBottomMenuBar.setIsBottomBar(true);
		mBottomMenuBar.setOnMenuItemClickListener(this);
		mActionMenuBar.setOnMenuItemClickListener(this);
		mEditText.setOnEditorActionListener(mPreferences.getBoolean(KEY_QUICK_SEND, false) ? this : null);
		mEditText.addTextChangedListener(this);
		mAccountSelectorAdapter = new AccountSelectorAdapter(this);
		mAccountSelector.setAdapter(mAccountSelectorAdapter);
		mAccountSelector.setOnItemClickListener(this);
		mAccountSelector.setOnItemLongClickListener(this);
		mAccountSelector.setScrollAfterItemClickEnabled(false);
		mAccountSelector.setScrollRightSpacingEnabled(false);

		final Intent intent = getIntent();

		if (savedInstanceState != null) {
			// Restore from previous saved state
			mSendAccountIds = savedInstanceState.getLongArray(EXTRA_ACCOUNT_IDS);
			mMediaType = savedInstanceState.getInt(EXTRA_ATTACHED_IMAGE_TYPE, ATTACHED_IMAGE_TYPE_NONE);
			mIsPossiblySensitive = savedInstanceState.getBoolean(EXTRA_IS_POSSIBLY_SENSITIVE);
			mMediaUri = savedInstanceState.getParcelable(EXTRA_IMAGE_URI);
			mInReplyToStatus = savedInstanceState.getParcelable(EXTRA_STATUS);
			mInReplyToStatusId = savedInstanceState.getLong(EXTRA_STATUS_ID);
			mMentionUser = savedInstanceState.getParcelable(EXTRA_USER);
			mDraftItem = savedInstanceState.getParcelable(EXTRA_DRAFT);
			mShouldSaveAccounts = savedInstanceState.getBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS);
			mOriginalText = savedInstanceState.getString(EXTRA_ORIGINAL_TEXT);
		} else {
			// The activity was first created
			final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
			final long notificationAccount = intent.getLongExtra(EXTRA_NOTIFICATION_ACCOUNT, -1);
			if (notificationId != -1) {
				mTwitterWrapper.clearNotificationAsync(notificationId, notificationAccount);
			}
			if (!handleIntent(intent)) {
				handleDefaultIntent(intent);
			}
			if (mSendAccountIds == null || mSendAccountIds.length == 0) {
				final long[] ids_in_prefs = ArrayUtils.parseLongArray(
						mPreferences.getString(KEY_COMPOSE_ACCOUNTS, null), ',');
				final long[] intersection = ArrayUtils.intersection(ids_in_prefs, mAccountIds);
				mSendAccountIds = intersection.length > 0 ? intersection : mAccountIds;
			}
			mOriginalText = ParseUtils.parseString(mEditText.getText());
		}
		if (!setComposeTitle(intent)) {
			setTitle(R.string.compose);
		}

		reloadAttachedImageThumbnail();

		final boolean bottomSendButton = mPreferences.getBoolean(KEY_BOTTOM_SEND_BUTTON, false);
		final boolean useBottomMenu = isSingleAccount() || !bottomSendButton;
		if (useBottomMenu) {
			mBottomMenuBar.inflate(R.menu.menu_compose);
		} else {
			mActionMenuBar.inflate(R.menu.menu_compose);
		}
		mBottomMenuBar.setVisibility(useBottomMenu ? View.VISIBLE : View.GONE);
		mActionMenuBar.setVisibility(useBottomMenu ? View.GONE : View.VISIBLE);
		mSendView.setVisibility(bottomSendButton ? View.GONE : View.VISIBLE);
		mBottomSendDivider.setVisibility(bottomSendButton ? View.VISIBLE : View.GONE);
		mBottomSendView.setVisibility(bottomSendButton ? View.VISIBLE : View.GONE);
		mSendView.setOnLongClickListener(this);
		mBottomSendView.setOnLongClickListener(this);
		final Menu menu = mBottomMenuBar.getMenu(), actionBarMenu = mActionMenuBar.getMenu();
		final Menu showingMenu = bottomSendButton ? actionBarMenu : menu;
		if (showingMenu != null) {
			final Intent compose_extensions_intent = new Intent(INTENT_ACTION_EXTENSION_COMPOSE);
			addIntentToMenu(this, showingMenu, compose_extensions_intent, MENU_GROUP_COMPOSE_EXTENSION);
			final Intent image_extensions_intent = new Intent(INTENT_ACTION_EXTENSION_EDIT_IMAGE);
			addIntentToMenu(this, showingMenu, image_extensions_intent, MENU_GROUP_IMAGE_EXTENSION);
		}
		final LinearLayout.LayoutParams bottomMenuContainerParams = (LinearLayout.LayoutParams) mBottomMenuContainer
				.getLayoutParams();
		final LinearLayout.LayoutParams accountSelectorParams = (LinearLayout.LayoutParams) mAccountSelector
				.getLayoutParams();
		final int maxItemsShown;
		final Resources res = getResources();
		if (isSingleAccount()) {
			accountSelectorParams.weight = 0;
			accountSelectorParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
			bottomMenuContainerParams.weight = 1;
			bottomMenuContainerParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
			maxItemsShown = res.getInteger(R.integer.max_compose_menu_buttons_bottom_singleaccount);
			mAccountSelectorDivider.setVisibility(View.VISIBLE);
		} else {
			accountSelectorParams.weight = 1;
			accountSelectorParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
			bottomMenuContainerParams.weight = 0;
			bottomMenuContainerParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
			maxItemsShown = res.getInteger(R.integer.max_compose_menu_buttons_bottom);
			mAccountSelectorDivider.setVisibility(bottomSendButton ? View.GONE : View.VISIBLE);
		}
		mBottomMenuContainer.setLayoutParams(bottomMenuContainerParams);
		mBottomMenuBar.setMaxItemsShown(maxItemsShown);
		setMenu();
		updateAccountSelection();
	}

	@Override
	protected void onStart() {
		super.onStart();
		final String uploader_component = mPreferences.getString(KEY_IMAGE_UPLOADER, null);
		final String shortener_component = mPreferences.getString(KEY_TWEET_SHORTENER, null);
		mImageUploaderUsed = !isEmpty(uploader_component);
		mTweetShortenerUsed = !isEmpty(shortener_component);
		setMenu();
		updateTextCount();
		final int text_size = mPreferences.getInt(KEY_TEXT_SIZE, getDefaultTextSize(this));
		mEditText.setTextSize(text_size * 1.25f);
	}

	@Override
	protected void onStop() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		mLocationManager.removeUpdates(this);
		super.onStop();
	}

	@Override
	protected void onTitleChanged(final CharSequence title, final int color) {
		super.onTitleChanged(title, color);
		mTitleView.setText(title);
	}

	private Uri createTempImageUri() {
		final File file = new File(getCacheDir(), "tmp_image_" + System.currentTimeMillis());
		return Uri.fromFile(file);
	}

	/**
	 * The Location Manager manages location providers. This code searches for
	 * the best provider of data (GPS, WiFi/cell phone tower lookup, some other
	 * mechanism) and finds the last known location.
	 **/
	private boolean getLocation() {
		final Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		final String provider = mLocationManager.getBestProvider(criteria, true);

		if (provider != null) {
			final Location location;
			if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			} else {
				location = mLocationManager.getLastKnownLocation(provider);
			}
			if (location == null) {
				mLocationManager.requestLocationUpdates(provider, 0, 0, this);
				setProgressVisibility(true);
			}
			mRecentLocation = location != null ? new ParcelableLocation(location) : null;
		} else {
			Crouton.showText(this, R.string.cannot_get_location, CroutonStyle.ALERT);
		}
		return provider != null;
	}

	private boolean handleDefaultIntent(final Intent intent) {
		if (intent == null) return false;
		final String action = intent.getAction();
		mShouldSaveAccounts = !Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action);
		final Uri data = intent.getData();
		if (data != null) {
			mMediaUri = data;
		}
		final CharSequence extra_subject = intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT);
		final CharSequence extra_text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
		final Uri extra_stream = intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (extra_stream != null) {
			new CopyImageTask(this, mMediaUri, extra_stream, createTempImageUri(), ATTACHED_IMAGE_TYPE_IMAGE).execute();
		}
		mEditText.setText(getShareStatus(this, extra_subject, extra_text));
		final int selection_end = mEditText.length();
		mEditText.setSelection(selection_end);
		return true;
	}

	private boolean handleEditDraftIntent(final DraftItem draft) {
		if (draft == null) return false;
		mEditText.setText(draft.text);
		final int selection_end = mEditText.length();
		mEditText.setSelection(selection_end);
		mSendAccountIds = draft.account_ids;
		mMediaUri = draft.media_uri != null ? Uri.parse(draft.media_uri) : null;
		mMediaType = draft.media_type;
		mIsPossiblySensitive = draft.is_possibly_sensitive;
		mInReplyToStatusId = draft.in_reply_to_status_id;
		return true;
	}

	private boolean handleIntent(final Intent intent) {
		final String action = intent.getAction();
		mShouldSaveAccounts = false;
		mMentionUser = intent.getParcelableExtra(EXTRA_USER);
		mInReplyToStatus = intent.getParcelableExtra(EXTRA_STATUS);
		mInReplyToStatusId = mInReplyToStatus != null ? mInReplyToStatus.id : -1;
		if (INTENT_ACTION_REPLY.equals(action))
			return handleReplyIntent(mInReplyToStatus);
		else if (INTENT_ACTION_QUOTE.equals(action))
			return handleQuoteIntent(mInReplyToStatus);
		else if (INTENT_ACTION_EDIT_DRAFT.equals(action)) {
			mDraftItem = intent.getParcelableExtra(EXTRA_DRAFT);
			return handleEditDraftIntent(mDraftItem);
		} else if (INTENT_ACTION_MENTION.equals(action))
			return handleMentionIntent(mMentionUser);
		else if (INTENT_ACTION_REPLY_MULTIPLE.equals(action)) {
			final String[] screenNames = intent.getStringArrayExtra(EXTRA_SCREEN_NAMES);
			final long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
			final long inReplyToUserId = intent.getLongExtra(EXTRA_IN_REPLY_TO_ID, -1);
			return handleReplyMultipleIntent(screenNames, accountId, inReplyToUserId);
		}
		// Unknown action or no intent extras
		return false;
	}

	private boolean handleMentionIntent(final ParcelableUser user) {
		if (user == null || user.id <= 0) return false;
		final String my_screen_name = getAccountScreenName(this, user.account_id);
		if (isEmpty(my_screen_name)) return false;
		mEditText.setText("@" + user.screen_name + " ");
		final int selection_end = mEditText.length();
		mEditText.setSelection(selection_end);
		mSendAccountIds = new long[] { user.account_id };
		return true;
	}

	private boolean handleQuoteIntent(final ParcelableStatus status) {
		if (status == null || status.id <= 0) return false;
		mEditText.setText(getQuoteStatus(this, status.user_screen_name, status.text_plain));
		mEditText.setSelection(0);
		mSendAccountIds = new long[] { status.account_id };
		return true;
	}

	private boolean handleReplyIntent(final ParcelableStatus status) {
		if (status == null || status.id <= 0) return false;
		final String myScreenName = getAccountScreenName(this, status.account_id);
		if (isEmpty(myScreenName)) return false;
		mEditText.append("@" + status.user_screen_name + " ");
		final int selectionStart = mEditText.length();
		if (!isEmpty(status.retweeted_by_screen_name)) {
			mEditText.append("@" + status.retweeted_by_screen_name + " ");
		}
		final Collection<String> mentions = mExtractor.extractMentionedScreennames(status.text_plain);
		for (final String mention : mentions) {
			if (mention.equalsIgnoreCase(status.user_screen_name) || mention.equalsIgnoreCase(myScreenName)
					|| mention.equalsIgnoreCase(status.retweeted_by_screen_name)) {
				continue;
			}
			mEditText.append("@" + mention + " ");
		}
		final int selectionEnd = mEditText.length();
		mEditText.setSelection(selectionStart, selectionEnd);
		mSendAccountIds = new long[] { status.account_id };
		return true;
	}

	private boolean handleReplyMultipleIntent(final String[] screenNames, final long accountId,
			final long inReplyToStatusId) {
		if (screenNames == null || screenNames.length == 0 || accountId <= 0) return false;
		final String myScreenName = getAccountScreenName(this, accountId);
		if (isEmpty(myScreenName)) return false;
		for (final String screenName : screenNames) {
			if (screenName.equalsIgnoreCase(myScreenName)) {
				continue;
			}
			mEditText.append("@" + screenName + " ");
		}
		mEditText.setSelection(mEditText.length());
		mSendAccountIds = new long[] { accountId };
		mInReplyToStatusId = inReplyToStatusId;
		return true;
	}

	private boolean hasMedia() {
		final String path = mMediaUri != null ? mMediaUri.getPath() : null;
		return path != null && new File(path).exists();
	}

	private boolean isSingleAccount() {
		return mAccountIds != null && mAccountIds.length == 1;
	}

	private boolean noReplyContent(final String text) {
		if (text == null) return true;
		final String action = getIntent().getAction();
		final boolean is_reply = INTENT_ACTION_REPLY.equals(action) || INTENT_ACTION_REPLY_MULTIPLE.equals(action);
		return is_reply && text.equals(mOriginalText);
	}

	private void pickImage() {
		final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		try {
			startActivityForResult(intent, REQUEST_PICK_IMAGE);
		} catch (final ActivityNotFoundException e) {
			showErrorMessage(this, null, e, false);
		}
	}

	private void reloadAttachedImageThumbnail() {
		final boolean has_media = hasMedia();
		mImageThumbnailPreview.setVisibility(has_media ? View.VISIBLE : View.GONE);
		mImageLoader.displayPreviewImage(mImageThumbnailPreview, has_media ? mMediaUri.toString() : null);
	}

	private void setCommonMenu(final Menu menu) {
		final boolean hasMedia = hasMedia();
		final int activatedColor = getUserThemeColor(this);
		final MenuItem itemAddImageSubmenu = menu.findItem(R.id.add_image_submenu);
		if (itemAddImageSubmenu != null) {
			final Drawable iconAddImage = itemAddImageSubmenu.getIcon();
			iconAddImage.mutate();
			if (hasMedia) {
				iconAddImage.setColorFilter(activatedColor, Mode.MULTIPLY);
			} else {
				iconAddImage.clearColorFilter();
			}
		}
		final MenuItem itemAddImage = menu.findItem(MENU_ADD_IMAGE);
		if (itemAddImage != null) {
			final Drawable iconAddImage = itemAddImage.getIcon().mutate();
			if (mMediaType == ATTACHED_IMAGE_TYPE_IMAGE) {
				iconAddImage.setColorFilter(activatedColor, Mode.MULTIPLY);
				itemAddImage.setTitle(R.string.remove_image);
			} else {
				iconAddImage.clearColorFilter();
				itemAddImage.setTitle(R.string.add_image);
			}
		}
		final MenuItem itemTakePhoto = menu.findItem(MENU_TAKE_PHOTO);
		if (itemTakePhoto != null) {
			final Drawable iconTakePhoto = itemTakePhoto.getIcon().mutate();
			if (mMediaType == ATTACHED_IMAGE_TYPE_PHOTO) {
				iconTakePhoto.setColorFilter(activatedColor, Mode.MULTIPLY);
				itemTakePhoto.setTitle(R.string.remove_photo);
			} else {
				iconTakePhoto.clearColorFilter();
				itemTakePhoto.setTitle(R.string.take_photo);
			}
		}
		final MenuItem itemAttachLocation = menu.findItem(MENU_ADD_LOCATION);
		if (itemAttachLocation != null) {
			final Drawable iconAttachLocation = itemAttachLocation.getIcon().mutate();
			final boolean attach_location = mPreferences.getBoolean(KEY_ATTACH_LOCATION, false);
			if (attach_location && getLocation()) {
				iconAttachLocation.setColorFilter(activatedColor, Mode.MULTIPLY);
				itemAttachLocation.setTitle(R.string.remove_location);
				itemAttachLocation.setChecked(true);
			} else {
				setProgressVisibility(false);
				mPreferences.edit().putBoolean(KEY_ATTACH_LOCATION, false).commit();
				iconAttachLocation.clearColorFilter();
				itemAttachLocation.setTitle(R.string.add_location);
				itemAttachLocation.setChecked(false);
			}
		}
		final MenuItem viewItem = menu.findItem(MENU_VIEW);
		if (viewItem != null) {
			viewItem.setVisible(mInReplyToStatus != null);
		}
		for (int i = 0, j = menu.size(); i < j; i++) {
			final MenuItem item = menu.getItem(i);
			if (item.getGroupId() == MENU_GROUP_IMAGE_EXTENSION) {
				item.setVisible(hasMedia);
				item.setEnabled(hasMedia);
			}
		}
		final MenuItem itemToggleSensitive = menu.findItem(MENU_TOGGLE_SENSITIVE);
		if (itemToggleSensitive != null) {
			itemToggleSensitive.setVisible(hasMedia);
			if (hasMedia) {
				final Drawable iconToggleSensitive = itemToggleSensitive.getIcon().mutate();
				if (mIsPossiblySensitive) {
					itemToggleSensitive.setTitle(R.string.remove_sensitive_mark);
					iconToggleSensitive.setColorFilter(activatedColor, Mode.MULTIPLY);
				} else {
					itemToggleSensitive.setTitle(R.string.mark_as_sensitive);
					iconToggleSensitive.clearColorFilter();
				}
			}
		}
		Utils.setMenuItemAvailability(menu, MENU_DELETE_SUBMENU, hasMedia);
	}

	private boolean setComposeTitle(final Intent intent) {
		final String action = intent.getAction();
		if (INTENT_ACTION_REPLY.equals(action)) {
			if (mInReplyToStatus == null) return false;
			final String display_name = getDisplayName(this, mInReplyToStatus.user_id, mInReplyToStatus.user_name,
					mInReplyToStatus.user_screen_name);
			setTitle(getString(R.string.reply_to, display_name));
		} else if (INTENT_ACTION_QUOTE.equals(action)) {
			if (mInReplyToStatus == null) return false;
			final String display_name = getDisplayName(this, mInReplyToStatus.user_id, mInReplyToStatus.user_name,
					mInReplyToStatus.user_screen_name);
			setTitle(getString(R.string.quote_user, display_name));
			mSubtitleView.setVisibility(mInReplyToStatus.user_is_protected
					&& mInReplyToStatus.account_id != mInReplyToStatus.user_id ? View.VISIBLE : View.GONE);
		} else if (INTENT_ACTION_EDIT_DRAFT.equals(action)) {
			if (mDraftItem == null) return false;
			setTitle(R.string.edit_draft);
		} else if (INTENT_ACTION_MENTION.equals(action)) {
			if (mMentionUser == null) return false;
			final String display_name = getDisplayName(this, mMentionUser.id, mMentionUser.name,
					mMentionUser.screen_name);
			setTitle(getString(R.string.mention_user, display_name));
		} else if (INTENT_ACTION_REPLY_MULTIPLE.equals(action)) {
			setTitle(R.string.reply);
		} else if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			setTitle(R.string.share);
		} else {
			setTitle(R.string.compose);
		}
		return true;
	}

	private void setMenu() {
		if (mBottomMenuBar == null || mActionMenuBar == null) return;
		final Menu bottomMenu = mBottomMenuBar.getMenu(), actionMenu = mActionMenuBar.getMenu();
		setCommonMenu(bottomMenu);
		setCommonMenu(actionMenu);
		mActionMenuBar.show();
		mBottomMenuBar.show();
	}

	private void setProgressVisibility(final boolean visible) {
		mProgress.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private void takePhoto() {
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			final File cache_dir = getExternalCacheDir();
			final File file = new File(cache_dir, "tmp_photo_" + System.currentTimeMillis());
			mTempPhotoUri = Uri.fromFile(file);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
			try {
				startActivityForResult(intent, REQUEST_TAKE_PHOTO);
			} catch (final ActivityNotFoundException e) {
				showErrorMessage(this, null, e, false);
			}
		}
	}

	private void updateAccountSelection() {
		if (mSendAccountIds == null) return;
		if (mShouldSaveAccounts) {
			final SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString(KEY_COMPOSE_ACCOUNTS, ArrayUtils.toString(mSendAccountIds, ',', false));
			editor.commit();
		}
		mAccountSelectorAdapter.clearAccountSelection();
		for (final long accountId : mSendAccountIds) {
			mAccountSelectorAdapter.setAccountSelected(accountId, true);
		}
		mColorIndicator.drawEnd(getAccountColors(this, mSendAccountIds));
	}

	private void updateStatus() {
		if (isFinishing()) return;
		final boolean hasMedia = hasMedia();
		final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
		final int tweetLength = mValidator.getTweetLength(text);
		if (!mTweetShortenerUsed && tweetLength > Validator.MAX_TWEET_LENGTH) {
			mEditText.setError(getString(R.string.error_message_status_too_long));
			final int text_length = mEditText.length();
			mEditText.setSelection(text_length - (tweetLength - Validator.MAX_TWEET_LENGTH), text_length);
			return;
		} else if (!hasMedia && (isEmpty(text) || noReplyContent(text))) {
			mEditText.setError(getString(R.string.error_message_no_content));
			return;
		}
		final boolean attach_location = mPreferences.getBoolean(KEY_ATTACH_LOCATION, false);
		if (mRecentLocation == null && attach_location) {
			final Location location;
			if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			} else {
				location = null;
			}
			mRecentLocation = location != null ? new ParcelableLocation(location) : null;
		}
		final boolean isQuote = INTENT_ACTION_QUOTE.equals(getIntent().getAction());
		final ParcelableLocation statusLocation = attach_location ? mRecentLocation : null;
		final boolean linkToQuotedTweet = mPreferences.getBoolean(KEY_LINK_TO_QUOTED_TWEET, true);
		final long inReplyToStatusId = !isQuote || linkToQuotedTweet ? mInReplyToStatusId : -1;
		final boolean isPossiblySensitive = hasMedia && mIsPossiblySensitive;
		mTwitterWrapper.updateStatusAsync(mSendAccountIds, text, statusLocation, mMediaUri, mMediaType,
				inReplyToStatusId, isPossiblySensitive);
		if (mPreferences.getBoolean(KEY_NO_CLOSE_AFTER_TWEET_SENT, false)
				&& (mInReplyToStatus == null || mInReplyToStatusId <= 0)) {
			mMediaType = ATTACHED_IMAGE_TYPE_NONE;
			mIsPossiblySensitive = false;
			mShouldSaveAccounts = true;
			mMediaUri = null;
			mTempPhotoUri = null;
			mInReplyToStatus = null;
			mMentionUser = null;
			mDraftItem = null;
			mInReplyToStatusId = -1;
			mOriginalText = null;
			final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
			setIntent(intent);
			setComposeTitle(intent);
			handleIntent(intent);
			reloadAttachedImageThumbnail();
			mEditText.setText(null);
			setMenu();
			updateTextCount();
		} else {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

	private void updateTextCount() {
		final boolean bottomSendButton = mPreferences.getBoolean(KEY_BOTTOM_SEND_BUTTON, false);
		final View sendItemView = bottomSendButton ? mBottomSendView : mSendView;
		if (sendItemView != null && mEditText != null) {
			final ComposeTextCountView sendTextCountView = (ComposeTextCountView) sendItemView
					.findViewById(R.id.send_text_count);
			sendItemView.setOnClickListener(this);
			final String text_orig = mEditText != null ? parseString(mEditText.getText()) : null;
			final String text = hasMedia() && text_orig != null ? mImageUploaderUsed ? getImageUploadStatus(this,
					FAKE_IMAGE_LINK, text_orig) : text_orig + " " + FAKE_IMAGE_LINK : text_orig;
			final int validated_count = text != null ? mValidator.getTweetLength(text) : 0;
			sendTextCountView.setTextCount(validated_count);
		}
	}

	public static class UnsavedTweetDialogFragment extends BaseSupportDialogFragment implements
			DialogInterface.OnClickListener {

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			final Activity activity = getActivity();
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					if (activity instanceof ComposeActivity) {
						((ComposeActivity) activity).saveToDrafts();
					}
					activity.finish();
					break;
				}
				case DialogInterface.BUTTON_NEGATIVE: {
					if (activity instanceof ComposeActivity) {
						new DiscardTweetTask((ComposeActivity) activity).execute();
					} else {
						activity.finish();
					}
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
			final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
			builder.setMessage(R.string.unsaved_status);
			builder.setPositiveButton(R.string.save, this);
			builder.setNegativeButton(R.string.discard, this);
			return builder.create();
		}
	}

	public static class ViewStatusDialogFragment extends BaseSupportDialogFragment {

		private StatusViewHolder mHolder;

		public ViewStatusDialogFragment() {
			setStyle(STYLE_NO_TITLE, 0);
		}

		@Override
		public void onActivityCreated(final Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			final Bundle args = getArguments();
			if (args == null || args.getParcelable(EXTRA_STATUS) == null) {
				dismiss();
				return;
			}
			final TwidereApplication application = getApplication();
			final ImageLoaderWrapper loader = application.getImageLoaderWrapper();
			final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
			final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
			mHolder.setShowAsGap(false);
			mHolder.setAccountColorEnabled(true);
			mHolder.setTextSize(prefs.getInt(KEY_TEXT_SIZE, getDefaultTextSize(getActivity())));
			((View) mHolder.content).setPadding(0, 0, 0, 0);
			mHolder.content.setItemBackground(null);
			mHolder.content.setItemSelector(null);
			mHolder.text.setText(status.text_unescaped);
			mHolder.name.setText(status.user_name);
			mHolder.screen_name.setText("@" + status.user_screen_name);
			mHolder.screen_name.setVisibility(View.VISIBLE);

			final String retweeted_by_name = status.retweeted_by_name;
			final String retweeted_by_screen_name = status.retweeted_by_screen_name;

			final boolean is_my_status = status.account_id == status.user_id;
			mHolder.setUserColor(getUserColor(getActivity(), status.user_id, true));
			mHolder.setHighlightColor(getStatusBackground(false, status.is_favorite, status.is_retweet));

			mHolder.setIsMyStatus(is_my_status && !prefs.getBoolean(KEY_INDICATE_MY_STATUS, true));

			mHolder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getUserTypeIconRes(status.user_is_verified, status.user_is_protected), 0);
			mHolder.time.setTime(status.timestamp);
			final int type_icon = getStatusTypeIconRes(status.is_favorite, isValidLocation(status.location),
					status.has_media, status.is_possibly_sensitive);
			mHolder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0, type_icon, 0);
			mHolder.reply_retweet_status
					.setVisibility(status.in_reply_to_status_id != -1 || status.is_retweet ? View.VISIBLE : View.GONE);
			if (status.is_retweet && !TextUtils.isEmpty(retweeted_by_name)
					&& !TextUtils.isEmpty(retweeted_by_screen_name)) {
				if (!prefs.getBoolean(KEY_NAME_FIRST, true)) {
					mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(
							R.string.retweeted_by_with_count, retweeted_by_screen_name, status.retweet_count - 1)
							: getString(R.string.retweeted_by, retweeted_by_screen_name));
				} else {
					mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(
							R.string.retweeted_by_with_count, retweeted_by_name, status.retweet_count - 1) : getString(
							R.string.retweeted_by, retweeted_by_name));
				}
				mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(
						R.string.retweeted_by_with_count, retweeted_by_name, status.retweet_count - 1) : getString(
						R.string.retweeted_by, retweeted_by_name));
				mHolder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet,
						0, 0, 0);
			} else if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
				mHolder.reply_retweet_status.setText(getString(R.string.in_reply_to, status.in_reply_to_screen_name));
				mHolder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.ic_indicator_conversation, 0, 0, 0);
			}
			if (prefs.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true)) {
				loader.displayProfileImage(mHolder.my_profile_image, status.user_profile_image_url);
				loader.displayProfileImage(mHolder.profile_image, status.user_profile_image_url);
			} else {
				mHolder.profile_image.setVisibility(View.GONE);
				mHolder.my_profile_image.setVisibility(View.GONE);
			}
			mHolder.image_preview_container.setVisibility(View.GONE);
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
			final ScrollView view = (ScrollView) inflater.inflate(R.layout.dialog_scrollable_status, parent, false);
			mHolder = new StatusViewHolder(view.getChildAt(0));
			return view;
		}

	}

	private static class AccountSelectorAdapter extends BaseArrayAdapter<Account> {

		private final LongSparseArray<Boolean> mAccountSelectStates = new LongSparseArray<Boolean>();

		public AccountSelectorAdapter(final Context context) {
			super(context, R.layout.compose_account_selector_item, Account.getAccounts(context, false));
		}

		public void clearAccountSelection() {
			mAccountSelectStates.clear();
			notifyDataSetChanged();
		}

		public long[] getSelectedAccountIds() {
			final ArrayList<Long> list = new ArrayList<Long>();
			for (int i = 0, j = getCount(); i < j; i++) {
				final Account account = getItem(i);
				if (mAccountSelectStates.get(account.account_id, false)) {
					list.add(account.account_id);
				}
			}
			return ArrayUtils.fromList(list);
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final Account account = getItem(position);
			final ImageLoaderWrapper loader = getImageLoader();
			final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
			loader.displayProfileImage(icon, account.profile_image_url);
			view.setActivated(mAccountSelectStates.get(account.account_id, false));
			return view;
		}

		public void setAccountSelected(final long accountId, final boolean selected) {
			mAccountSelectStates.put(accountId, selected);
			notifyDataSetChanged();
		}

	}

	private static class CopyImageTask extends AsyncTask<Void, Void, Boolean> {

		private final ComposeActivity activity;
		private final int image_type;
		private final Uri old, src, dst;

		CopyImageTask(final ComposeActivity activity, final Uri old, final Uri src, final Uri dst, final int image_type) {
			this.activity = activity;
			this.old = old;
			this.src = src;
			this.dst = dst;
			this.image_type = image_type;
		}

		@Override
		protected Boolean doInBackground(final Void... params) {
			try {
				final ContentResolver resolver = activity.getContentResolver();
				final InputStream is = resolver.openInputStream(src);
				final OutputStream os = resolver.openOutputStream(dst);
				copyStream(is, os);
				os.close();
				if (old != null && !old.equals(dst) && ContentResolver.SCHEME_FILE.equals(old.getScheme())) {
					new File(old.getPath()).delete();
				}
				if (ContentResolver.SCHEME_FILE.equals(src.getScheme()) && image_type == ATTACHED_IMAGE_TYPE_PHOTO) {
					new File(src.getPath()).delete();
				}
			} catch (final Exception e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean result) {
			activity.setProgressVisibility(false);
			activity.mMediaUri = dst;
			activity.mMediaType = image_type;
			activity.reloadAttachedImageThumbnail();
			activity.setMenu();
			activity.updateTextCount();
			if (!result) {
				Crouton.showText(activity, R.string.error_occurred, CroutonStyle.ALERT);
			}
		}

		@Override
		protected void onPreExecute() {
			activity.setProgressVisibility(true);
		}
	}

	private static class DeleteImageTask extends AsyncTask<Uri, Void, Boolean> {

		final ComposeActivity activity;

		DeleteImageTask(final ComposeActivity activity) {
			this.activity = activity;
		}

		@Override
		protected Boolean doInBackground(final Uri... params) {
			if (params == null) return false;
			try {
				final Uri uri = activity.mMediaUri;
				if (uri != null && ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
					new File(uri.getPath()).delete();
				}
				for (final Uri target : params) {
					if (target == null) {
						continue;
					}
					if (ContentResolver.SCHEME_FILE.equals(target.getScheme())) {
						new File(target.getPath()).delete();
					}
				}
			} catch (final Exception e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean result) {
			activity.setProgressVisibility(false);
			activity.mMediaUri = null;
			activity.mMediaType = ATTACHED_IMAGE_TYPE_NONE;
			activity.mIsPossiblySensitive = false;
			activity.setMenu();
			activity.reloadAttachedImageThumbnail();
			if (!result) {
				Crouton.showText(activity, R.string.error_occurred, CroutonStyle.ALERT);
			}
		}

		@Override
		protected void onPreExecute() {
			activity.setProgressVisibility(true);
		}
	}

	private static class DiscardTweetTask extends AsyncTask<Void, Void, Void> {

		final ComposeActivity activity;

		DiscardTweetTask(final ComposeActivity activity) {
			this.activity = activity;
		}

		@Override
		protected Void doInBackground(final Void... params) {
			final Uri uri = activity.mMediaUri;
			try {
				if (uri == null) return null;
				if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
					new File(uri.getPath()).delete();
				}
			} catch (final Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			activity.setProgressVisibility(false);
			activity.finish();
		}

		@Override
		protected void onPreExecute() {
			activity.setProgressVisibility(true);
		}
	}
}
