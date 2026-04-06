package com.example.breeze_seas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

/**
 * TicketsFragment is the top-level Tickets destination displayed in bottom navigation.
 *
 * <p>Current state:
 * - Hosts the tab shell for {@link TicketDB}-backed ticket fragments.
 */
public class TicketsFragment extends Fragment {
    private static final String STATE_SELECTED_TAB = "state_selected_tab";

    private ViewPager2 viewPager;
    private TextView[] tabButtons;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;
    private int selectedTabIndex;

    /**
     * Inflates the Tickets shell, binds the segmented tab controls, and attaches the pager.
     *
     * @param inflater Layout inflater used to build the fragment view.
     * @param container Optional parent that will host the inflated hierarchy.
     * @param savedInstanceState Saved state containing the previously selected tab, if any.
     * @return The inflated Tickets root view.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_tickets, container, false);

        viewPager = view.findViewById(R.id.tickets_view_pager);
        tabButtons = new TextView[] {
                view.findViewById(R.id.tickets_tab_active),
                view.findViewById(R.id.tickets_tab_attending),
                view.findViewById(R.id.tickets_tab_past)
        };

        viewPager.setAdapter(new TicketsPagerAdapter(this));
        viewPager.setOffscreenPageLimit(3);

        for (int i = 0; i < tabButtons.length; i++) {
            final int tabIndex = i;
            tabButtons[i].setOnClickListener(new View.OnClickListener() {
                /**
                 * Switches the pager to the tapped ticket tab.
                 *
                 * @param v The tab button that was pressed.
                 */
                @Override
                public void onClick(View v) {
                    selectTab(tabIndex, true);
                }
            });
        }

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            /**
             * Syncs the custom tab controls when the pager changes pages.
             *
             * @param position The pager position that is now selected.
             */
            @Override
            public void onPageSelected(int position) {
                updateTabSelection(position);
            }
        };
        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        selectedTabIndex = savedInstanceState != null
                ? savedInstanceState.getInt(STATE_SELECTED_TAB, 0)
                : 0;
        selectTab(selectedTabIndex, false);

        return view;
    }

    /**
     * Stores the currently selected tab so the custom segmented control restores correctly.
     *
     * @param outState Bundle that receives the selected tab index.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_TAB, selectedTabIndex);
    }

    /**
     * Releases pager references and callbacks tied to the fragment view hierarchy.
     */
    @Override
    public void onDestroyView() {
        if (viewPager != null && pageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
            pageChangeCallback = null;
        }

        if (viewPager != null) {
            viewPager.setAdapter(null);
            viewPager = null;
        }

        tabButtons = null;

        super.onDestroyView();
    }

    /**
     * Selects a tab, updates the segmented control state, and optionally animates the pager move.
     *
     * @param index The zero-based tab index to select.
     * @param smoothScroll {@code true} to animate the pager move; {@code false} to jump directly.
     */
    private void selectTab(int index, boolean smoothScroll) {
        if (viewPager == null) {
            return;
        }

        selectedTabIndex = index;
        updateTabSelection(index);

        if (viewPager.getCurrentItem() != index) {
            viewPager.setCurrentItem(index, smoothScroll);
        }
    }

    public void openTab(int index) {
        if (index < 0 || index >= 3) {
            return;
        }
        selectTab(index, true);
    }

    /**
     * Updates the activated state of the custom segmented control buttons.
     *
     * @param selectedIndex The zero-based tab index that should appear selected.
     */
    private void updateTabSelection(int selectedIndex) {
        selectedTabIndex = selectedIndex;

        if (tabButtons == null) {
            return;
        }

        for (int i = 0; i < tabButtons.length; i++) {
            boolean isSelected = i == selectedIndex;
            tabButtons[i].setActivated(isSelected);
            tabButtons[i].setSelected(isSelected);
        }
    }

    /**
     * Pager adapter that supplies the three Tickets child fragments.
     */
    private static class TicketsPagerAdapter extends FragmentStateAdapter {

        /**
         * Creates a pager adapter scoped to the parent Tickets fragment.
         *
         * @param fragment The parent fragment that owns the pager lifecycle.
         */
        public TicketsPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        /**
         * Creates the fragment for the requested ticket tab position.
         *
         * @param position The zero-based pager position.
         * @return The fragment that should render the requested tab.
         */
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) return new ActiveTicketsFragment();
            if (position == 1) return new AttendingTicketsFragment();
            return new PastTicketsFragment();
        }

        /**
         * Returns the number of fixed ticket tabs.
         *
         * @return Always {@code 3} for Active, Attending, and Past.
         */
        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
