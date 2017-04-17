package no.uio.ifi.viettt.mscosa.EDFManagement;
public class EDFSignal {
    private Double[] unitsInDigit;
    private short[][] digitalValues;
    private double[][] valuesInUnits;

    public Double[] getUnitsInDigit() {
        return unitsInDigit;
    }

    public void setUnitsInDigit(Double[] unitsInDigit) {
        this.unitsInDigit = unitsInDigit;
    }

    public short[][] getDigitalValues() {
        return digitalValues;
    }

    public void setDigitalValues(short[][] digitalValues) {
        this.digitalValues = digitalValues;
    }

    public double[][] getValuesInUnits() {
        return valuesInUnits;
    }

    public void setValuesInUnits(double[][] valuesInUnits) {
        this.valuesInUnits = valuesInUnits;
    }
}
