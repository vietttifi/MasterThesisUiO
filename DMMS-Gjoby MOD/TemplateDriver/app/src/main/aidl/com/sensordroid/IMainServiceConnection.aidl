// IMainServiceConnection.aidl
package com.sensordroid;

// Declare any non-default types here with import statements

interface IMainServiceConnection {
    oneway void putJson(in String json);
}
