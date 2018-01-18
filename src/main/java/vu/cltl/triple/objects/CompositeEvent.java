package vu.cltl.triple.objects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by piek on 4/23/14.
 */
public class CompositeEvent implements Serializable{

    private SemObject event;
    private ArrayList<SemTime> mySemTimes;
    private ArrayList<SemObject> mySemActors;
    private ArrayList<SemRelation> mySemRelations;

    public CompositeEvent() {
        this.event = new SemObject(SemObject.EVENT);
        this.mySemTimes = new ArrayList<SemTime>();
        this.mySemActors = new ArrayList<SemObject>();
        this.mySemRelations = new ArrayList<SemRelation>();
    }



    public CompositeEvent(SemObject event,
                          ArrayList<SemObject> mySemActors,
                          ArrayList<SemTime> mySemTimes,
                          ArrayList<SemRelation> mySemRelations
                          ) {
        this.event = event;
        this.mySemTimes = mySemTimes;
        this.mySemActors = mySemActors;
        this.mySemRelations = mySemRelations;
    }
    
    public SemObject getEvent() {
        return event;
    }

    public void setEvent(SemObject event) {
        this.event = event;
    }

    public ArrayList<SemTime> getMySemTimes() {
        return mySemTimes;
    }

    public void setMySemTimes(ArrayList<SemTime> mySemTimes) {
        this.mySemTimes = mySemTimes;
    }

    public void addMySemTime(SemTime mySemTime) {
        this.mySemTimes.add(mySemTime);
    }

    public ArrayList<SemObject> getMySemActors() {
        return mySemActors;
    }

    public void setMySemActors(ArrayList<SemObject> mySemActors) {
        this.mySemActors = mySemActors;
    }

    public void addMySemActor(SemObject mySemActor) {
        this.mySemActors.add(mySemActor);
    }

    public ArrayList<SemRelation> getMySemRelations() {
        return mySemRelations;
    }

    public void setMySemRelations(ArrayList<SemRelation> mySemRelations) {
        this.mySemRelations = mySemRelations;
    }

    public void addMySemRelation(SemRelation mySemRelation) {
        this.mySemRelations.add(mySemRelation);
    }

}
