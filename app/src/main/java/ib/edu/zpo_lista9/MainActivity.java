package ib.edu.zpo_lista9;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends Activity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isRunning = false;
    private TextView textViewAXValue;
    private TextView textViewAYValue;
    private TextView textViewAZValue;
    private TextView textViewSteps;
    private PowerManager powerManager;
    private PowerManager.WakeLock myWakeLock;
    private XYSeries seriesX;
    private XYSeries seriesY;
    private XYSeries seriesZ;
    private XYSeriesRenderer rendered;
    private XYMultipleSeriesRenderer mrendered;
    private XYMultipleSeriesDataset datasetX;
    private XYMultipleSeriesDataset datasetY;
    private XYMultipleSeriesDataset datasetZ;
    private LinearLayout chartLayoutX;
    private LinearLayout chartLayoutY;
    private LinearLayout chartLayoutZ;
    private GraphicalView chartView;
    private int counter;

    private AccelerationData accelerationData;

    private static final int MY_PERMISSIONS_REQUEST_WRITE = 10;
    private boolean canWriteToFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accelerationData = new AccelerationData();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        seriesX = new XYSeries("X");
        seriesY = new XYSeries("Y");
        seriesZ = new XYSeries("Z");

        rendered = new XYSeriesRenderer();
        rendered.setLineWidth(2);
        rendered.setColor(Color.BLUE);
        rendered.setPointStyle(PointStyle.CIRCLE);

        mrendered = new XYMultipleSeriesRenderer();
        mrendered.addSeriesRenderer(rendered);
        mrendered.setShowGrid(true);

        datasetX = new XYMultipleSeriesDataset();
        datasetY = new XYMultipleSeriesDataset();
        datasetZ = new XYMultipleSeriesDataset();

        chartLayoutX = (LinearLayout) findViewById(R.id.llv);
        chartLayoutY = (LinearLayout) findViewById(R.id.llvY);
        chartLayoutZ = (LinearLayout) findViewById(R.id.llvZ);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        myWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:test");
        textViewAXValue = (TextView) findViewById(R.id.textViewAXValue);
        textViewAYValue = (TextView) findViewById(R.id.textViewAYValue);
        textViewAZValue = (TextView) findViewById(R.id.textViewAZValue);
        textViewSteps = (TextView) findViewById(R.id.textViewSteps);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canWriteToFile = true;
                } else {
                    canWriteToFile = false;
                }
                return;
            }
        }
    }


    public void onClick(View view) {

        Log.d(TAG, "Button  pressed");
        isRunning = !isRunning;

        if (isRunning) myWakeLock.acquire();
        else myWakeLock.release();

        if (!isRunning) {
            Toast.makeText(getApplicationContext(), "Kroki = " + accelerationData.countSteps(), Toast.LENGTH_LONG).show();
            textViewSteps.setText(getResources().getString(R.string.steps) + accelerationData.countSteps());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isRunning) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float aX = event.values[0]; //skladowa x wektora przyspieszenia
                float aY = event.values[1];
                float aZ = event.values[2];
                float timeStamp = event.timestamp; //czas w ns
                Log.d(TAG, "przyspieszenie aX = " + Float.toString(aX) + " timeStamp = " + Float.toString(timeStamp));

                accelerationData.addData(aX, aY, aZ);

                textViewAXValue.setText(Float.toString(aX));
                textViewAYValue.setText(Float.toString(aY));
                textViewAZValue.setText(Float.toString(aZ));

                counter++;

                //wykres ax
                seriesX.add(counter, aX);
                datasetX.clear();
                datasetX.addSeries(seriesX);
                chartView = ChartFactory.getLineChartView(this, datasetX, mrendered);
                chartLayoutX.removeAllViews();
                chartLayoutX.addView(chartView);

                //wykres ay
                seriesY.add(counter, aY);
                datasetY.clear();
                datasetY.addSeries(seriesY);
                chartView = ChartFactory.getLineChartView(this, datasetY, mrendered);
                chartLayoutY.removeAllViews();
                chartLayoutY.addView(chartView);

                //wykres az
                seriesZ.add(counter, aZ);
                datasetZ.clear();
                datasetZ.addSeries(seriesZ);
                chartView = ChartFactory.getLineChartView(this, datasetZ, mrendered);
                chartLayoutZ.removeAllViews();
                chartLayoutZ.addView(chartView);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void onClickSave(View view) {
        saveToFile("/TEST/", "acceleration_data.txt");
    }


    private void saveToFile(String folder, String fileName) {

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + folder);
        dir.mkdirs();
        File file = new File(dir, fileName);

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);

            for (int i = 0; i < counter - 1; i++) {
                pw.println(seriesX.getY(i) + " " + seriesY.getY(i) + " " + seriesZ.getY(i));
            }

            pw.flush();
            pw.close();
            f.close();

            Toast.makeText(getApplicationContext(), "Data saved", Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("My", "******* File not found *********");

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "You don't have WRITE permission", Toast.LENGTH_LONG).show();
        }
    }
}

