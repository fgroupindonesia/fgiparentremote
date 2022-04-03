package helper;

import android.app.Activity;
import android.widget.Toast;


public class ShowDialog {

    private static Activity myAct;
    private static String pesan;

    private static void clearUp(){
        if(myAct!=null){
            myAct = null;
        }

        if(pesan != null){
            pesan = null;
        }
    }

    public static void message(Activity komp, String pesanMasuk){

        clearUp();
        setup(komp, pesanMasuk);

        myAct.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(myAct,pesan,Toast.LENGTH_LONG).show();
            }
        });

    }

    private static void setup(Activity k, String s){
        myAct = k;
        pesan = s;
    }

    public static void shortMessage(Activity komp, String pesanMasuk){

        clearUp();
        setup(komp, pesanMasuk);

        myAct.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(myAct,pesan,Toast.LENGTH_SHORT).show();
            }
        });

        //Toast.makeText(komp,pesanMasuk,Toast.LENGTH_SHORT).show();

    }

}
