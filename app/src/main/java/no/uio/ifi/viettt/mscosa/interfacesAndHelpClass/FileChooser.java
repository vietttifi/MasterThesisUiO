package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import ir.sohreco.androidfilechooser.FileChooserDialog;
import no.uio.ifi.viettt.mscosa.MainActivity;
import no.uio.ifi.viettt.mscosa.MainFragments.SourceFromFileFragment;

/**
 * Created by viettt on 06/12/2016.
 */
public class FileChooser implements FileChooserDialog.ChooserListener {
    String chosenFilePath = null;
    public FileChooser(){
    }

    @Override
    public void onSelect(String path) {
        chosenFilePath = path;
    }
    public String getChosenFilePath(){
        return chosenFilePath;
    }

}