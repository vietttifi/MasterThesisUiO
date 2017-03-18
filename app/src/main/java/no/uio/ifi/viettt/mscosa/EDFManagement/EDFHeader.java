package no.uio.ifi.viettt.mscosa.EDFManagement;

public class EDFHeader
{
        private String version = null;
        private String patientInfo = null;
        private String clinicInfo = null;
        private String startDate = null;
        private String startTime = null;
        private int bytesInHeader = 0;
        private String reservedFormat = null;
        private int numberOfRecords = 0;
        private double durationOfRecords = 0;
        private int numberOfChannels = 0;
        private String[] channelLabels = null;
        private String[] transducerTypes = null;
        private String[] dimensions = null;
        private Double[] minInUnits = null;
        private Double[] maxInUnits = null;
        private Integer[] digitalMin = null;
        private Integer[] digitalMax = null;
        private String[] prefilterings = null;
        private Integer[] numberOfSamples = null;
        private byte[][] reserveds = null;

        public void printHeader(){
            System.out.println("------------- THE HEADER RECORD CONTAINS -----------------");
            System.out.println("8 ascii : version of this data format (0) ---> "+version);
            System.out.println("80 ascii : local patient identification ---> "+patientInfo);
            System.out.println("80 ascii : local recording identification ---> "+clinicInfo);
            System.out.println("8 ascii : startdate of recording (dd.mm.yy) ---> "+startDate);
            System.out.println("8 ascii : starttime of recording (hh.mm.ss). ---> "+startTime);
            System.out.println("8 ascii : number of bytes in header record ---> "+bytesInHeader);
            System.out.println("44 ascii : reserved ---> "+reservedFormat);
            System.out.println("8 ascii : number of data records (-1 if unknown) ---> "+numberOfRecords);
            System.out.println("8 ascii : duration of a data record, in seconds ---> "+durationOfRecords);
            System.out.println("4 ascii : number of signals (ns) in data record ---> "+numberOfChannels);

            System.out.println(" ----------------------- CHANNELS --------------------- ");
            for(int i = 0; i<numberOfChannels;i++){
                System.out.println("ns * 16 ascii : ns * label ---> "+channelLabels[i]);
                System.out.println("ns * 80 ascii : ns * transducer type (e.g. AgAgCl electrode) ---> "+transducerTypes[i]);
                System.out.println("ns * 8 ascii : ns * physical dimension (e.g. uV) ---> "+dimensions[i]);
                System.out.println("ns * 8 ascii : ns * physical minimum (e.g. -500 or 34) ---> "+minInUnits[i]);
                System.out.println("ns * 8 ascii : ns * physical maximum (e.g. 500 or 40) ---> "+maxInUnits[i]);
                System.out.println("ns * 8 ascii : ns * digital minimum (e.g. -2048) ---> "+digitalMin[i]);
                System.out.println("ns * 8 ascii : ns * digital maximum (e.g. 2047) ---> "+digitalMax[i]);
                System.out.println("ns * 80 ascii : ns * prefiltering (e.g. HP:0.1Hz LP:75Hz) ---> "+prefilterings[i]);
                System.out.println("ns * 8 ascii : ns * nr of samples in each data record ---> "+numberOfSamples[i]);
                System.out.println("ns * 32 ascii : ns * reserved <-------      END           ------> ");
            }
        }

        public String getVersion() {
                return version;
        }

        public void setVersion(String version) {
                this.version = version;
        }

        public String getPatientInfo() {
                return patientInfo;
        }

        public void setPatientInfo(String patientInfo) {
                this.patientInfo = patientInfo;
        }

        public String getClinicInfo() {
                return clinicInfo;
        }

        public void setClinicInfo(String clinicInfo) {
                this.clinicInfo = clinicInfo;
        }

        public String getStartDate() {
                return startDate;
        }

        public void setStartDate(String startDate) {
                this.startDate = startDate;
        }

        public String getStartTime() {
                return startTime;
        }

        public void setStartTime(String startTime) {
                this.startTime = startTime;
        }

        public int getBytesInHeader() {
                return bytesInHeader;
        }

        public void setBytesInHeader(int bytesInHeader) {
                this.bytesInHeader = bytesInHeader;
        }

        public String getReservedFormat() {
                return reservedFormat;
        }

        public void setReservedFormat(String reservedFormat) {
                this.reservedFormat = reservedFormat;
        }

        public int getNumberOfRecords() {
                return numberOfRecords;
        }

        public void setNumberOfRecords(int numberOfRecords) {
                this.numberOfRecords = numberOfRecords;
        }

        public double getDurationOfRecords() {
                return durationOfRecords;
        }

        public void setDurationOfRecords(double durationOfRecords) {
                this.durationOfRecords = durationOfRecords;
        }

        public int getNumberOfChannels() {
                return numberOfChannels;
        }

        public void setNumberOfChannels(int numberOfChannels) {
                this.numberOfChannels = numberOfChannels;
        }

        public String[] getChannelLabels() {
                return channelLabels;
        }

        public void setChannelLabels(String[] channelLabels) {
                this.channelLabels = channelLabels;
        }

        public String[] getTransducerTypes() {
                return transducerTypes;
        }

        public void setTransducerTypes(String[] transducerTypes) {
                this.transducerTypes = transducerTypes;
        }

        public String[] getDimensions() {
                return dimensions;
        }

        public void setDimensions(String[] dimensions) {
                this.dimensions = dimensions;
        }

        public Double[] getMinInUnits() {
                return minInUnits;
        }

        public void setMinInUnits(Double[] minInUnits) {
                this.minInUnits = minInUnits;
        }

        public Double[] getMaxInUnits() {
                return maxInUnits;
        }

        public void setMaxInUnits(Double[] maxInUnits) {
                this.maxInUnits = maxInUnits;
        }

        public Integer[] getDigitalMin() {
                return digitalMin;
        }

        public void setDigitalMin(Integer[] digitalMin) {
                this.digitalMin = digitalMin;
        }

        public Integer[] getDigitalMax() {
                return digitalMax;
        }

        public void setDigitalMax(Integer[] digitalMax) {
                this.digitalMax = digitalMax;
        }

        public String[] getPrefilterings() {
                return prefilterings;
        }

        public void setPrefilterings(String[] prefilterings) {
                this.prefilterings = prefilterings;
        }

        public Integer[] getNumberOfSamples() {
                return numberOfSamples;
        }

        public void setNumberOfSamples(Integer[] numberOfSamples) {
                this.numberOfSamples = numberOfSamples;
        }

        public byte[][] getReserveds() {
                return reserveds;
        }

        public void setReserveds(byte[][] reserveds) {
                this.reserveds = reserveds;
        }


}
