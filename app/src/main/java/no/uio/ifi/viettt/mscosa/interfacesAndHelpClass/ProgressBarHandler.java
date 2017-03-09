package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.os.Handler;
import android.os.Message;

import no.uio.ifi.viettt.mscosa.MainFragments.SourceFromFileFragment;

/**
 * Created by viettt on 11/02/2017.
 */

public class ProgressBarHandler extends Handler {
    /*
    private SourceFromFileFragment sourceFromFileFragment;

    public ProgressBarHandler(SourceFromFileFragment sourceFromFileFragment){
        this.sourceFromFileFragment = sourceFromFileFragment;
    }

    public void handleMessage(Message msg) {
        switch (msg.what){
            case SourceFromFileFragment.ADD_FILE_SOURCE:
                sourceFromFileFragment.updateListView();
                sourceFromFileFragment.lastUpdate += SourceFromFileFragment.REFRESH_INTERVAL;
                break;
            case SourceFromFileFragment.FILE_IS_LOADING:
                boolean needUpdate = (System.currentTimeMillis() - sourceFromFileFragment.lastUpdate) > SourceFromFileFragment.REFRESH_INTERVAL;
                if ( !sourceFromFileFragment.isScrolling && needUpdate) {
                    sourceFromFileFragment.updateListView();
                    sourceFromFileFragment.lastUpdate = System.currentTimeMillis();
                }
                break;
            case SourceFromFileFragment.START_STOP_LOADING:{
                if(!sourceFromFileFragment.loading_File_data.get(msg.arg1).isRunning() ) {
                    synchronized (sourceFromFileFragment.loading_File_data.get(msg.arg1).lock) {
                        sourceFromFileFragment.loading_File_data.get(msg.arg1).lock.notifyAll();
                    }
                }
                sourceFromFileFragment.loading_File_data.get(msg.arg1).setRunning( !sourceFromFileFragment.loading_File_data.get(msg.arg1).isRunning());
                if ( !sourceFromFileFragment.isScrolling ) {
                    sourceFromFileFragment.updateListView();
                    sourceFromFileFragment.lastUpdate += SourceFromFileFragment.REFRESH_INTERVAL;
                }
                break;
            }
            case SourceFromFileFragment.FILE_IS_LOADED:
                synchronized (sourceFromFileFragment.lock) {
                    sourceFromFileFragment.done_list.add(msg.arg1);
                }
                sourceFromFileFragment.updateListView();
                sourceFromFileFragment.lastUpdate += SourceFromFileFragment.REFRESH_INTERVAL;
                break;

            default:
                break;
        }

    }

    */

}
