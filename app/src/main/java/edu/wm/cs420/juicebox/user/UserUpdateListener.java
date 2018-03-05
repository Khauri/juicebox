package edu.wm.cs420.juicebox.user;

import edu.wm.cs420.juicebox.database.models.JuiceboxUser;

/**
 * Created by Khauri on 3/2/2018.
 */

public interface UserUpdateListener {
    void userCreated(JuiceboxUser user);
    void userUpdated(JuiceboxUser user);
}
