package com.kuzasystems.myapplication;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.kuzasystems.myapplication.databinding.ActivityMainBinding;
import com.kuzasystems.printer.printerclasses.PosPrinter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(view -> {
            universalPrinter();
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public void universalPrinter(){
        String line;
        line="- - - - - - - - - - - - - - - - - - - - - -";
        List<PosPrinter.PosPrinterEntry> entries = new ArrayList<>();
        entries.add(new PosPrinter.PosPrinterEntry("****** REPRINTED ******",true, "CENTER","STRING"));
        entries.add(new PosPrinter.PosPrinterEntry("*** PARCELS RECEIPT ***",true, "CENTER","STRING"));
        entries.add(new PosPrinter.PosPrinterEntry(line,true, "CENTER","LINE"));
        entries.add(new PosPrinter.PosPrinterEntry("Tracking Number : 9256",true, "LEFT","STRING"));
        entries.add(new PosPrinter.PosPrinterEntry("Origin: Embu",false, "LEFT","STRING"));
        entries.add(new PosPrinter.PosPrinterEntry("Destination: NYERI LOWER",false, "LEFT","STRING"));
        entries.add(new PosPrinter.PosPrinterEntry("Sender: David",false, "LEFT","STRING"));
        new PosPrinter().print(this,entries);
    }
}