package org.firstinspires.ftc.teamcode.mechanisms;

import android.util.Log;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.limelightvision.LLResult;

@Autonomous()
public class ColorBallFinder extends OpMode {

    private Limelight3A limelight;
    private static final String TAG = "LL_DEBUG";

    @Override
    public void init() {
        Log.d(TAG, "--- INIT STARTED ---");
        limelight = hardwareMap.get(Limelight3A.class, "mywebcam");

        // Force Pipeline 2 (Color/Contour Pipeline)
        Log.d(TAG, "Switching to Pipeline 3...");
        limelight.pipelineSwitch(3);

        Log.d(TAG, "Starting Limelight...");
        limelight.start();
    }

    @Override
    public void init_loop() {
        LLResult result = limelight.getLatestResult();
        boolean connected = limelight.isConnected();
        int pipeline = limelight.getStatus().getPipelineIndex();

        Log.d(TAG, String.format("Init Loop - Connected: %b, Pipeline: %d", connected, pipeline));

        if (result != null) {
            // result.isValid() replaces checking tag size for generic contours
            Log.d(TAG, "Init Loop - Frame received! Target Visible: " + result.isValid());
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
            long staleness = result.getStaleness();
            Log.d(TAG, "Loop - Data received. Staleness: " + staleness + "ms");

            // For contours, result.isValid() checks if the crosshair has a locked target (tv)
            if (result.isValid()) {
                // For standard contours, grab tx and ty directly from the main result object
                double tx = result.getTx();
                double ty = result.getTy();
                double ta = result.getTa(); // Target Area (% of image)

                Log.i(TAG, String.format("SUCCESS: Target Locked | tx: %.2f | ty: %.2f | Area: %.2f%%", tx, ty, ta));

                telemetry.addData("Target Found", "YES");
                telemetry.addData("tx (Horizontal)", tx);
                telemetry.addData("ty (Vertical)", ty);
                telemetry.addData("ta (Area)", ta);
            } else {
                Log.w(TAG, "Loop - Frame valid, but zero color contours match your threshold criteria.");
                telemetry.addData("Target Found", "NO");
                telemetry.addLine("Adjust your HSV thresholds in the Hardware Manager!");
            }
        }

        telemetry.update();
    }
}