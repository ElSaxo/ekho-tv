package nl.ekholabs.ekhotv.activity;

/**
 * @author Wilder Rodrigues (wilder.rodrigues@ekholabs.nl)
 */
public class EkhoTVProxyActivity {

    private static EkhoTVProxyActivity instance;
    static {
	instance = new EkhoTVProxyActivity();
    }

    private EkhoTVActivity activity;

    private EkhoTVProxyActivity() {
    }

    public static EkhoTVProxyActivity getInstance() {
	return instance;
    }

    public void bindActivity(EkhoTVActivity activity) {
	if (activity != null) {
	    this.activity = activity;
	}
    }

    public EkhoTVActivity getActivity() {
	return activity;
    }
}