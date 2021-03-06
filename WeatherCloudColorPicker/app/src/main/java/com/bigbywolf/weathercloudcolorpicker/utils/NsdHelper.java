/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.bigbywolf.weathercloudcolorpicker.utils;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;
import android.util.Log;

import java.util.ArrayList;

public class NsdHelper {

    Context mContext;

    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;

    private ArrayList<NsdServiceInfo> foundServices = null;

    Object lock = null;


    public static final String SERVICE_TYPE = "_http._tcp.";
//    private static final String SERVICE_TYPE = "_services._dns-sd._udp";
//    private static final String SERVICE_TYPE = "_tcp.local.";

    public static final String TAG = "NsdHelper";
//    public String mServiceName = "_esp";
    public String mServiceName = "Canon MX920 series";

    NsdServiceInfo mService;

    public NsdHelper(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeNsd() {

        foundServices = new ArrayList<>();

        initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();
        lock = new Object();
        //mNsdManager.init(mContext.getMainLooper(), this);

    }

    public void initializeDiscoveryListener() {

        foundServices.clear();

        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success " + service);

                foundServices.add(service);
//                if (!service.getServiceType().equals(SERVICE_TYPE)) {
//                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType() + " " + service.getServiceName());
//                }
//                else if (service.getServiceName().equals(mServiceName)) {
//                    Log.d(TAG, "Same machine: " + mServiceName);
//                }
//                else if (service.getServiceName().contains(mServiceName)){
//                    Log.d(TAG, "service found");
//                    mNsdManager.resolveService(service, mResolveListener);
//                }
//                 if (service.getServiceName().contains(mServiceName)){
//                    Log.d(TAG, "service found");
//                    mNsdManager.resolveService(service, mResolveListener);
//                }
//                else{
//                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType() + " " + service.getServiceName());
//                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                if (mService == service) {
                    mService = null;
                }
            }
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);        
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                synchronized (lock) {
                    Log.e(TAG, "Resolve failed " + errorCode);
                    mService = null;
                    lock.notify();
                }
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                synchronized (lock) {
                    Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

//                if (serviceInfo.getServiceName().equals(mServiceName)) {
//                    Log.d(TAG, "Same IP.");
//                    return;
//                }
                    mService = serviceInfo;
                    lock.notify();
                }
            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
            }
            
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }
            
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }
            
        };
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        
    }

    public void discoverServices() {
        foundServices.clear();
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public NsdServiceInfo resolveFoundService(NsdServiceInfo service){
        synchronized (lock){

            try {
                mNsdManager.resolveService(service, mResolveListener);
                lock.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return mService;
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }

    public ArrayList<NsdServiceInfo> getServiceList() { return foundServices; }
    
    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
    }
}
