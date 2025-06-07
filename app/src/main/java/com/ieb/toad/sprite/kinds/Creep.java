package com.ieb.toad.sprite.kinds;

import com.ieb.toad.world.core.Thing;

public abstract class Creep extends Thing {

    /** Called when this creep is hit by a thrown object.
     *  Should normally bump and go into falling-through mode.*/
    public abstract void hitCreep(Thing hitBy);

    /** Called when this creep is thrown by Toad */
    public abstract void thrown();
}
