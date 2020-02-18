package com.example.sih2.ui.driving;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sih2.R;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DrivingFragment extends Fragment implements SensorEventListener, LocationListener {
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 12;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private Button start;
    private Button stop;
    private TextView accx;
    private TextView accy;
    private TextView accz;
    private TextView gyrox;
    private TextView gyroy;
    private TextView gyroz;
    private TextView lont;
    private TextView latd;
    private TextView speed;
    private TextView ts;
    private String res;
    private double accx1;
    private double accy1;
    private double accz1;
    private double gyrox1;
    private double gyroy1;
    private double gyroz1;
    private double lont1;
    private double latd1;
    private double speed1;
    private double ts1;
    private int i;
    JSONObject po;
    String po1;
    private DatabaseReference mDataBase;
    private double accx10[], accy10[], accz10[], gyrox10[], gyroy10[], gyroz10[], speed10[], latd10[], lont10[];

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);

        accx10 = new double[20];
        accy10 = new double[20];
        accz10 = new double[20];
        gyrox10 = new double[20];
        gyroy10 = new double[20];
        gyroz10 = new double[20];
        latd10 = new double[20];
        lont10 = new double[20];
        speed10 = new double[20];

        i = 0;
        View root = inflater.inflate(R.layout.fragment_driving, container, false);
        accx = root.findViewById(R.id.accx);
        accy = root.findViewById(R.id.accy);
        accz = root.findViewById(R.id.accz);
        gyrox = root.findViewById(R.id.gyrox);
        gyroy = root.findViewById(R.id.gyroy);
        gyroz = root.findViewById(R.id.gyroz);
        latd = root.findViewById(R.id.lat);
        lont = root.findViewById(R.id.lon);
        speed = root.findViewById(R.id.speed);
        start=root.findViewById(R.id.button_start);
        stop=root.findViewById(R.id.button_stop);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.start();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
            }
        });
        return root;
    }
    CountDownTimer countDownTimer = new CountDownTimer(10000, 500) {

        public void onTick(long millisUntilFinished) {
            latd.setText(String.valueOf(latd1));
            lont.setText(String.valueOf(lont1));
            speed.setText(String.valueOf(speed1));
            accx.setText(String.valueOf(accx1));
            accy.setText(String.valueOf(accy1));
            accz.setText(String.valueOf(accz1));
            gyrox.setText(String.valueOf(gyrox1));
            gyroy.setText(String.valueOf(gyroy1));
            gyroz.setText(String.valueOf(gyroz1));

            if (i < 20) {
                accx10[i] = accx1;
                accy10[i] = accy1;
                accz10[i] = accz1;
                gyrox10[i] = gyrox1;
                gyroy10[i] = gyroy1;
                gyroz10[i] = gyroz1;
                latd10[i] = latd1;
                lont10[i] = lont1;
                speed10[i] = speed1;
                if (i == 19)
                    senddatatoserver();
                i++;

            } else {
                i = 0;
            }

        }

        public void onFinish() {
            Log.d(DrivingFragment.class.getName(), "In onFinish");
            countDownTimer.start();
        }
    };

    @Override
    public void onLocationChanged(Location location) {
        latd1 = location.getLatitude();
        lont1 = location.getLongitude();
        speed1 = location.getSpeed();
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            getGyroscope(event);
        }

    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];
        accx1 = x;
        accy1 = y;
        accz1 = z;

    }

    private void getGyroscope(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];
        gyrox1 = x;
        gyroy1 = y;
        gyroz1 = z;


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onResume() {

        super.onResume();
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION
            );
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION
            );
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 0, this);

    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
    }

    public void senddatatoserver() {

        JSONObject post_dict = new JSONObject();
        String s="[";
        String x;
        try {
            for (int i = 0; i < 20; i++) {
                post_dict.put("accx", accx10[i]);
                post_dict.put("accy", accy10[i]);
                post_dict.put("accz", accz10[i]);
                post_dict.put("gyrx", gyrox10[i]);
                post_dict.put("gyry", gyroy10[i]);
                post_dict.put("gyrz", gyroz10[i]);
                post_dict.put("longitude", lont10[i]);
                post_dict.put("latitude", latd10[i]);
                post_dict.put("speed", speed10[i]);
                x=post_dict.toString();
                s=s+x;
                if(i!=19)
                    s+=",";
                else
                    s+="]";
            }

            //po=new JSONObject(s);
            System.out.println(s);
            po1=s;
            SendJsonDataToServer s2=new SendJsonDataToServer();
            s2.execute(po1);
        } catch (JSONException e) {
            System.out.println("Blahshit");
            e.printStackTrace();
        }
    }

   // try {
       /* RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        String URL = "http://...";
        //JSONObject jsonBody = new JSONObject(po1);
        final String requestBody = po1;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        requestQueue.add(stringRequest);*/
   /* } catch (JSONException e) {
        e.printStackTrace();
    }*/

// Add the request to the RequestQueue.

    class SendJsonDataToServer extends AsyncTask<String, Void, String> {
        private final String TAG = SendJsonDataToServer.class.getName();

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Inside Pre");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            /*String postUrl= "https://blitztech-sih-2020.herokuapp.com/predict";

            String postBodyText=po1;
            MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
            RequestBody postBody = RequestBody.create(mediaType, postBodyText);

            postRequest(postUrl, postBody);
            return res;*/

            Log.d(TAG, "inside doin");
            String JsonResponse = null;
            String JsonDATA;
            JSONObject j=new JSONObject();
            try {
                j.put("name","Ram");
                j.put("dob", "2000-04-03");
                j.put("age", "18");
                j.put("email", "hdevjdh@gmail.com");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonDATA=j.toString();
            HttpURLConnection urlConnection=null;
            BufferedReader reader = null;
            try {
                URL url = new URL("https://blitztech-sih-2020.herokuapp.com/predict");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                System.out.println(urlConnection.getResponseCode());

                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/Json"); //"User-agent"
                urlConnection.setRequestProperty("Accept", "application/Json");

//set headers and method
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), UTF_8));
                writer.write(JsonDATA);

                writer.flush();
// json data
                writer.close();
                urlConnection.getOutputStream().close();

               // System.out.println(urlConnection.getResponseCode());
                //System.out.println("hitesh");
               /* InputStream inputStream = urlConnection.getInputStream();
                System.out.println("hitesh");
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    //Stream was empty. No point in parsing.
                    return null;
                }
                JsonResponse = buffer.toString();
//response data
                Log.i(TAG, JsonResponse);
                try {
//send to post execute

                    return JsonResponse;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return null;*/


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if (reader != null)
                {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
           // return JsonResponse;
               return null;
        }

        @Override
        protected void onPostExecute(String jsonObject) {
            Log.d(TAG, "inside OPS");
            super.onPostExecute(jsonObject);
            System.out.println(jsonObject + "blahblahblahblah");
        }
        //return null;
    }
   /* void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        System.out.println("Failed to Connect to Server");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            res=response.body().string();
                           // responseText.setText(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }*/
}





