/**
 * LocationService.java
 * 
 * Created by Xiaochao Yang on Sep 11, 2011 4:50:19 PM
 * 
 */

package com.michaeltroger.prediction1;

import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import com.google.common.util.concurrent.AtomicDouble;
import com.meapsoft.FFT;

public class SensorsService extends Service implements SensorEventListener {

	private static final int mFeatLen = Globals.ACCELEROMETER_BLOCK_CAPACITY + 2;
	
	private File mFeatureFile;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private int mServiceTaskType;
	private String mLabel;
	private FeatureVectorTask featureVectorTask;
	private static final String[] LABELS = {
            Globals.CLASS_LABEL_STANDING,
            Globals.CLASS_LABEL_WALKING,
            Globals.CLASS_LABEL_RUNNING,
            Globals.CLASS_LABEL_OTHER
	};

    private AtomicDouble cachedAccValue = new AtomicDouble();
	private static ArrayBlockingQueue<Double> mAccBuffer;
	public static final DecimalFormat mdf = new DecimalFormat("#.##");
    private SamplingTask samplingTask;

    @Override
	public void onCreate() {
		super.onCreate();

		mAccBuffer = new ArrayBlockingQueue<>(Globals.ACCELEROMETER_BUFFER_CAPACITY);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

		final Bundle extras = intent.getExtras();
		mLabel = extras.getString(Globals.CLASS_LABEL_KEY);

		mServiceTaskType = Globals.SERVICE_TASK_TYPE_COLLECT;

		final Intent i = new Intent(this, CollectorActivity.class);
		// Read:
		// http://developer.android.com/guide/topics/manifest/activity-element.html#lmode
		// IMPORTANT!. no re-create activity
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		final PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

		final Notification notification = new Notification.Builder(this)
				.setContentTitle(
						getApplicationContext().getString(
								R.string.ui_sensor_service_notification_title))
				.setContentText(
						getResources()
								.getString(
										R.string.ui_sensor_service_notification_content))
				.setSmallIcon(R.drawable.greend).setContentIntent(pi).build();
		final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(0, notification);


		featureVectorTask = new FeatureVectorTask();
		featureVectorTask.execute();
        samplingTask = new SamplingTask();
        samplingTask.execute();

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		featureVectorTask.cancel(true);
        samplingTask.cancel(true);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mSensorManager.unregisterListener(this);
		super.onDestroy();

	}

    private class SamplingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            long startTimeMillis = System.currentTimeMillis();

            while (true) {
                if (isCancelled()) {
                    return null;
                }

                final long currentTimeMillis = System.currentTimeMillis();

                if (currentTimeMillis >= startTimeMillis + Globals.SAMPLING_RATE_MILLIS) {
                    startTimeMillis = currentTimeMillis;
                    final double cachedValue = cachedAccValue.get();
                    try {
                        mAccBuffer.add(cachedValue);
                    } catch (IllegalStateException e) {
                        final ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<>(mAccBuffer.size() * 2);

                        mAccBuffer.drainTo(newBuf);
                        mAccBuffer = newBuf;
                        mAccBuffer.add(cachedValue);
                    }
                }
            }
        }
    }

	private class FeatureVectorTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {

			int blockSize = 0;
			int blockSize2 = 0;

			final FFT fft = new FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY);

			final double[] accBlock = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			final double[] re = accBlock;
			final double[] im = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];

			final double[] accBlock2 = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			final double[] re2 = accBlock2;
			final double[] im2 = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];

			final int ACCELEROMETER_HALF_BLOCK_CAPACITY = Globals.ACCELEROMETER_BLOCK_CAPACITY / 2;
			boolean isInitial = true;

			double max = Double.MIN_VALUE;
			double max2 = Double.MIN_VALUE;

			final Double[] featureVector = new Double[Globals.ACCELEROMETER_BLOCK_CAPACITY+1];
			final Double[] featureVector2 = new Double[Globals.ACCELEROMETER_BLOCK_CAPACITY+1];

			while (true) {
				try {
					if (isCancelled()) {
				        return null;
				    }

					// Dumping buffer
					double buffer = mAccBuffer.take();
					accBlock[blockSize++] = buffer;

					if (!isInitial) {
						accBlock2[blockSize2++] = buffer;
					}

					if (isInitial && blockSize == ACCELEROMETER_HALF_BLOCK_CAPACITY) {
						isInitial = false;
					}

					if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
						blockSize = 0;

						// time = System.currentTimeMillis();
						max = .0;
						for (final double val : accBlock) {
							if (max < val) {
								max = val;
							}
						}

						fft.fft(re, im);

						for (int i = 0; i < re.length; i++) {
							double mag = Math.sqrt(re[i] * re[i] + im[i] * im[i]);
							featureVector[i] = mag;
							im[i] = .0; // Clear the field
						}

						// Append max after frequency component
						featureVector[Globals.ACCELEROMETER_BLOCK_CAPACITY] = max;
						final Double p = WekaClassifier.classify(featureVector);

						final int indexDetectedActivity = p.intValue();

						//Put your all data using put extra
						final Intent broadcastIntent = new Intent();
						broadcastIntent.putExtra("key", LABELS[indexDetectedActivity]);
						broadcastIntent.setAction(Globals.ACTION_NAME);
						sendBroadcast(broadcastIntent);

					} else if (blockSize2 == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
						blockSize2 = 0;

						// time = System.currentTimeMillis();
						max2 = .0;
						for (final double val : accBlock2) {
							if (max < val) {
								max = val;
							}
						}

						fft.fft(re2, im2);

						for (int i = 0; i < re2.length; i++) {
							double mag = Math.sqrt(re2[i] * re2[i] + im2[i] * im2[i]);
							featureVector2[i] = mag;
							im2[i] = .0; // Clear the field
						}

						// Append max after frequency component
						featureVector2[Globals.ACCELEROMETER_BLOCK_CAPACITY] = max2;
						final Double p = WekaClassifier.classify(featureVector);

						final int indexDetectedActivity = p.intValue();

						//Put your all data using put extra
						final Intent broadcastIntent = new Intent();
						broadcastIntent.putExtra("key", LABELS[indexDetectedActivity]);
						broadcastIntent.setAction(Globals.ACTION_NAME);
						sendBroadcast(broadcastIntent);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			cachedAccValue.set(Math.sqrt(
					event.values[0] * event.values[0]
					+ event.values[1] * event.values[1]
					+ event.values[2] * event.values[2]
			));
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
