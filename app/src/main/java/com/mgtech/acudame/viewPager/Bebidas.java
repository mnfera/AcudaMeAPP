package com.mgtech.acudame.viewPager;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mgtech.acudame.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Bebidas extends Fragment {

    View view;

    public Bebidas() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_bebidas, container, false);

        //Código



        return view;
    }
}
