package com.daluiamit.shield;

public interface AppConfigUtils  {
    // This is the name of the file where we are keeping the user login state in app
    String USER_LOGIN_STATE_STORAGE_FILE_NAME = "com.daluiamit.shield.userlogin";

    String USER_LOGIN_STATE_KEY = "isLoggedIn";

    String CURRENT_LOGGED_IN_USER_KEY = "currentUser";

}
