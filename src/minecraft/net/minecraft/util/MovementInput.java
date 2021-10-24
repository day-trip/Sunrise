package net.minecraft.util;

public class MovementInput
{
    /**
     * The speed at which the player is strafing. Positive numbers to the left and negative to the right.
     */
    public float moveStrafe;

    /**
     * The speed at which the player is moving forward. Negative numbers will move backwards.
     */
    public float moveForward;
    public boolean jump;
    public boolean sneak;

    public void updatePlayerMoveState() throws Exception {
    }

    public void stopAll() {
        moveForward = 0;
        moveStrafe = 0;
    }

    public void setForward() {
        moveForward++;
    }

    public void setBackward() {
        moveForward--;
    }

    public void setLeft() {
        moveStrafe++;
    }

    public void setRight() {
        moveStrafe--;
    }
}
