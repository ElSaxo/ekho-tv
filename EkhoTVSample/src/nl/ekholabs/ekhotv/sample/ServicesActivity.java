package nl.ekholabs.ekhotv.sample;

import java.util.ArrayList;
import java.util.List;

import javax.tv.service.Service;
import javax.tv.service.navigation.ServiceIterator;

import nl.ekholabs.ekhotv.activity.EkhoTVActivity;
import nl.ekholabs.ekhotv.sample.adapter.ServiceInfoAdapter;
import nl.ekholabs.ekhotv.sample.util.SeviceInformationUtilities;
import android.os.Bundle;
import android.widget.ListView;

public class ServicesActivity extends EkhoTVActivity {

    private List<Service> services;
    private ServiceInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_services);

	services = new ArrayList<Service>();
	adapter = new ServiceInfoAdapter(this, services);

	ListView servicesList = (ListView) findViewById(R.id.services_list);
	servicesList.setAdapter(adapter);

	setSupportProgressBarIndeterminateVisibility(true);
	
	configureServicesList();
    }

    private void configureServicesList() {
	SeviceInformationUtilities informationUtilities = new SeviceInformationUtilities();

	ServiceIterator iterator = informationUtilities.loadServices();
	if (iterator != null) {
	    while (iterator.hasNext()) {
		Service s = iterator.nextService();
		adapter.add(s);
	    }
	    adapter.notifyDataSetChanged();
	}
	setSupportProgressBarIndeterminateVisibility(false);
    }
}