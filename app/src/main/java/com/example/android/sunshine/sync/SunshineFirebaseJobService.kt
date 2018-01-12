/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.sync

import android.os.AsyncTask
import com.firebase.jobdispatcher.Job
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.firebase.jobdispatcher.RetryStrategy
import io.reactivex.disposables.CompositeDisposable


class SunshineFirebaseJobService : JobService() {

    private var mFetchWeatherTask: AsyncTask<Void, Void, Void>? = null
    private val compositeDisposable:CompositeDisposable = CompositeDisposable()

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     *
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */
    override fun onStartJob(jobParameters: JobParameters): Boolean {

        mFetchWeatherTask = object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                val context = applicationContext
                SunshineSyncTask.syncWeather(context,compositeDisposable)
                jobFinished(jobParameters, false)
                return null
            }

            override fun onPostExecute(aVoid: Void) {
                jobFinished(jobParameters, false)
            }
        }

        mFetchWeatherTask!!.execute()
        return true
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return whether the job should be retried
     * @see Job.Builder.setRetryStrategy
     * @see RetryStrategy
     */
    override fun onStopJob(jobParameters: JobParameters): Boolean {
        if (mFetchWeatherTask != null) {
            mFetchWeatherTask!!.cancel(true)
        }
        compositeDisposable.clear()
        return true
    }
}