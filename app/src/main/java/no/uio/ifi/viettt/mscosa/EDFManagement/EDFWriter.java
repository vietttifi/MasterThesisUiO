package no.uio.ifi.viettt.mscosa.EDFManagement;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;

public class EDFWriter{

    public static void writeEDFHeaderToFile(RandomAccessFile raf, EDFHeader header) throws IOException{
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        DecimalFormat shortFormatter = new DecimalFormat("#0.0", dfs);
        DecimalFormat longFormatter = new DecimalFormat("#0.0####", dfs);

        ByteBuffer byteBuffer = ByteBuffer.allocate(header.getBytesInHeader());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        /*Put information in the header to byte buffer*/
        putString(byteBuffer,header.getVersion(),EDFElementSize.VERSION_SIZE);
        putString(byteBuffer,header.getPatientInfo(),EDFElementSize.PATIENT_INFO_SIZE);
        putString(byteBuffer,header.getClinicInfo(),EDFElementSize.CLINIC_INFO_SIZE);
        putString(byteBuffer,header.getStartDate(),EDFElementSize.START_DATE_SIZE);
        putString(byteBuffer,header.getStartTime(),EDFElementSize.START_TIME_SIZE);
        putInt(byteBuffer,header.getBytesInHeader(),EDFElementSize.HEADER_SIZE);
        putString(byteBuffer,header.getReservedFormat(),EDFElementSize.RESERVED_FORMAT_SIZE);
        putInt(byteBuffer,header.getNumberOfRecords(),EDFElementSize.NUMBER_OF_DATA_RECORDS_SIZE);
        putDouble(byteBuffer,header.getDurationOfRecords(),EDFElementSize.DURATION_DATA_RECORDS_SIZE,longFormatter);
        putInt(byteBuffer,header.getNumberOfChannels(), EDFElementSize.NUMBER_OF_CHANNELS_SIZE);

        //for each channel, put its data to buffer
        for(int i = 0; i < header.getChannelLabels().length; i++){
            if(header.getChannelLabels()[i] != null)
                putString(byteBuffer,header.getChannelLabels()[i],EDFElementSize.LABEL_OF_CHANNEL_SIZE);
        }

        for(int i = 0; i < header.getTransducerTypes().length; i++){
            if(header.getTransducerTypes()[i] != null)
                putString(byteBuffer,header.getTransducerTypes()[i],EDFElementSize.TRANSDUCER_TYPE_SIZE);
        }

        for(int i = 0; i < header.getDimensions().length; i++){
            if(header.getDimensions()[i] != null)
                putString(byteBuffer,header.getDimensions()[i],EDFElementSize.PHYSICAL_DIMENSION_OF_CHANNEL_SIZE);
        }

        for(int i = 0; i < header.getMinInUnits().length; i++){
            if(header.getMinInUnits()[i] != null)
                putDouble(byteBuffer,header.getMinInUnits()[i],EDFElementSize.PHYSICAL_MIN_IN_UNITS_SIZE,shortFormatter);
        }

        for(int i = 0; i < header.getMaxInUnits().length; i++){
            if(header.getMaxInUnits()[i] != null)
                putDouble(byteBuffer,header.getMaxInUnits()[i],EDFElementSize.PHYSICAL_MAX_IN_UNITS_SIZE,shortFormatter);
        }

        for(int i = 0; i < header.getDigitalMin().length; i++){
            if(header.getDigitalMin()[i] != null)
                putInt(byteBuffer,header.getDigitalMin()[i],EDFElementSize.DIGITAL_MIN_SIZE);
        }

        for(int i = 0; i < header.getDigitalMax().length; i++){
            if(header.getDigitalMax()[i] != null)
                putInt(byteBuffer,header.getDigitalMax()[i],EDFElementSize.DIGITAL_MAX_SIZE);
        }

        for(int i = 0; i < header.getPrefilterings().length; i++){
            if(header.getPrefilterings()[i] != null)
                putString(byteBuffer,header.getPrefilterings()[i],EDFElementSize.PREFILTERING_SIZE);
        }

        for(int i = 0; i < header.getNumberOfSamples().length; i++){
            if(header.getNumberOfSamples()[i] != null)
                putInt(byteBuffer,header.getNumberOfSamples()[i],EDFElementSize.NUMBER_OF_SAMPLES_SIZE);
        }

        for(int i = 0; i < header.getReserveds().length; i++){
            if(header.getReserveds()[i] != null)
                byteBuffer.put(header.getReserveds()[i]);
        }

        raf.write(byteBuffer.array());

    }

    public static void writeDatarecordsToEDF  (RandomAccessFile raf, Record[] listRecord, int numberofBytes) throws IOException{
    }

    private static void putString(ByteBuffer byteBuffer, String value, int elemSize){
        ByteBuffer valueFormat = ByteBuffer.allocate(elemSize);
        if(value.length() >= elemSize) valueFormat.put(value.getBytes(Charset.forName("ASCII")),0,elemSize);
        else valueFormat.put(value.getBytes(Charset.forName("ASCII")));
        while(valueFormat.remaining() > 0){
            valueFormat.put(" ".getBytes());
        }
        valueFormat.rewind();
        byteBuffer.put(valueFormat);
    }

    private static void putDouble(ByteBuffer byteBuffer, double value, int elemSize, DecimalFormat df){
        putString(byteBuffer,df.format(value), elemSize);
    }

    private static void putInt(ByteBuffer byteBuffer, int value, int elemSize){
        putString(byteBuffer,String.valueOf(value),elemSize);
    }


}
