package com.torstar.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailAntivityFragment extends Fragment {

    public DetailAntivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent= getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail_antivity, container, false);

        if (intent != null){
            String weather = intent.getStringExtra(Intent.EXTRA_TEXT);

            TextView textView = (TextView)rootView.findViewById(R.id.detail_textView);
            textView.setText(weather);

        }

        return rootView;
    }
}
