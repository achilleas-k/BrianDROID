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
        CodegenTemplate rstest = new CodegenTemplate();
        rstest.setAppContext(getApplicationContext());
        rstest.setStatusText("FOO");
        rstest.setup();
        rstest.run();
        TextView status = (TextView)findViewById(R.id.statusTextView);
        status.setText("SIMULATION FINISHED!\n");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
