package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;

/**
 * Created by viettt on 13/02/2017.
 */

public class ProducerConsumerBuffer {
    private Record[] recordsBuff;
    private int capacity;
    private int consumerInx = 0;
    private int producerCnt = 0;

    public ProducerConsumerBuffer(int capacity){
        this.capacity = capacity;
        recordsBuff = new Record[capacity];
    }

    private boolean is_full(){
        return producerCnt == capacity;
    }

    private boolean is_empty(){
        return producerCnt == 0;
    }

    public int getSize(){
        return capacity;
    }

    public synchronized void putToBuff(Record record){
        while (is_full()){
            try {
                wait();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        recordsBuff[(consumerInx+producerCnt)%capacity] = record;
        producerCnt++;
        notifyAll();
    }

    public synchronized Record getFromBuff(){
        while (is_empty()){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Record record = recordsBuff[consumerInx];
        consumerInx = (consumerInx+1) % capacity;
        producerCnt--;
        notifyAll();
        return record;
    }

}
