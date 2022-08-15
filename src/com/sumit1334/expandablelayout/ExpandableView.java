package com.sumit1334.expandablelayout;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.AndroidViewComponent;

import com.sumit1334.expandablelayout.expandablelayout.ExpandableLayout;
import static com.sumit1334.expandablelayout.expandablelayout.ExpandableLayout.State.COLLAPSED;
import static com.sumit1334.expandablelayout.expandablelayout.ExpandableLayout.State.COLLAPSING;
import static com.sumit1334.expandablelayout.expandablelayout.ExpandableLayout.State.EXPANDED;
import static com.sumit1334.expandablelayout.expandablelayout.ExpandableLayout.State.EXPANDING;

import java.util.HashMap;

public class ExpandableView extends AndroidNonvisibleComponent implements Component {
    private final Context context;
    private final String TAG = "Expandable View";
    private final HashMap<String, ExpandableViewHelper> layouts = new HashMap<>();

    public ExpandableView(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
        Log.i(TAG, "ExpandableView: Extension Initialized");
    }

    @SimpleEvent
    public void Expanded(final String id) {
        EventDispatcher.dispatchEvent(this, "Expanded", id);
    }

    @SimpleEvent
    public void Expanding(final String id, float expandFraction) {
        EventDispatcher.dispatchEvent(this, "Expanding", id, expandFraction);
    }

    @SimpleEvent
    public void Collapsed(final String id) {
        EventDispatcher.dispatchEvent(this, "Collapsed", id);
    }

    @SimpleEvent
    public void Collapsing(final String id, float expandFraction) {
        EventDispatcher.dispatchEvent(this, "Collapsing", id, expandFraction);
    }

    @SimpleFunction
    public void CreateLayout(final String id, AndroidViewComponent in, AndroidViewComponent header, AndroidViewComponent content, String orientation) {
        if (this.layouts.containsKey(id))
            return;
        Log.i(TAG, "CreateLayout: Creating the expandable layout");
        this.layouts.put(id, new ExpandableViewHelper(id, in, header, content, orientation, false));
    }

    @SimpleFunction
    public void CreateLayoutFirst(final String id, AndroidViewComponent in, AndroidViewComponent header, AndroidViewComponent content, String orientation) {
        if (this.layouts.containsKey(id))
            return;
        Log.i(TAG, "CreateLayout: Creating the expandable layout");
        this.layouts.put(id, new ExpandableViewHelper(id, in, header, content, orientation, true));
    }

    @SimpleFunction
    public void Expand(final String id, int duration, boolean animate) {
        if (this.layouts.containsKey(id)) {
            final ExpandableLayout layout = this.layouts.get(id).expandableLayout;
            layout.setDuration(duration);
            layout.expand(animate);
        }
    }

    @SimpleFunction
    public void Collapse(final String id, int duration, boolean animate) {
        if (this.layouts.containsKey(id)) {
            final ExpandableLayout layout = this.layouts.get(id).expandableLayout;
            layout.setDuration(duration);
            layout.collapse(animate);
        }
    }

    @SimpleFunction
    public boolean IsExpanded(String id) {
        try {
            return this.layouts.get(id).expandableLayout.isExpanded();
        } catch (Exception e) {
            throw new NullPointerException("ID does not exist");
        }
    }

    @SimpleFunction
    public boolean IdExist(String id) {
        return layouts.containsKey(id);
    }

    @SimpleFunction
    public void Remove(String id) {
        if (this.layouts.containsKey(id)) {
            layouts.get(id).remove();
            layouts.remove(id);
        }
    }

    @SimpleProperty
    public String Vertical() {
        return "Vertical";
    }

    @SimpleProperty
    public String Horizontal() {
        return "Horizontal";
    }

    public final class ExpandableViewHelper implements ExpandableLayout.OnExpansionUpdateListener {

        private final ExpandableLayout expandableLayout;
        private final String id;
        private final LinearLayout holder;

        public ExpandableViewHelper(@NonNull String id, AndroidViewComponent in, AndroidViewComponent header, AndroidViewComponent content, @Nullable String orientation, boolean isAbove) {
            this.id = id;
            this.holder = new LinearLayout(context);
            holder.setLayoutParams(new LayoutParams(-1, -2));
            this.expandableLayout = new ExpandableLayout(context);
            holder.setOrientation(orientation.equals(Horizontal()) ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
            expandableLayout.setOrientation(orientation.equals(Vertical()) ? ExpandableLayout.VERTICAL : ExpandableLayout.HORIZONTAL);
            expandableLayout.setExpanded(false, false);
            expandableLayout.setOnExpansionUpdateListener(this);
            expandableLayout.addView(removeView(content));
            if (isAbove) {
                holder.addView(expandableLayout);
                holder.addView(removeView(header));
            } else {
                holder.addView(removeView(header));
                holder.addView(expandableLayout);
            }
            if (in.getClass().getSimpleName().equalsIgnoreCase("MakeroidCardView")) {
                View view = in.getView();
                ViewGroup layout = (ViewGroup) view;
                ViewGroup cardView = (ViewGroup) layout.getChildAt(0);
                ViewGroup container = (ViewGroup) cardView.getChildAt(0);
                container.addView(holder);
            } else
                ((LinearLayout) ((ViewGroup) in.getView()).getChildAt(0)).addView(holder);
        }

        private View removeView(AndroidViewComponent component) {
            View view = component.getView();
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            if (viewGroup != null)
                viewGroup.removeView(view);
            return view;
        }

        public void remove() {
            ViewGroup viewGroup = (ViewGroup) holder.getParent();
            viewGroup.removeView(holder);
        }

        @Override
        public void onExpansionUpdate(float expansionFraction, int state) {
            final String position = id;
            if (state == EXPANDED)
                Expanded(position);
            else if (state == EXPANDING)
                Expanding(position, expansionFraction);
            else if (state == COLLAPSED)
                Collapsed(position);
            else if (state == COLLAPSING)
                Collapsing(position, expansionFraction);
        }
    }
}