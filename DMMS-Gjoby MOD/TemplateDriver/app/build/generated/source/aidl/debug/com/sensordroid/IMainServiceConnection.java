/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/viettt/Desktop/DMMS-master/TemplateDriver/app/src/main/aidl/com/sensordroid/IMainServiceConnection.aidl
 */
package com.sensordroid;
// Declare any non-default types here with import statements

public interface IMainServiceConnection extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.sensordroid.IMainServiceConnection
{
private static final java.lang.String DESCRIPTOR = "com.sensordroid.IMainServiceConnection";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.sensordroid.IMainServiceConnection interface,
 * generating a proxy if needed.
 */
public static com.sensordroid.IMainServiceConnection asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.sensordroid.IMainServiceConnection))) {
return ((com.sensordroid.IMainServiceConnection)iin);
}
return new com.sensordroid.IMainServiceConnection.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_putJson:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.putJson(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.sensordroid.IMainServiceConnection
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void putJson(java.lang.String json) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(json);
mRemote.transact(Stub.TRANSACTION_putJson, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_putJson = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void putJson(java.lang.String json) throws android.os.RemoteException;
}
