package org.firstinspires.ftc.teamcode.mechanisms;

import android.util.Log;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.limelightvision.LLResult;
import java.util.List;

@Autonomous()
public class SimpleOpenCV extends OpMode {
    private Limelight3A limelight;
    // TAG to easily filter in Android Studio Logcat (Filter by "LL_DEBUG")
    private static final String TAG = "LL_DEBUG";

    @Override
    public void init() {
        Log.d(TAG, "--- INIT STARTED ---");
        limelight = hardwareMap.get(Limelight3A.class, "mywebcam");

        Log.d(TAG, "Switching to Pipeline 1...");
        limelight.pipelineSwitch(1);

        Log.d(TAG, "Starting Limelight...");
        limelight.start();
    }

    @Override
    public void init_loop() {
        LLResult result = limelight.getLatestResult();
        boolean connected = limelight.isConnected();
        int pipeline = limelight.getStatus().getPipelineIndex();

        // Log general connection status during initialization
        Log.d(TAG, String.format("Init Loop - Connected: %b, Pipeline: %d", connected, pipeline));

        if (result != null) {
            int tagCount = result.getFiducialResults().size();
            Log.d(TAG, "Init Loop - Frame received! AprilTag Count: " + tagCount);
        } else {
            Log.w(TAG, "Init Loop - LLResult is NULL");
        }

        telemetry.addData("Connected", connected);
        telemetry.addData("Pipeline Index", pipeline);
        telemetry.update();
    }

    @Override
    public void start() {
        Log.d(TAG, "--- OPMODE STARTED ---");
    }

    @Override
    public void loop() {
        LLResult result = limelight.getLatestResult();

        if (result == null) {
            Log.w(TAG, "Loop - LLResult is NULL (No data from camera)");
            telemetry.addLine("No result");
        } else {
            // Check if the results are stale (old data)
            long staleness = result.getStaleness();
            Log.d(TAG, "Loop - Data received. Staleness: " + staleness + "ms");

            List<LLResultTypes.FiducialResult> tags = result.getFiducialResults();
            Log.d(TAG, "Loop - Detected Fiducial Count: " + tags.size());

            if (!tags.isEmpty()) {
                // Log individual details for the primary tracked tag
                LLResultTypes.FiducialResult tag = tags.get(0);
                Log.i(TAG, String.format("SUCCESS: Tag ID: %d | X Deg: %.2f | Y Deg: %.2f",
                        tag.getFiducialId(),
                        tag.getTargetXDegrees(),
                        tag.getTargetYDegrees()));

                telemetry.addData("Tag ID", tag.getFiducialId());
                telemetry.addData("tx", tag.getTargetXDegrees());
                telemetry.addData("ty", tag.getTargetYDegrees());
            } else {
                Log.w(TAG, "Loop - Frame valid, but ZERO AprilTags match current pipeline criteria.");
                telemetry.addLine("No tags detected in result");
            }
        }

        telemetry.update();
    }
}
