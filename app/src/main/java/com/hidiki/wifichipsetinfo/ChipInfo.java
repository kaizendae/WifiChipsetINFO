package com.hidiki.wifichipsetinfo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.internal.gmsg.HttpClient;

import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.crypto.Mac;

import static java.lang.String.*;

public class ChipInfo extends AppCompatActivity {

    private TextView MacVendor;
    private EditText MacTxt ;
    private Button BtnGet;
    private  Button BtnClear;
    private Button BtnDetect;
    private String Macres;
    private String SearchString;
    private FileInputStream is;
    private AdView myAdv;
    private BufferedReader reader;
    private InterstitialAd mInterstitialAd;
    private ProgressBar pbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chip_info);
         MacVendor = (TextView)findViewById(R.id.MacVendor);
         BtnGet = (Button)findViewById(R.id.BtnGet);
         MacTxt = (EditText)findViewById(R.id.MacTxt);
         BtnClear = (Button)findViewById(R.id.ClearBtn);
         BtnDetect = (Button)findViewById(R.id.DetectBtn);
         pbar = (ProgressBar) findViewById(R.id.ProgressBB);

        BtnDetect.setEnabled(false);
        MobileAds.initialize(this, "YOUR_ADMOB_APP_ID");

        myAdv = (AdView)findViewById(R.id.adView);
        AdRequest adrequest = new AdRequest.Builder().build();
        myAdv.loadAd(adrequest);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

    }

    protected void onPause(){
        super.onPause();
        mInterstitialAd.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    public void OnClickGetMac(View view){
        MacTxt.setText(getMacAddr());
        if(!MacTxt.getText().toString().equals(""))
        BtnDetect.setEnabled(true);
    }
    public void OnClickDetect(View view){
        if(MacTxt.getText().toString().isEmpty())
            MacTxt.setText(getMacAddr());
        new findMacTask().execute(MacTxt.getText().toString());
    }
    public void OnClickClear(View view){
        MacTxt.setText("");
        MacVendor.setText("");
        BtnDetect.setEnabled(false);
    }
    class findMacTask extends AsyncTask<String ,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... mac) {
            String resultat = "Vendor not found";
            try {
                resultat = readFromAssets("macdb.dat", mac[0]);
               /*if (resultat == null) {
                    resultat = getPageGet(url2 + mac[0]);
                }*/
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return resultat;
        }

        @Override
        protected void onPostExecute(String resultat) {
            pbar.setVisibility(View.INVISIBLE);
            MacVendor.setText(resultat);
            if(resultat.isEmpty()){
                MacVendor.setText("Vendor Not found");
            }
        }
    }


    public static String getMacAddr(){
        try {
            String str = "wlan0";
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.getName().equalsIgnoreCase(str)) {
                    byte[] hardwareAddress = networkInterface.getHardwareAddress();
                    if (hardwareAddress == null) {
                        return "";
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    int length = hardwareAddress.length;
                    for (int i = 0; i < length; i++) {
                        stringBuilder.append(format("%02X:", new Object[] { Byte.valueOf(hardwareAddress[i]) }));
                    }
                    if (stringBuilder.length() > 0) {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                    return stringBuilder.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    /*public String getPageGet(String url) {
        StringBuffer text = new StringBuffer("");
        try {
            URI uri = new URI(url);
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                text.append(line + "\n");
            }
        } catch (Exception e) {
            Log.i("Exemple_Android", e.getMessage());
        }
        return text.toString();
    }*/

    public String readFromAssets(String filename, String mac) throws IOException {
        String mac_adr = mac.replace(":", "").replace("-", "").replace(".", "").replace(" ", "");
        BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(filename)));

        // do reading, usually loop until end of file reading
        StringBuilder sb = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            String s = mLine.split("~")[0];
            if (mac_adr.contains(s)) {
                return mLine.split("~")[1];

            }
            //

            mLine = reader.readLine();

        }
        reader.close();

        return null;
    }


}
