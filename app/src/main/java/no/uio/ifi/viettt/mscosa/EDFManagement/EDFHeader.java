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
