package no.uio.ifi.viettt.mscosa;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import no.uio.ifi.viettt.mscosa.MainFragments.PlotViewFragment;
import no.uio.ifi.viettt.mscosa.MainFragments.SourceFromFileFragment;
import no.uio.ifi.viettt.mscosa.MainFragments.MiningFragment;
import no.uio.ifi.viettt.mscosa.MainFragments.ServerFragment;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;
import no.uio.ifi.viettt.mscosa.Services.Constants;
import no.uio.ifi.viettt.mscosa.Services.ForegroundDBService;

public class MainActivity extends AppCompatActivity {
    public ServerFragment swf;
    public SourceFromFileFragment ef;
    public PlotViewFragment ccsf;
    public MiningFragment mf;

    public BottomBar bottomBar;

    public MainActivity(){
        swf = new ServerFragment();
        ef = new SourceFromFileFragment();
        ccsf = new PlotViewFragment();
        mf = new MiningFragment();

        swf.setmMainActivityAndOther(this,ccsf);
        ef.setPointerToCurUI(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId){
                    case R.id.action_sensorWrapper:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, swf).commit();
                        break;
                    case R.id.action_export:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,ef).commit();
                        break;
                    case R.id.action_current_connected:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,ccsf).commit();
                        break;
                    case R.id.action_mining:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,mf).commit();
                        break;
                    default:
                        break;
                }
            }
        });

        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
            }
        });


    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        swf.stopAllConnection();
    }


}

