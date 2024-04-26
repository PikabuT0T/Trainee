package com.example.traine;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import com.cloudmersive.client.invoker.ApiClient;
//import com.cloudmersive.client.invoker.ApiException;
//import com.cloudmersive.client.invoker.Configuration;
//import com.cloudmersive.client.invoker.auth.*;
//import com.cloudmersive.client.BarcodeLookupApi;
//
//import com.cloudmersive.client.model.BarcodeLookupResponse;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ScannerActivity extends AppCompatActivity{

    CoordinatorLayout main;
    FloatingActionButton buttonScan;
    TextView textView2;

    ImageButton buttonMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        buttonScan = findViewById(R.id.buttonScan);
        main = findViewById(R.id.main);
        buttonMain = findViewById(R.id.buttonToMainActivity);
        buttonMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScannerActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });

        buttonScan.setOnClickListener(view -> {
            scanCode();
        });
    }

    private void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
       if(result.getContents() != null)
       {
           //можно заменить
           AlertDialog.Builder builder = new AlertDialog.Builder(ScannerActivity.this);
           builder.setTitle("Result");
           builder.setMessage(result.getContents());
           builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialogInterface, int i) {
                   dialogInterface.dismiss();
               }
           }).show();

           String barcode = result.getContents();
           textView2.setText(barcode);

           //https://listex.info/uk/search/?q=4820007956638&type=goods
       }
    });


//    private void findByBarcode(String contents) {
//        ApiClient defaultClient = Configuration.getDefaultApiClient();
//        // Configure API key authorization: Apikey
//        ApiKeyAuth Apikey = (ApiKeyAuth) defaultClient.getAuthentication("Apikey");
//        Apikey.setApiKey("e7af652d-99ee-4dc2-be74-2980e60445b4");
//// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
////Apikey.setApiKeyPrefix("Token");
//
//        BarcodeLookupApi apiInstance = new BarcodeLookupApi();
//         // String | Barcode value
//           try {
//               BarcodeLookupResponse response = apiInstance.barcodeLookupEanLookup(contents);
//               String responseText = response.toString();
//               textView5.setText(responseText);
//           } catch (ApiException e) {
//               Snackbar.make(main, "Помилка авторізації!" + e.getMessage(), Snackbar.LENGTH_LONG).show();
//           }
//    }
}