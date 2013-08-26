package org.briansimulator.briandroidtemplate;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RenderscriptTest rstest = new RenderscriptTest();
        rstest.setAppContext(getApplicationContext());
        rstest.setStatusText("FOO");
        rstest.setup();
        rstest.run();
        TextView status = (TextView)findViewById(R.id.statusTextView);
        status.setText("SIMULATION FINISHED!\n");
        long parallelDura = rstest.getFullParallelVarsDuration();
        long serialDura = rstest.getSerialVarsDuration();
        status.append("Serial variable updating took   "+serialDura+" ms\n");
        status.append("Parallel variable updating took "+parallelDura+" ms\n");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
