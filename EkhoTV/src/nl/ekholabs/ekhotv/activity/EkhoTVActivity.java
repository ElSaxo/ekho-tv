package nl.ekholabs.ekhotv.activity;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * @author Wilder Rodrigues (wilder.rodrigues@ekholabs.nl)
 */
public abstract class EkhoTVActivity extends SherlockActivity {

    EkhoTVActivity instance;

    public EkhoTVActivity() {
	instance = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	EkhoTVProxyActivity.getInstance().bindActivity(this);
    }
}