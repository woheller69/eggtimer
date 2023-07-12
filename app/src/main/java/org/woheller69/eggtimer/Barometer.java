package org.woheller69.eggtimer;


import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import androidx.preference.PreferenceManager;

public class Barometer {

        private static SensorEventListener sensorListener;
        private static SensorManager sensorManager;
        private static double boilingTemp=100;

        public static double getBoilingTemp() {
            return boilingTemp;
        }

        static void stopPressureSensor(Context context){
            if (sensorManager != null) {
                sensorManager.unregisterListener(sensorListener);
            }
            sensorListener=null;
        }

        static void requestPressure(Context context, TextView altitudeTextView) {
                sensorManager = (SensorManager) context.getSystemService(Service.SENSOR_SERVICE);
                Sensor barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
                if (sensorListener == null) sensorListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                            float[] values = event.values;
                            boilingTemp = calcBoilingTemp(values[0]);
                            altitudeTextView.setText((int)values[0]+ "\u2009" + context.getString(R.string.unit_mbar));
                            altitudeTextView.setTextColor(MainActivity.getThemeColor(context,R.attr.colorOnPrimaryContainer));
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int i) {

                    }
                };
                sensorManager.registerListener(sensorListener, barometer, SensorManager.SENSOR_DELAY_NORMAL);

            }

            public static boolean hasSensor (Context context){
                sensorManager = (SensorManager) context.getSystemService(Service.SENSOR_SERVICE);
                Sensor barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
                return barometer != null;
            }


            public static double calcBoilingTemp(double pressure){
                //Data are based on the equation of state recommended by the International Association for the Properties of Steam, as presented in Haar, Gallagher, and Kell, NBS-NRC Steam Tables (Hemisphere Publishing Corp., New York, 1984).
                double[][] presBoil = {{	50	,	32.88	}	,
                                        {	100	,	45.82	}	,
                                        {	150	,	53.98	}	,
                                        {	200	,	60.07	}	,
                                        {	250	,	64.98	}	,
                                        {	300	,	69.11	}	,
                                        {	350	,	72.7	}	,
                                        {	400	,	75.88	}	,
                                        {	450	,	78.74	}	,
                                        {	500	,	81.34	}	,
                                        {	550	,	83.73	}	,
                                        {	600	,	85.95	}	,
                                        {	650	,	88.02	}	,
                                        {	700	,	89.96	}	,
                                        {	750	,	91.78	}	,
                                        {	800	,	93.51	}	,
                                        {	850	,	95.15	}	,
                                        {	900	,	96.71	}	,
                                        {	905	,	96.87	}	,
                                        {	910	,	97.02	}	,
                                        {	915	,	97.17	}	,
                                        {	920	,	97.32	}	,
                                        {	925	,	97.47	}	,
                                        {	930	,	97.62	}	,
                                        {	935	,	97.76	}	,
                                        {	940	,	97.91	}	,
                                        {	945	,	98.06	}	,
                                        {	950	,	98.21	}	,
                                        {	955	,	98.35	}	,
                                        {	960	,	98.5	}	,
                                        {	965	,	98.64	}	,
                                        {	970	,	98.78	}	,
                                        {	975	,	98.93	}	,
                                        {	980	,	99.07	}	,
                                        {	985	,	99.21	}	,
                                        {	990	,	99.35	}	,
                                        {	995	,	99.49	}	,
                                        {	1000	,	99.63	}	,
                                        {	1005	,	99.77	}	,
                                        {	1010	,	99.91	}	,
                                        {	1013.25	,	100	}	,
                                        {	1015	,	100.05	}	,
                                        {	1020	,	100.19	}	,
                                        {	1025	,	100.32	}	,
                                        {	1030	,	100.46	}	,
                                        {	1035	,	100.6	}	,
                                        {	1040	,	100.73	}	,
                                        {	1045	,	100.87	}	,
                                        {	1050	,	101	}	,
                                        {	1055	,	101.14	}	,
                                        {	1060	,	101.27	}	,
                                        {	1065	,	101.4	}	,
                                        {	1070	,	101.54	}	,
                                        {	1075	,	101.67	}	,
                                        {	1080	,	101.8	}	,
                                        {	1085	,	101.93	}	,
                                        {	1090	,	102.06	}	,
                                        {	1095	,	102.19	}	,
                                        {	1100	,	102.32	}	,
                                        {	1150	,	103.59	}	,
                                        {	1200	,	104.81	}	,
                                        {	1250	,	105.99	}	,
                                        {	1300	,	107.14	}	,
                                        {	1350	,	108.25	}	,
                                        {	1400	,	109.32	}	,
                                        {	1450	,	110.36	}	,
                                        {	1500	,	111.38	}	,
                                        {	1550	,	112.37	}	,
                                        {	1600	,	113.33	}	,
                                        {	1650	,	114.26	}	,
                                        {	1700	,	115.18	}	,
                                        {	1750	,	116.07	}	,
                                        {	1800	,	116.94	}	,
                                        {	1850	,	117.79	}	,
                                        {	1900	,	118.63	}	,
                                        {	1950	,	119.44	}	,
                                        {	2000	,	120.24	}	,
                                        {	2050	,	121.02	}	,
                                        {	2100	,	121.79	}	,
                                        {	2150	,	122.54	}	};
                int i;
                for (i = 0; i < presBoil.length; i++) {
                    if (presBoil[i][0]>pressure)
                        break;
                }
                double boilTemp;
                if (i==0) boilTemp = presBoil[i][1];
                else{
                    double factor = (pressure-presBoil[i-1][0])/(presBoil[i][0] - presBoil[i-1][0]);
                    boilTemp = presBoil[i-1][1]+factor*(presBoil[i][1]-presBoil[i-1][1]);
                }
                return boilTemp;
            }

    }
