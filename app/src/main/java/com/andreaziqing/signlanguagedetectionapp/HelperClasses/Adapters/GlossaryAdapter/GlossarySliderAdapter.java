package com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.GlossaryAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.andreaziqing.signlanguagedetectionapp.R;

public class GlossarySliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public GlossarySliderAdapter(Context context) {
        this.context = context;
    }

    int[] images = {
            R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d,
            R.drawable.e, R.drawable.f, R.drawable.g, R.drawable.h, R.drawable.i, R.drawable.j, R.drawable.k,
            R.drawable.l, R.drawable.m, R.drawable.n, R.drawable.o, R.drawable.p, R.drawable.q, R.drawable.r,
            R.drawable.s, R.drawable.t, R.drawable.u, R.drawable.v, R.drawable.w, R.drawable.x, R.drawable.y,
            R.drawable.z
    };

    int[] titles = {
            R.string.A, R.string.B, R.string.C, R.string.D,
            R.string.E, R.string.F, R.string.G, R.string.H, R.string.I, R.string.J, R.string.K,
            R.string.L, R.string.M, R.string.N, R.string.O, R.string.P, R.string.Q, R.string.R,
            R.string.S, R.string.T, R.string.U, R.string.V, R.string.W, R.string.X, R.string.Y,
            R.string.Z
    };

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (ConstraintLayout)object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.glossary_slides_layout, container, false);

        ImageView image_bg = view.findViewById(R.id.first_letter_slide);
        TextView title = view.findViewById(R.id.first_letter_slide_title);

        image_bg.setImageResource(images[position]);
        title.setText(titles[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout)object);
    }
}

