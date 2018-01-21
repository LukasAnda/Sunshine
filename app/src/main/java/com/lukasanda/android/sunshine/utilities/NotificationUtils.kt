package com.lukasanda.android.sunshine.utilities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import com.lukasanda.android.sunshine.DetailActivity
import com.lukasanda.android.sunshine.R
import com.lukasanda.android.sunshine.data.Data
import com.lukasanda.android.sunshine.data.SunshinePreferences

object NotificationUtils {


    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 3004 is in no way significant.
     */
    private val WEATHER_NOTIFICATION_ID = 3004

    /**
     * Constructs and displays a notification for the newly updated weather for today.
     *
     * @param context Context used to query our ContentProvider and use various Utility methods
     */
    fun notifyUserOfNewWeather(context: Context, day: Data?) {
        /*
         * If todayWeatherCursor is empty, moveToFirst will return false. If our cursor is not
         * empty, we want to show the notification.
         */
        if (day != null) {

            /* Weather ID as returned by API, used to identify the icon to be used */
            val weatherId = day.weather?.code?.toInt()
            val high = day.max_temp
            val low = day.min_temp

            val resources = context.resources
            val largeArtResourceId = SunshineWeatherUtils
                    .getLargeArtResourceIdForWeatherCondition(weatherId)

            val largeIcon = BitmapFactory.decodeResource(
                    resources,
                    largeArtResourceId)

            val notificationTitle = context.getString(R.string.app_name)

            val notificationText = getNotificationText(context, weatherId, high, low)

            /* getSmallArtResourceIdForWeatherCondition returns the proper art to show given an ID */
            val smallArtResourceId = SunshineWeatherUtils
                    .getSmallArtResourceIdForWeatherCondition(weatherId)

            /*
             * NotificationCompat Builder is a very convenient way to build backward-compatible
             * notifications. In order to use it, we provide a context and specify a color for the
             * notification, a couple of different icons, the title for the notification, and
             * finally the text of the notification, which in our case in a summary of today's
             * forecast.
             */
            val notificationBuilder = NotificationCompat.Builder(context)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setAutoCancel(true)

            /*
             * This Intent will be triggered when the user clicks the notification. In our case,
             * we want to open Sunshine to the DetailActivity to display the newly updated weather.
             */
            val detailIntentForToday = Intent(context, DetailActivity::class.java)
            detailIntentForToday.putExtra("date",day.datetime)

            val taskStackBuilder = TaskStackBuilder.create(context)
            taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday)
            val resultPendingIntent = taskStackBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

            notificationBuilder.setContentIntent(resultPendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            /* WEATHER_NOTIFICATION_ID allows you to update or cancel the notification later on */
            notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build())

            /*
             * Since we just showed a notification, save the current time. That way, we can check
             * next time the weather is refreshed if we should show another notification.
             */
            SunshinePreferences.saveLastNotificationTime(context, System.currentTimeMillis())
        }
    }

    /**
     * Constructs and returns the summary of a particular day's forecast using various utility
     * methods and resources for formatting. This method is only used to create the text for the
     * notification that appears when the weather is refreshed.
     *
     *
     * The String returned from this method will look something like this:
     *
     *
     * Forecast: Sunny - High: 14°C Low 7°C
     *
     * @param context   Used to access utility methods and resources
     * @param weatherId ID as determined by Open Weather Map
     * @param high      High temperature (either celsius or fahrenheit depending on preferences)
     * @param low       Low temperature (either celsius or fahrenheit depending on preferences)
     * @return Summary of a particular day's forecast
     */
    private fun getNotificationText(context: Context, weatherId: Int?, high: Double, low: Double): String {

        /*
         * Short description of the weather, as provided by the API.
         * e.g "clear" vs "sky is clear".
         */
        val shortDescription = SunshineWeatherUtils
                .getStringForWeatherCondition(context, weatherId)

        val notificationFormat = context.getString(R.string.format_notification)

        /* Using String's format method, we create the forecast summary */

        return String.format(notificationFormat,
                shortDescription,
                SunshineWeatherUtils.formatTemperature(context, high),
                SunshineWeatherUtils.formatTemperature(context, low))
    }
}
