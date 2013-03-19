package nl.ekholabs.ekhotv.sample.adapter;

import java.util.List;

import javax.tv.service.Service;

import nl.ekholabs.ekhotv.sample.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Wilder Rodrigues (wilder.rodrigues@ekholabs.nl)
 */
public class ServiceInfoAdapter extends ArrayAdapter<Service> {

    private final Context context;
    private final List<Service> values;

    public ServiceInfoAdapter(Context context, List<Service> values) {
	super(context, R.layout.activity_services_items, values);
	this.context = context;
	this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	LayoutInflater inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	Service service = values.get(position);

	View rowView = inflater.inflate(R.layout.activity_services_items,
		parent, false);

	TextView serviceNameView = (TextView) rowView
		.findViewById(R.id.service_name);
	serviceNameView.setText(service.getName());

	TextView serviceTypeView = (TextView) rowView
		.findViewById(R.id.service_type);
	serviceTypeView.setText(service.getServiceType().toString());

	return rowView;
    }
}