/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.behavior;

import baritone.Baritone;
import baritone.api.Settings;
import baritone.api.behavior.ILookBehavior;
import baritone.api.event.events.PlayerUpdateEvent;
import baritone.api.event.events.RotationMoveEvent;
import baritone.api.utils.Rotation;

public final class LookBehavior extends Behavior implements ILookBehavior {

    /**
     * Target's values are as follows:
     */
    private Rotation target;

    /**
     * Whether or not rotations are currently being forced
     */
    private boolean force;

    /**
     * The last player yaw angle. Used when free looking
     *
     * @see Settings#freeLook
     */
    private float lastYaw;

    public LookBehavior(Baritone baritone) {
        super(baritone);
    }

    @Override
    public void updateTarget(Rotation target, boolean force) {
        this.target = target;
        if (!force) {
            double rand = Math.random() - 0.5;
            if (Math.abs(rand) < 0.1) {
                rand *= 4;
            }
            this.target = new Rotation(this.target.getYaw() + (float) (rand * Baritone.settings().randomLooking113.value), this.target.getPitch());
        }
        this.force = force || !Baritone.settings().freeLook.value;
    }

    @Override
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.target == null) {
            return;
        }

        // Whether or not we're going to silently set our angles
        boolean silent = Baritone.settings().antiCheatCompatibility.value && !this.force;

        switch (event.getState()) {
            case PRE: {
                if (this.force) {
                    float desiredYaw = Math.round(this.target.getYaw());
                    float oldPitch = Math.round(ctx.player().getXRot());
                    float desiredPitch = Math.round(this.target.getPitch());
                    double xPos = ctx.player().getX();
                    double yPos = ctx.player().getY();
                    double zPos = ctx.player().getZ();
                    ctx.player().lerpTo(xPos,yPos,zPos, desiredYaw, desiredPitch, Baritone.settings().smoothAim.value, true);
                    ctx.player().setYRot((float) (ctx.player().getYRot() + (Math.random() - 0.5) * Baritone.settings().randomLooking.value));
                    ctx.player().setXRot((float) (ctx.player().getXRot() + (Math.random() - 0.5) * Baritone.settings().randomLooking.value));
                    if (desiredPitch == oldPitch && !Baritone.settings().freeLook.value) {
                        nudgeToLevel();
                    }
                    this.target = null;
                }
                if (silent) {
                    this.lastYaw = ctx.player().getYRot();
                    ctx.player().setYRot(this.target.getYaw());
                }
                break;
            }
            case POST: {
                if (silent) {
                    ctx.player().setYRot(this.lastYaw);
                    this.target = null;
                }
                break;
            }
            default:
                break;
        }
    }

    public void pig() {
        if (this.target != null) {
            ctx.player().setYRot(this.target.getYaw());
        }
    }

    @Override
    public void onPlayerRotationMove(RotationMoveEvent event) {
        if (this.target != null) {

            event.setYaw(this.target.getYaw());

            // If we have antiCheatCompatibility on, we're going to use the target value later in onPlayerUpdate()
            // Also the type has to be MOTION_UPDATE because that is called after JUMP
            if (!Baritone.settings().antiCheatCompatibility.value && event.getType() == RotationMoveEvent.Type.MOTION_UPDATE && !this.force) {
                this.target = null;
            }
        }
    }

    /**
     * Nudges the player's pitch to a regular level. (Between {@code -20} and {@code 10}, increments are by {@code 1})
     */
    private void nudgeToLevel() {
        if (ctx.player().getXRot() < -20) {
            ctx.player().setXRot(ctx.player().getXRot() + 1);
        } else if (ctx.player().getXRot() > 10) {
            ctx.player().setXRot(ctx.player().getXRot() - 1);
        }
    }
}
