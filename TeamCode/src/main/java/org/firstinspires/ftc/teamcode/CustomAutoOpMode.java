package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.drive.MyTankDrive;

@Autonomous(name = "CustomAutoOpMode")
public class CustomAutoOpMode extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize the custom tank drive system
        MyTankDrive drive = new MyTankDrive(hardwareMap);

        // Define start position. Update values according to your start position on the field
        Pose2d startPose = new Pose2d();

        // Define a trajectory to follow. Update waypoints and values as per your strategy
        Trajectory trajectory = drive.trajectoryBuilder(startPose)
                .forward(40) // Example waypoint
                .build();

        waitForStart();

        if (isStopRequested()) return;

        // Follow the defined trajectory
        drive.followTrajectory(trajectory);

        // Add additional actions or movements as per your strategy

        // Example: drive.turn(Math.toRadians(90)); // 90 degree turn
    }
}
