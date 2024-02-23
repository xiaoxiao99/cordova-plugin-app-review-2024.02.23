package by.chemerisuk.cordova;

import static com.google.android.gms.tasks.Tasks.await;
import static by.chemerisuk.cordova.support.ExecutionThread.WORKER;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.json.JSONException;

import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;


public class AppReviewPlugin extends ReflectiveCordovaPlugin {
    @CordovaMethod(WORKER)
    private void requestReview(final CallbackContext callbackContext) {
        final Activity activity = cordova.getActivity();
        final ReviewManager manager = ReviewManagerFactory.create(activity);

        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(@NonNull Task<ReviewInfo> task) {
                if (task.isSuccessful()) {
                    // We got the ReviewInfo object
                    ReviewInfo reviewInfo = task.getResult();
                    Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                    flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // The in-app review dialog is shown
                                callbackContext.success();
                            } else {
                                // There was some problem, continue regardless of the result.
                                callbackContext.success();
                            }
                        }
                    });
                } else {
                    // There was some problem with getting the review info.
                    callbackContext.error(task.getException().getMessage());
                }
            }
        });
    }

    @CordovaMethod
    protected void openStoreScreen(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        String packageName = args.getString(0);
        if (packageName == null) {
            packageName = cordova.getActivity().getPackageName();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        cordova.getActivity().startActivity(intent);
        callbackContext.success();
    }
}
