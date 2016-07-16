package app.taolin.cnbeta;

import android.app.Application;

/**
 * @author taolin
 * @version v1.0
 * @date Jul 11, 2016.
 * @description Application
 */

public class App extends Application {

    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static App getInstance() {
        return sInstance;
    }
}
