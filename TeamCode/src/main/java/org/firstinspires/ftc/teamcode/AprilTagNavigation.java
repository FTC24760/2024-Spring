package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

@TeleOp(name = "AprilTag Navigation", group = "Autonomous")
public class AprilTagNavigation extends LinearOpMode {

    private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera

    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;

    private DcMotor leftDrive, rightDrive;

    private static final double WHEEL_DIAMETER_INCHES = 3.5; // Updated wheel diameter
    private static final double TICKS_PER_REVOLUTION = 560; // Updated for REV HD Hex motor with 20:1 gearbox
    private static final double DRIVE_GEAR_REDUCTION = 1.0; // Assuming no additional gear reduction
    private static final double WHEEL_CIRCUMFERENCE_INCHES = WHEEL_DIAMETER_INCHES * Math.PI;
    private static final double TICKS_PER_INCH = (TICKS_PER_REVOLUTION * DRIVE_GEAR_REDUCTION) / WHEEL_CIRCUMFERENCE_INCHES;

    @Override
    public void runOpMode() {
        initAprilTag();
        initHardware();

        waitForStart();

        while (opModeIsActive()) {
            telemetryAprilTag();

            List<AprilTagDetection> detections = aprilTag.getDetections();
            if (!detections.isEmpty()) {
                AprilTagDetection tag = detections.get(0);
                navigateToTag(tag);
            }

            telemetry.update();
        }

        visionPortal.close();
    }

    private void initAprilTag() {
        aprilTag = AprilTagProcessor.easyCreateWithDefaults();
        if (USE_WEBCAM) {
            visionPortal = VisionPortal.easyCreateWithDefaults(hardwareMap.get(WebcamName.class, "Webcam 1"), aprilTag);
        } else {
            visionPortal = VisionPortal.easyCreateWithDefaults(BuiltinCameraDirection.BACK, aprilTag);
        }
    }

    private void initHardware() {
        leftDrive = hardwareMap.get(DcMotor.class, "leftDrive");
        rightDrive = hardwareMap.get(DcMotor.class, "rightDrive");

        leftDrive.setDirection(DcMotorSimple.Direction.REVERSE);
        rightDrive.setDirection(DcMotorSimple.Direction.FORWARD);

        leftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private void telemetryAprilTag() {
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        telemetry.addData("# AprilTags Detected", currentDetections.size());

        for (AprilTagDetection detection : currentDetections) {
            telemetry.addLine(String.format("ID %d: x=%6.1f y=%6.1f z=%6.1f", detection.id, detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z));
        }
    }

    private void navigateToTag(AprilTagDetection tag) {
        double x = tag.ftcPose.x;
        double y = tag.ftcPose.y;

        // Simple navigation: drive forward if the tag is ahead, turn if it's to the side
        if (Math.abs(x) < 6) { // 6 inches threshold for straight movement
            driveStraight(0.5, y); // Adjust speed and direction based on y value
        } else {
            turnToTag(x); // Turn towards the tag
        }
    }

    private void driveStraight(double speed, double inches) {
        int newLeftTarget = leftDrive.getCurrentPosition() + (int) (inches * TICKS_PER_INCH);
        int newRightTarget = rightDrive.getCurrentPosition() + (int) (inches * TICKS_PER_INCH);

        leftDrive.setTargetPosition(newLeftTarget);
        rightDrive.setTargetPosition(newRightTarget);

        leftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        leftDrive.setPower(Math.abs(speed));
        rightDrive.setPower(Math.abs(speed));

        while (opModeIsActive() && (leftDrive.isBusy() && rightDrive.isBusy())) {
            // Wait for the motors to reach the target
        }

        // Stop all motion
        leftDrive.setPower(0);
        rightDrive.setPower(0);

        leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private void turnToTag(double x) {
        double turnSpeed = 0.5;
        if (x > 0) {
            // Turn right
            leftDrive.setPower(turnSpeed);
            rightDrive.setPower(-turnSpeed);
        } else {
            // Turn left
            leftDrive.setPower(-turnSpeed);
            rightDrive.setPower(turnSpeed);
        }

        sleep(500); // Simple delay for turning; you might want to refine this

        // Stop the motors
        leftDrive.setPower(0);
        rightDrive.setPower(0);
    }
}
