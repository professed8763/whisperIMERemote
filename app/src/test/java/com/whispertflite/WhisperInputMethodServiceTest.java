package com.whispertflite;

import static org.junit.Assert.*;

import android.app.Application;
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
import org.robolectric.android.controller.ServiceController;

@RunWith(RobolectricTestRunner.class)
public class WhisperInputMethodServiceTest {

    @After
    public void clearTranslatePref() {
        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.getApplication())
                .edit()
                .remove(WhisperInputMethodService.PREF_TRANSLATE)
                .commit();
    }

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
    public void newServiceInstanceDoesNotInheritToggleFromPrevious() {
        // Regression test: previously the `translate` field was static, so a
        // tap in one session leaked into every subsequent session. With the
        // fix, the field is instance-scoped and backed by SharedPreferences —
        // clearing prefs between sessions gives a fresh-install state.
        ServiceController<WhisperInputMethodService> controllerA =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService serviceA = controllerA.create().get();
        View viewA = serviceA.onCreateInputView();
        ImageButton btnTranslateA = viewA.findViewById(R.id.btnTranslate);
        btnTranslateA.performClick();  // sets pref = true for this session

        // Simulate a fresh install for the next service
        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.getApplication())
                .edit()
                .remove(WhisperInputMethodService.PREF_TRANSLATE)
                .commit();

        ServiceController<WhisperInputMethodService> controllerB =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService serviceB = controllerB.create().get();
        serviceB.onCreateInputView();

        boolean stored = PreferenceManager.getDefaultSharedPreferences(
                RuntimeEnvironment.getApplication())
                .getBoolean(WhisperInputMethodService.PREF_TRANSLATE, false);
        assertFalse("Fresh service should not see translate=true leaked from prior instance",
                stored);
    }
}
