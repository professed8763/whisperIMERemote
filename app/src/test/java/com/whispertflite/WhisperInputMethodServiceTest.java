package com.whispertflite;

import static org.junit.Assert.*;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.shadows.ShadowApplication;

@RunWith(RobolectricTestRunner.class)
public class WhisperInputMethodServiceTest {

    @Test
    public void serviceCanBeCreated() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();
        assertNotNull("Service should be created", service);
    }

    @Test
    public void onCreateInputViewInflatesLayout() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();
        assertNotNull("Input view should not be null", inputView);
    }

    @Test
    public void inputViewContainsAllRequiredButtons() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();

        assertNotNull("Record button should exist", inputView.findViewById(R.id.btnRecord));
        assertNotNull("Keyboard button should exist", inputView.findViewById(R.id.btnKeyboard));
        assertNotNull("Translate button should exist", inputView.findViewById(R.id.btnTranslate));
        assertNotNull("Mode auto button should exist", inputView.findViewById(R.id.btnModeAuto));
        assertNotNull("Enter button should exist", inputView.findViewById(R.id.btnEnter));
        assertNotNull("Delete button should exist", inputView.findViewById(R.id.btnDel));
    }

    @Test
    public void inputViewContainsProgressBarAndStatus() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();

        ProgressBar progressBar = inputView.findViewById(R.id.processing_bar);
        assertNotNull("Progress bar should exist", progressBar);

        TextView tvStatus = inputView.findViewById(R.id.tv_status);
        assertNotNull("Status text view should exist", tvStatus);
    }

    @Test
    public void modeAutoDefaultsToOff() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();

        LinearLayout layoutButtons = inputView.findViewById(R.id.layout_buttons);
        assertEquals("Buttons layout should be visible when modeAuto is off",
                View.VISIBLE, layoutButtons.getVisibility());
    }

    @Test
    public void serviceOnDestroyDoesNotCrash() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        // Should not throw
        service.onDestroy();
    }

    @Test
    public void statusShowsPermissionWarningWhenNotGranted() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();
        TextView tvStatus = inputView.findViewById(R.id.tv_status);

        // Without RECORD_AUDIO permission, checkRecordPermission() makes status visible
        assertEquals("Status text should be visible when permission not granted",
                View.VISIBLE, tvStatus.getVisibility());
    }

    @After
    public void clearTranslatePref() {
        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.getApplication())
                .edit()
                .remove(WhisperInputMethodService.PREF_TRANSLATE)
                .commit();
    }

    @Test
    public void translatePrefDefaultsToFalseOnFreshInstall() {
        Application app = RuntimeEnvironment.getApplication();
        boolean stored = PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean(WhisperInputMethodService.PREF_TRANSLATE, false);
        assertFalse("Translate preference should default to false", stored);
    }

    @Test
    public void togglingTranslateButtonPersistsTrueToPreferences() {
        Application app = RuntimeEnvironment.getApplication();
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();
        ImageButton btnTranslate = inputView.findViewById(R.id.btnTranslate);

        btnTranslate.performClick();

        boolean stored = PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean(WhisperInputMethodService.PREF_TRANSLATE, false);
        assertTrue("Tapping translate should persist true in SharedPreferences", stored);
    }

    @Test
    public void togglingTranslateButtonTwicePersistsFalseAgain() {
        Application app = RuntimeEnvironment.getApplication();
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();
        ImageButton btnTranslate = inputView.findViewById(R.id.btnTranslate);

        btnTranslate.performClick();
        btnTranslate.performClick();

        boolean stored = PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean(WhisperInputMethodService.PREF_TRANSLATE, true);
        assertFalse("Tapping translate twice should persist false in SharedPreferences", stored);
    }

    @Test
    public void translateBannerShownWhenPreferenceIsTrueAndPermissionGranted() {
        Application app = RuntimeEnvironment.getApplication();
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putBoolean(WhisperInputMethodService.PREF_TRANSLATE, true)
                .commit();
        ShadowApplication shadowApp = Shadows.shadowOf(app);
        shadowApp.grantPermissions(Manifest.permission.RECORD_AUDIO);

        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();
        TextView tvStatus = inputView.findViewById(R.id.tv_status);

        assertEquals("Banner should be visible when translate is active",
                View.VISIBLE, tvStatus.getVisibility());
        assertEquals("Banner should show translate-active text",
                app.getString(R.string.translate_active_banner),
                tvStatus.getText().toString());
    }

    @Test
    public void translateBannerHiddenWhenPreferenceIsFalseAndPermissionGranted() {
        Application app = RuntimeEnvironment.getApplication();
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putBoolean(WhisperInputMethodService.PREF_TRANSLATE, false)
                .commit();
        ShadowApplication shadowApp = Shadows.shadowOf(app);
        shadowApp.grantPermissions(Manifest.permission.RECORD_AUDIO);

        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();
        TextView tvStatus = inputView.findViewById(R.id.tv_status);

        assertEquals("Banner should be hidden when translate is off",
                View.GONE, tvStatus.getVisibility());
    }

    @Test
    public void permissionWarningTakesPriorityOverTranslateBanner() {
        Application app = RuntimeEnvironment.getApplication();
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putBoolean(WhisperInputMethodService.PREF_TRANSLATE, true)
                .commit();
        // Permission NOT granted

        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();
        TextView tvStatus = inputView.findViewById(R.id.tv_status);

        assertEquals("Status should show permission warning, not translate banner",
                app.getString(R.string.need_record_audio_permission),
                tvStatus.getText().toString());
    }

    @Test
    public void togglingToggleOnThenOffUpdatesBanner() {
        Application app = RuntimeEnvironment.getApplication();
        ShadowApplication shadowApp = Shadows.shadowOf(app);
        shadowApp.grantPermissions(Manifest.permission.RECORD_AUDIO);

        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();
        ImageButton btnTranslate = inputView.findViewById(R.id.btnTranslate);
        TextView tvStatus = inputView.findViewById(R.id.tv_status);

        btnTranslate.performClick();
        assertEquals("Banner should appear after enabling translate",
                View.VISIBLE, tvStatus.getVisibility());
        assertEquals(app.getString(R.string.translate_active_banner),
                tvStatus.getText().toString());

        btnTranslate.performClick();
        assertEquals("Banner should hide after disabling translate",
                View.GONE, tvStatus.getVisibility());
    }

    @Test
    public void translateStateNotSharedBetweenServiceInstances() {
        // Regression test: previously the `translate` field was static, so any
        // tap would persist across Android process lifetime. With the fix, the
        // field is instance-scoped and backed by SharedPreferences — two fresh
        // service instances with no prior prefs should both start with
        // translate = false even after one toggles it (because prefs were
        // cleared via @After).
        ServiceController<WhisperInputMethodService> controllerA =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService serviceA = controllerA.create().get();
        View viewA = serviceA.onCreateInputView();
        ImageButton btnTranslateA = viewA.findViewById(R.id.btnTranslate);
        btnTranslateA.performClick();  // sets pref = true for this session

        // Clear the pref to simulate "fresh install" for a new service
        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.getApplication())
                .edit()
                .remove(WhisperInputMethodService.PREF_TRANSLATE)
                .commit();

        ServiceController<WhisperInputMethodService> controllerB =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService serviceB = controllerB.create().get();
        View viewB = serviceB.onCreateInputView();
        TextView tvStatusB = viewB.findViewById(R.id.tv_status);

        // If translate leaked statically, the banner would show. With the fix,
        // service B starts fresh with translate = false.
        ShadowApplication shadowApp = Shadows.shadowOf(RuntimeEnvironment.getApplication());
        shadowApp.grantPermissions(Manifest.permission.RECORD_AUDIO);
        // Even with permission granted, banner text should not be the translate banner.
        String translateBanner = RuntimeEnvironment.getApplication()
                .getString(R.string.translate_active_banner);
        assertNotEquals("Fresh service with cleared prefs should not show translate banner",
                translateBanner, tvStatusB.getText().toString());
    }
}
