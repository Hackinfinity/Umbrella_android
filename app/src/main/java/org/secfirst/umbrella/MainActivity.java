package org.secfirst.umbrella;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.secfirst.umbrella.adapters.DrawerAdapter;
import org.secfirst.umbrella.fragments.DashboardFragment;
import org.secfirst.umbrella.fragments.DifficultyFragment;
import org.secfirst.umbrella.fragments.TabbedFragment;
import org.secfirst.umbrella.models.Category;
import org.secfirst.umbrella.models.Difficulty;
import org.secfirst.umbrella.models.DrawerChildItem;
import org.secfirst.umbrella.util.UmbrellaUtil;

import java.util.List;


public class MainActivity extends BaseActivity implements DifficultyFragment.OnDifficultySelected {

    public DrawerLayout drawer;
    public ExpandableListView drawerList;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    public int groupItem, navItem;
    public long drawerItem;
    private Spinner titleSpinner;
    private DrawerChildItem childItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UmbrellaUtil.setStatusBarColor(this, getResources().getColor(R.color.umbrella_purple_dark));
        UmbrellaUtil.migrateData();
        if (global.hasPasswordSet() && !global.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (!global.getTermsAccepted()) {
            startActivity(new Intent(this, TourActivity.class));
        }

        titleSpinner = (Spinner) findViewById(R.id.spinner_nav);
        titleSpinner.setTag(0);
        navItem = 0;
        groupItem = -1;
        titleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<Difficulty> hasDifficulty = Difficulty.find(Difficulty.class, "category = ?", String.valueOf(childItem.getPosition()));
                if (hasDifficulty.size() > 0) {
                    hasDifficulty.get(0).setSelected(position);
                    hasDifficulty.get(0).save();
                }
                if (((Integer) titleSpinner.getTag()) == position) {
                    return;
                }
                titleSpinner.setTag(position);
                setFragment(1, childItem.getTitle());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ExpandableListView) findViewById(R.id.left_drawer);
        DrawerAdapter adapter = new DrawerAdapter(this);
        View header = View.inflate(this, R.layout.drawer_header, null);
        final TextView loginHeader = (TextView) header.findViewById(R.id.login_header);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (global.hasPasswordSet()) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
                    global.setPassword(MainActivity.this);
                }
                loginHeader.setText(global.isLoggedIn() ? R.string.log_out : R.string.log_in);
            }
        });
        loginHeader.setText(global.isLoggedIn() ? R.string.log_out : R.string.log_in);
        drawerList.addHeaderView(header);
        drawerList.setAdapter(adapter);
        drawerList.setOnChildClickListener(adapter);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer,
                R.drawable.ic_drawer, R.string.open_drawer,
                R.string.close_drawer) {
            public void onDrawerClosed(View view) {}

            public void onDrawerOpened(View drawerView) {}
        };

        drawer.setDrawerListener(actionBarDrawerToggle);
        setFragment(0, "My Security");
        drawer.openDrawer(Gravity.LEFT);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    public void setNavItems(String title) {
        Category childCategory = Category.findById(Category.class, childItem.getPosition());
        String[] navArray = new String[] {};
//        switch (childCategory.getDifficulties()) {
//            case 1:
//                navArray = new String[] {title +" Beginner"};
//                break;
//            case 2:
//                navArray = new String[] {title +" Intermediate"};
//                break;
//            case 3:
//                navArray = new String[] {title +" Expert"};
//                break;
//            case 4:
//                navArray = new String[] {title +" Beginner", title +" Expert"};
//                break;
//            case 5:
//                navArray = new String[] {title +" Intermediate", title +" Expert"};
//                break;
//            case 7:
//                break;
//        }
                navArray = new String[] {title +" Beginner", title +" Advanced", title +" Expert"};
        ArrayAdapter<String> navAdapter = new ArrayAdapter<>(this, R.layout.spinner_nav_item, android.R.id.text1, navArray);
        titleSpinner.setVisibility(View.VISIBLE);
        titleSpinner.setAdapter(navAdapter);
    }

    public void setFragment(int fragType, String groupName) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragType == 0) {
            fragmentTransaction.replace(R.id.container, DashboardFragment.newInstance(global)).commit();
            drawer.closeDrawer(drawerList);
            titleSpinner.setVisibility(View.GONE);
            setTitle(groupName);
        } else {
            List<Difficulty> hasDifficulty = Difficulty.find(Difficulty.class, "category = ?", String.valueOf(childItem.getPosition()));
            drawerItem = childItem.getPosition();
            drawer.closeDrawer(drawerList);
            setNavItems(childItem.getTitle());
            if (hasDifficulty.size() > 0) {
                fragmentTransaction.replace(R.id.container, TabbedFragment.newInstance(childItem.getPosition(), hasDifficulty.get(0).getSelected()), childItem.getTitle()).commit();
                if (hasDifficulty.get(0).getSelected() >= titleSpinner.getAdapter().getCount()) {
                    titleSpinner.setSelection(titleSpinner.getAdapter().getCount()-1);
                } else {
                    titleSpinner.setSelection(hasDifficulty.get(0).getSelected());
                }
            } else {
                fragmentTransaction.replace(R.id.container, DifficultyFragment.newInstance(childItem.getPosition()), childItem.getTitle()).commit();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    public void onNavigationDrawerItemSelected(DrawerChildItem selectedItem) {
        childItem = selectedItem;
        setFragment(1, "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem _searchMenuItem = menu.findItem(R.id.action_search_view);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(_searchMenuItem);
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.i("submit", s);
                if (s.length()>2) {
                    Intent i = new Intent(MainActivity.this, SearchActivity.class);
                    i.setAction(Intent.ACTION_SEARCH);
                    i.putExtra(SearchManager.QUERY, s);
                    startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this, "The search query needs to be at least 3 characters long", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(_searchMenuItem,new MenuItemCompat.OnActionExpandListener()
        {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemResetPw = menu.findItem(R.id.action_reset_password);
        MenuItem itemSetPw = menu.findItem(R.id.action_set_password);
        MenuItem itemLogout = menu.findItem(R.id.action_logout);
        itemSetPw.setVisible(!global.hasPasswordSet());
        itemResetPw.setVisible(global.hasPasswordSet());
        itemLogout.setVisible(global.hasPasswordSet());
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        int id = item.getItemId();
        if (sdk >= android.os.Build.VERSION_CODES.HONEYCOMB && id==android.R.id.home) {
            if (drawer.isDrawerOpen(drawerList))
                drawer.closeDrawer(drawerList);
            else
                drawer.openDrawer(drawerList);
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_logout) {
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        }
        if (id == R.id.action_set_password) {
            global.setPassword(this);
            return true;
        }
        if (id == R.id.action_reset_password) {
            global.resetPassword(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDifficultySelected(int difficulty) {
        Log.i("ch count", String.valueOf(titleSpinner.getChildCount()));
        if (difficulty >= titleSpinner.getAdapter().getCount()) {
            titleSpinner.setSelection(titleSpinner.getAdapter().getCount()-1);
        } else {
            titleSpinner.setSelection(difficulty);
        }
        setFragment(1, "");
    }
}
