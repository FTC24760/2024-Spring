package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name="CenterStage Bot 24760")

public class FTCCode extends OpMode
{
    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor leftDrive = null;
    private DcMotor rightDrive = null;
    private DcMotor armLeft = null;
    private DcMotor armRight = null;
    private Servo gripper = null;
    private Servo wrist = null;
    private Servo servo1 = null;
    private Servo servo2 = null;

    private boolean manualMode = false;
    private double armSetpoint = 0.0;
    private double wristSetpoint = 0.0;

    private final double armManualDeadband = 0.03;

    private final double gripperClosedPosition = 1.0;
    private final double gripperOpenPosition = 0.0;
    private final double wristUpPosition = 1.0;
    private final double wristDownPosition = 0.0;

    private final int armHomePosition = -60;
    private final int armIntakePosition = 10;
    private final int armScorePosition = 65;
    private final int armShutdownThreshold = 5;
    private final double targetIntakePosition = 1.0;
    private double curWrist = 0.0;

    private boolean speedToggle = false;
    private boolean spinToggle = false;
    private boolean reverseSpinToggle = false;

    long spinPress = 0;
    long speedPress = 0;
    long reversePress = 0;

    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {
        telemetry.addData("Status", "Initialized");

        leftDrive  = hardwareMap.get(DcMotor.class, "leftDrive");
        rightDrive = hardwareMap.get(DcMotor.class, "rightDrive");
        armLeft  = hardwareMap.get(DcMotor.class, "armLeft");
        armRight = hardwareMap.get(DcMotor.class, "armRight");
        gripper = hardwareMap.get(Servo.class, "gripper");
        wrist = hardwareMap.get(Servo.class, "wrist");
        servo1 = hardwareMap.get(Servo.class, "servo1");
        servo2 = hardwareMap.get(Servo.class, "servo2");

        leftDrive.setDirection(DcMotor.Direction.FORWARD);
        rightDrive.setDirection(DcMotor.Direction.REVERSE);
        leftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        rightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        armLeft.setDirection(DcMotor.Direction.FORWARD);
        armRight.setDirection(DcMotor.Direction.REVERSE);
        armLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        armRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        armLeft.setPower(0.0);
        armRight.setPower(0.0);

        telemetry.addData("Status", "Initialized");
    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */
    @Override
    public void init_loop() {
    }

    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start() {
        runtime.reset();

        armLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armLeft.setTargetPosition(armHomePosition);
        armRight.setTargetPosition(armHomePosition);
        armLeft.setPower(1.0);
        armRight.setPower(1.0);
        armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {
        double leftPower;
        double rightPower;
        double manualArmPower;

        //DRIVE
        double drive = gamepad1.left_stick_y;
        double turn  =  gamepad1.right_stick_x;
        double leftIdeal = drive + turn;
        double rightIdeal = drive - turn;
        if (speedToggle) {
            leftIdeal = (drive + turn) * 0.5;
            rightIdeal = (drive - turn) * 0.5;
        }
        leftPower    = Range.clip(leftIdeal, -1.0, 1.0) ;
        rightPower   = Range.clip(rightIdeal, -1.0, 1.0) ;

        leftDrive.setPower(leftPower);
        rightDrive.setPower(rightPower);


        if (spinToggle) {
            if (reverseSpinToggle) {
                servo1.setPosition(targetIntakePosition);
                servo2.setPosition(-targetIntakePosition);
            } else {
                servo1.setPosition(-targetIntakePosition);
                servo2.setPosition(targetIntakePosition);
            }
        }
        else if (!spinToggle) {
            servo1.setPosition(0.5);
            servo2.setPosition(0.5);
        }
        //ARM & WRIST
        manualArmPower = gamepad1.right_trigger - gamepad1.left_trigger;
        if (Math.abs(manualArmPower) > armManualDeadband) {
            if (!manualMode) {
                armLeft.setPower(0.0);
                armRight.setPower(0.0);
                armLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                armRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                manualMode = true;
            }
            armLeft.setPower(manualArmPower);
            armRight.setPower(manualArmPower);
        }
        else {
            if (manualMode) {
                armLeft.setTargetPosition(armLeft.getCurrentPosition());
                armRight.setTargetPosition(armRight.getCurrentPosition());
                armLeft.setPower(1.0);
                armRight.setPower(1.0);
                armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                armRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                manualMode = false;
            }

        }



        //Re-zero encoder button
        if (gamepad1.start) {
            armLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            armRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            armLeft.setPower(0.0);
            armRight.setPower(0.0);
            manualMode = false;
        }

        //Watchdog to shut down motor once the arm reaches the home position
        if (!manualMode &&
                armLeft.getMode() == DcMotor.RunMode.RUN_TO_POSITION &&
                armLeft.getTargetPosition() <= armShutdownThreshold &&
                armLeft.getCurrentPosition() <= armShutdownThreshold
        ) {
            armLeft.setPower(0.0);
            armRight.setPower(0.0);
            armLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            armRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        if (gamepad2.right_trigger > 0.1) {
            // wrist range 0.5 (back inactive) to  0.4? (intake and scoring)
            if (curWrist <= 0.99) {
                curWrist += 0.01;
                wrist.setPosition(curWrist);
            }
        } else if (gamepad2.left_trigger > 0.1) {
            if (curWrist >= 0.01) {
                curWrist -= 0.01;
                wrist.setPosition(curWrist);
            }
        }

        if (gamepad1.right_bumper && System.currentTimeMillis() - speedPress > 500) {
            spinPress = System.currentTimeMillis();
            speedToggle = !speedToggle;
        }

        if (gamepad1.x) {
            armLeft.setTargetPosition(armScorePosition);
            armRight.setTargetPosition(armScorePosition);
            armLeft.setPower(0.7);
            armRight.setPower(0.7);
            armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            armRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }
        else if (gamepad1.b) {
            armLeft.setTargetPosition(armHomePosition);
            armRight.setTargetPosition(armHomePosition);
            armLeft.setPower(1.0);
            armRight.setPower(1.0);
            armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            armRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }
        else if (gamepad1.a && System.currentTimeMillis() - spinPress > 500) {
            spinPress = System.currentTimeMillis();
            spinToggle = !spinToggle;
        }
        else if (gamepad1.y && System.currentTimeMillis() - reversePress > 500) {
            reversePress = System.currentTimeMillis();
            reverseSpinToggle = !reverseSpinToggle;
        }

        telemetry.addData("Status", "Run Time: " + runtime.toString());
        telemetry.addData("Gamepad", "drive (%.2f), turn (%.2f)", drive, turn);
        telemetry.addData("Motors", "left (%.2f), right (%.2f)", leftPower, rightPower);
        telemetry.addData("Manual Power", manualArmPower);
        telemetry.addData("Arm Pos:",
                "left = " +
                        ((Integer)armLeft.getCurrentPosition()).toString() +
                        ", right = " +
                        ((Integer)armRight.getCurrentPosition()).toString());
        telemetry.addData("Arm Pos:",
                "left = " +
                        ((Integer)armLeft.getTargetPosition()).toString() +
                        ", right = " +
                        ((Integer)armRight.getTargetPosition()).toString());
        telemetry.addData("Speed Limiter: ", speedToggle);
        telemetry.addData("Spin Enabled: ", spinToggle);
        telemetry.addData("Reverse Spin: ", reverseSpinToggle);

        telemetry.update();

    }

    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }

}
