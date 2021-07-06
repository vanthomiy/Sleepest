package com.doitstudio.sleepest_master.sleepapi;

        import android.content.Context;
        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ProgressBar;
        import android.widget.TextView;

        import com.google.android.gms.location.DetectedActivity;

        import java.util.ArrayList;
        import java.util.HashMap;

/**
 * Adapter that is backed by an array of {@code DetectedActivity} objects. Finds UI elements in the
 * detected_activity layout and populates each element with data from a DetectedActivity
 * object.
 */
class DetectedActivitiesAdapter extends ArrayAdapter<DetectedActivity> {

    DetectedActivitiesAdapter(Context context,
                              ArrayList<DetectedActivity> detectedActivities) {
        super(context, 0, detectedActivities);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        DetectedActivity detectedActivity = getItem(position);


        // Populate widgets with values.
        if (detectedActivity != null) {

        }
        return view;
    }

    /**
     * Process list of recently detected activities and updates the list of {@code DetectedActivity}
     * objects backing this adapter.
     *
     * @param detectedActivities the freshly detected activities
     */
    void updateActivities(ArrayList<DetectedActivity> detectedActivities) {
        HashMap<Integer, Integer> detectedActivitiesMap = new HashMap<>();
        for (DetectedActivity activity : detectedActivities) {
            detectedActivitiesMap.put(activity.getType(), activity.getConfidence());
        }
        // Every time we detect new activities, we want to reset the confidence level of ALL
        // activities that we monitor. Since we cannot directly change the confidence
        // of a DetectedActivity, we use a temporary list of DetectedActivity objects. If an
        // activity was freshly detected, we use its confidence level. Otherwise, we set the
        // confidence level to zero.
        ArrayList<DetectedActivity> tempList = new ArrayList<>();
        for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
            int confidence = detectedActivitiesMap.containsKey(Constants.MONITORED_ACTIVITIES[i]) ?
                    detectedActivitiesMap.get(Constants.MONITORED_ACTIVITIES[i]) : 0;

            tempList.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i],
                    confidence));
        }

        // Remove all items.
        this.clear();

        // Adding the new list items notifies attached observers that the underlying data has
        // changed and views reflecting the data should refresh.
        for (DetectedActivity detectedActivity: tempList) {
            this.add(detectedActivity);
        }
    }
}