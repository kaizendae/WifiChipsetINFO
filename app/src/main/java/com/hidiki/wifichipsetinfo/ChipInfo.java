package com.hidiki.wifichipsetinfo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URL;
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
        MobileAds.initialize(this,"ca-app-pub-4739138619539871~5960738859");

        myAdv = (AdView)findViewById(R.id.adView);
        AdRequest adrequest = new AdRequest.Builder().build();
        myAdv.loadAd(adrequest);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4739138619539871/2580375978");
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
               if (resultat == null) {
                    resultat = getPageGet(mac[0]);
                }
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
    public String getPageGet(String mac) {
        HttpURLConnection urlConnection = null;
        StringBuffer text = new StringBuffer("");
        String ur = "https://macvendors.co/api/vendorname/" + mac;
        StringBuilder Output = new StringBuilder();

        try{
            URL url = new URL(ur);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while ((line = reader.readLine()) != null) {
                Output.append(line);
            }
            in.close();

            Log.i("klm",Output.toString());

        }catch (Exception e) {
            Log.i("Exemple_Android", e.getMessage());
        }finally {
            urlConnection.disconnect();
        }
        return Output.toString();
    }

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
            mLine = reader.readLine();

        }
        reader.close();

        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_more_apps:
                String developer_id = "Androdiki";
                try {

                    this.startActivity(
                            new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://store/apps/developer?id=" + developer_id)
                            )
                    );

                } catch (android.content.ActivityNotFoundException e) {

                    this.startActivity(
                            new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/developer?id=" + developer_id)
                            )
                    );

                }
                break;
            case R.id.action_about:
                //View messageView = getLayoutInflater().inflate(R.layout.about, null);

                // When linking text, force to always use default color. This works
                // around a pressed color state bug.
                String version = "1.0.0";
                try {
                    PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("About");
                builder.setMessage("Wifi CHipSet Info \n\n" + version + "\n\nCopyright © Heidiki 2018");
                builder.setPositiveButton("More Apps", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String developer_id = "Androdiki";
                        try {

                            ChipInfo.this.startActivity(
                                    new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("market://store/apps/developer?id=" + developer_id)
                                    )
                            );

                        } catch (android.content.ActivityNotFoundException e) {

                            ChipInfo.this.startActivity(
                                    new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/apps/developer?id=" + developer_id)
                                    )
                            );

                        }
                    }
                });
                builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mInterstitialAd.show();
                    }
                });
                AlertDialog Dialog = builder.create();
                Dialog.show();
                break;
        }
        return true;    }
}
