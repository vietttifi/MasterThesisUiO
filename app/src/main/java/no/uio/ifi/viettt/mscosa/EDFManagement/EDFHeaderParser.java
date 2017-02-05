package no.uio.ifi.viettt.mscosa.EDFManagement;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EDFHeaderParser{
    public static EDFHeader parseHeader(InputStream is){
        try {
            EDFHeader header = new EDFHeader();
            header.setVersion(readAnASCIIString(is, EDFElementSize.VERSION_SIZE));

            //according to edfplus.org. Version must be 0
            //8 ascii : version of this data format (0)
            if (!header.getVersion().trim().equals("0")){
                return null;
            }
            /* Header general information*/
            header.setPatientInfo(readAnASCIIString(is, EDFElementSize.PATIENT_INFO_SIZE));
            header.setClinicInfo(readAnASCIIString(is, EDFElementSize.CLINIC_INFO_SIZE));
            header.setStartDate(readAnASCIIString(is, EDFElementSize.START_DATE_SIZE));
            header.setStartTime(readAnASCIIString(is, EDFElementSize.START_TIME_SIZE));
            header.setBytesInHeader(Integer.parseInt(readAnASCIIString(is, EDFElementSize.HEADER_SIZE).trim()));
            header.setReservedFormat(readAnASCIIString(is, EDFElementSize.RESERVED_FORMAT_SIZE));
            header.setNumberOfRecords(Integer.parseInt(readAnASCIIString(is, EDFElementSize.NUMBER_OF_DATA_RECORDS_SIZE).trim()));
            header.setDurationOfRecords(Double.parseDouble(readAnASCIIString(is, EDFElementSize.DURATION_DATA_RECORDS_SIZE).trim()));
            header.setNumberOfChannels(Integer.parseInt(readAnASCIIString(is, EDFElementSize.NUMBER_OF_CHANNELS_SIZE).trim()));

            /*Channels information*/
            try{
                int numberOfChannels = header.getNumberOfChannels();
                header.setChannelLabels(readASCIIStrings(is, EDFElementSize.LABEL_OF_CHANNEL_SIZE, numberOfChannels));
                header.setTransducerTypes(readASCIIStrings(is, EDFElementSize.TRANSDUCER_TYPE_SIZE, numberOfChannels));
                header.setDimensions(readASCIIStrings(is, EDFElementSize.PHYSICAL_DIMENSION_OF_CHANNEL_SIZE, numberOfChannels));
                header.setMinInUnits(readDoubles(is, EDFElementSize.PHYSICAL_MIN_IN_UNITS_SIZE, numberOfChannels));
                header.setMinInUnits(readDoubles(is, EDFElementSize.PHYSICAL_MAX_IN_UNITS_SIZE, numberOfChannels));
                header.setDigitalMin(readIntegers(is, EDFElementSize.DIGITAL_MIN_SIZE, numberOfChannels));
                header.setDigitalMax(readIntegers(is, EDFElementSize.DIGITAL_MAX_SIZE, numberOfChannels));
                header.setPrefilterings(readASCIIStrings(is, EDFElementSize.PREFILTERING_SIZE, numberOfChannels));
                header.setNumberOfSamples(readIntegers(is, EDFElementSize.NUMBER_OF_SAMPLES_SIZE, numberOfChannels));

                byte[][] reserved = new byte[numberOfChannels][];
                for (int i = 0; i < reserved.length; i++) {
                    reserved[i] = new byte[EDFElementSize.RESERVED_SIZE];
                    int no_read_bytes = is.read(reserved[i]);
                    if(no_read_bytes == -1) throw new IOException();
                }

                header.setReserveds(reserved);

            } catch (IOException e){
                return null;
            }

            return header;

        } catch (IOException e){
            return null;
        }
    }

    public static List<EDFAnnotation> parseAnnotations(byte[] b) {
        List<EDFAnnotation> annotations = new ArrayList<>();
        int inxOnSet = 0, inxDuration = -1, inxAnnotation = -1, inxEndAnn = -1;
        for (int i = 0; i < b.length - 1; i++){

            //duration begin after code 21 as specifying in edfplus.org
            if (b[i] == 21){
                inxDuration = i;
            } else if (b[i] == 20 && inxOnSet > inxAnnotation){
                //It is a next annotation
                inxAnnotation = i;
            } else if (b[i] == 20 && b[i + 1] == 0){
                //End one annotation
                inxEndAnn = i;
            } else if (b[i] != 0 && inxOnSet < inxEndAnn){
                //Collect an annotation information
                if (inxDuration > inxOnSet){
                    double onSetValue = Double.parseDouble(new String(b, inxOnSet, inxDuration - inxOnSet));
                    double durationValue = Double.parseDouble(new String(b, inxDuration, inxAnnotation - inxDuration));
                    String annotation = new String(b, inxAnnotation, inxEndAnn - inxAnnotation);
                    //code 20 which is a separator between each annotation = u0014
                    annotations.add(new EDFAnnotation(onSetValue, durationValue, annotation.split("[\u0014]")));
                } else {
                    double onSetValue = Double.parseDouble(new String(b, inxOnSet, inxAnnotation - inxOnSet));
                    String annotation = new String(b, inxAnnotation, inxEndAnn - inxAnnotation);
                    //code 20 which is a separator between each annotation = u0014
                    annotations.add(new EDFAnnotation(onSetValue, 0, annotation.split("[\u0014]")));
                }

                inxOnSet = i;
            } else{
                System.out.println("Unexpected character in EDF format");
            }
        }

        return annotations;
    }

    private static String[] readASCIIStrings(InputStream is, int size, int length) throws IOException {
        String[] result = new String[length];
        for (int i = 0; i < length; i++) result[i] = readAnASCIIString(is, size);
        return result;
    }

    private static Double[] readDoubles(InputStream is, int size, int length) throws IOException {
        Double[] result = new Double[length];
        for (int i = 0; i < length; i++) result[i] = Double.parseDouble(readAnASCIIString(is, size).trim());
        return result;
    }

    private static Integer[] readIntegers(InputStream is, int size, int length) throws IOException {
        Integer[] result = new Integer[length];
        for (int i = 0; i < length; i++) result[i] = Integer.parseInt(readAnASCIIString(is, size).trim());
        return result;
    }

    private static String readAnASCIIString(InputStream is, int size) throws IOException {
        int len;
        byte[] data = new byte[size];
        len = is.read(data);
        if (len != data.length) throw new IOException();
        return new String(data, Charset.forName("ASCII"));
    }
}
