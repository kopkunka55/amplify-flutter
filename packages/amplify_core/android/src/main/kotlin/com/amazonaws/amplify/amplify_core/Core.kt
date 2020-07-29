/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.amplify.amplify_core

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import com.amplifyframework.AmplifyException
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.json.JSONObject
import java.io.Serializable


/** Core */
public class Core : FlutterPlugin, ActivityAware, MethodCallHandler {

    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private var mainActivity: Activity? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "com.amazonaws.amplify/core")
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.applicationContext;
        Log.i("Amplify Flutter", "Added Core plugin")
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.amazonaws.amplify/core")
            Log.i("Amplify Flutter", "Added Core plugin")
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "configure" -> 
                try {
                    onConfigure(result, (call.arguments as HashMap<*, *>)["configuration"] as String)
                }
                catch (e: Exception) {
                    result.error("AmplifyException", "Error casting configuration map", e.message )
                }
            else -> result.notImplemented()
        }
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.mainActivity = binding.activity
    }

    override fun onDetachedFromActivity() {
        this.mainActivity = null
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun onConfigure(@NonNull result: Result, @NonNull config: String) {
        try {
            Amplify.configure(AmplifyConfiguration.fromJson(JSONObject(config)), context);
            result.success(true);
        } catch (e: AmplifyException) {
            result.error("AmplifyException", e.message, formatAmplifyException(e) )
        }
    }

    private fun formatAmplifyException(@NonNull e: AmplifyException): Map<String, Serializable?> {
        return mapOf(
            "cause" to e.cause,
            "recoverySuggestion" to e.recoverySuggestion
        )
    }
}
