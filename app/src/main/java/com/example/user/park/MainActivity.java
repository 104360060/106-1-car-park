package com.example.user.park;

import android.Manifest;
import android.app.DownloadManager;
import android.app.VoiceInteractor;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.service.voice.VoiceInteractionSession;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.KeyEvent;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.*;

import java.io.IOException;
import java.sql.Struct;

import javax.xml.transform.Result;

public class MainActivity extends AppCompatActivity {

    EditText editName;
    Button search,back,bike,scooter,car;
    SQLiteDatabase dbrw;

    class Data {
        Result result;
        class Result {
            Results[] results;
            class Results
            {
                String 停車場名稱;
                @SerializedName("經度(WGS84)")
                String 經度WGS84;
                @SerializedName("緯度(WGS84)")
                String 緯度WGS84;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editName = (EditText) findViewById(R.id.editName);
        search = (Button) findViewById(R.id.search);
        back = (Button)findViewById(R.id.back);
        bike = (Button)findViewById(R.id.bike);
        car = (Button)findViewById(R.id.car);
        scooter = (Button)findViewById(R.id.scooter);




        OkHttpClient mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=a880adf3-d574-430a-8e29-3192a41897a5").build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Intent i = new Intent("MyMessage");
                i.putExtra("json", response.body().string());
                sendBroadcast(i);

            }
        });

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String myJson = intent.getExtras().getString("json");
                Gson gson = new Gson();
                final Data data = gson.fromJson(myJson, Data.class);

                int a=0,b=0,c=0;
                final String[] bikeData = new String[124];
                final String[] bikeLon = new String[124];
                final String[] bikeLat = new String[124];
                final String[] carData = new String[24];
                final String[] carLon = new String[24];
                final String[] carLat = new String[24];
                final String[] scooterData = new String[38];
                final String[] scooterLon = new String[38];
                final String[] scooterLat = new String[38];

                for (int i = 0;i< data.result.results.length;i++)
                {
                    if(data.result.results[i].停車場名稱.indexOf("自行車")>-1 || data.result.results[i].停車場名稱.indexOf("踏")>-1)
                    {
                        bikeData[a] = data.result.results[i].停車場名稱;
                        bikeLon[a] = data.result.results[i].經度WGS84;
                        bikeLat[a] = data.result.results[i].緯度WGS84;
                        a++ ;
                    }
                    else if( data.result.results[i].停車場名稱.indexOf("機車")>-1)
                    {
                        scooterData[c] = data.result.results[i].停車場名稱;
                        scooterLon[c] = data.result.results[i].經度WGS84;
                        scooterLat[c] = data.result.results[i].緯度WGS84;
                        c++;
                    }
                    else if(data.result.results[i].停車場名稱.indexOf("轉乘")>-1 || data.result.results[i].停車場名稱.indexOf("廠")>-1)
                    {
                        carData[b] =  data.result.results[i].停車場名稱;
                        carLon[b] = data.result.results[i].經度WGS84;
                        carLat[b] = data.result.results[i].緯度WGS84;
                        b++;

                    }
                    else
                        System.out.println(data.result.results[i].停車場名稱);
                }
                System.out.println(c);
                refreshMaap(data); //初始化地圖副程式

                search.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        searchPark(data);
                    }
                });
                bike.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(new OnMapReadyCallback()
                        {
                            @Override
                            public void onMapReady(GoogleMap googleMap)
                            {
                                googleMap.clear();
                                for (int i=0 ;i<124;i++)
                                {
                                    MarkerOptions M = new MarkerOptions();
                                    double y =Double.parseDouble(bikeLon[i]) ;
                                    double z =Double.parseDouble(bikeLat[i]);
                                    M.position(new LatLng(z,y));
                                    M.title(bikeData[i]);
                                    M.draggable(false);
                                    googleMap.addMarker(M);
                                }
                            }
                        });
                    }
                });
                car.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(new OnMapReadyCallback()
                        {
                            @Override
                            public void onMapReady(GoogleMap googleMap)
                            {
                                googleMap.clear();
                                for (int i=0 ;i<24;i++)
                                {
                                    MarkerOptions M = new MarkerOptions();
                                    double y =Double.parseDouble(carLon[i]) ;
                                    double z =Double.parseDouble(carLat[i]);
                                    M.position(new LatLng(z,y));
                                    M.title(carData[i]);
                                    M.draggable(false);
                                    googleMap.addMarker(M);
                                }
                            }
                        });
                    }
                });
                scooter.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(new OnMapReadyCallback()
                        {
                            @Override
                            public void onMapReady(GoogleMap googleMap)
                            {
                                googleMap.clear();
                                for (int i=0 ;i<38;i++)
                                {
                                    MarkerOptions M = new MarkerOptions();
                                    double y =Double.parseDouble(scooterLon[i]) ;
                                    double z =Double.parseDouble(scooterLat[i]);
                                    M.position(new LatLng(z,y));
                                    M.title(scooterData[i]);
                                    M.draggable(false);
                                    googleMap.addMarker(M);
                                }

                            }
                        });
                    }
                });
            };
        };
        IntentFilter intentFilter = new IntentFilter("MyMessage");
        registerReceiver(broadcastReceiver, intentFilter);

        BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String myJson = intent.getExtras().getString("json");
                Gson gson = new Gson();
                final Data data = gson.fromJson(myJson, Data.class);

                back.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        refreshMaap(data);
                    }
                });
            }
        };
        IntentFilter intentFilter2 = new IntentFilter("MyMessage");
        registerReceiver(broadcastReceiver2, intentFilter2);
    }



    public void refreshMaap(final Data data)
    {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap googleMap)
            {
                for(int i = 0; i<data.result.results.length; i++)
                {
                    MarkerOptions M = new MarkerOptions();
                    String l = new String();

                    double a =Double.parseDouble(data.result.results[i].經度WGS84) ;
                    double b =Double.parseDouble(data.result.results[i].緯度WGS84);
                    l = data.result.results[i].停車場名稱;

                    M.position(new LatLng(b,a));
                    M.title(l);
                    M.draggable(false);
                    googleMap.addMarker(M);
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.066668, 121.510465), 11));
            }
        });
    }
    public void searchPark(Data d)
    {

        for(int i = 0;i< d.result.results.length;i++)
        {
            if (editName.getText().toString().equals(d.result.results[i].停車場名稱))
            {

                final double a =Double.parseDouble(d.result.results[i].經度WGS84) ;
                final double b =Double.parseDouble(d.result.results[i].緯度WGS84);
                final String l = d.result.results[i].停車場名稱;
                final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(new OnMapReadyCallback()
                {

                    @Override
                    public void onMapReady(GoogleMap googleMap)
                    {

                        googleMap.clear();

                        MarkerOptions M = new MarkerOptions();
                        M.title(l);
                        M.position(new LatLng(b,a));
                        googleMap.addMarker(M);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(b,a ), 13));
                    }
                });
                break;
            }
            else if(i==185)
                Toast.makeText(this,"查無資料",Toast.LENGTH_SHORT).show();

       }
    }
}