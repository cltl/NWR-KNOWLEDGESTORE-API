package vu.cltl.triple.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 4/23/14.
 */
public class CompositeEvent implements Serializable{

    private SemObject event;
    private ArrayList<SemTime> mySemTimes;
    private ArrayList<SemActor> mySemActors;
    private ArrayList<SemRelation> mySemRelations;

    public CompositeEvent() {
        this.event = new SemObject(SemObject.EVENT);
        this.mySemTimes = new ArrayList<SemTime>();
        this.mySemActors = new ArrayList<SemActor>();
        this.mySemRelations = new ArrayList<SemRelation>();
    }



    public CompositeEvent(SemEvent event,
                          ArrayList<SemActor> mySemActors,
                          ArrayList<SemTime> mySemTimes,
                          ArrayList<SemRelation> mySemRelations
                          ) {
        this.event = event;
        this.mySemTimes = mySemTimes;
        this.mySemActors = mySemActors;
        this.mySemRelations = mySemRelations;
    }

    public boolean isValid (){
        boolean hasParticipant = false;
        boolean hasTime = false;
        for (int i = 0; i < mySemRelations.size(); i++) {
            SemRelation semRelation = mySemRelations.get(i);
            for (int j = 0; j < semRelation.getPredicates().size(); j++) {
                String predicate = semRelation.getPredicates().get(j);
                if (predicate.endsWith(Sem.hasPlace.getLocalName()) || predicate.endsWith(Sem.hasActor.getLocalName())) {
                //if (predicate.toLowerCase().endsWith("actor") || predicate.toLowerCase().endsWith("place")) {
                   hasParticipant = true;
                }
                if (SemRelation.isTemporalSemRelationProperty(predicate)) {
                //if (predicate.toLowerCase().endsWith("time") || predicate.toLowerCase().endsWith("timestamp")) {
                   hasTime = true;
                }
            }
        }
        if (hasParticipant && hasTime) {
            return true;
        }
        else {
            return false;
        }
    }


    public static  ArrayList<SemTime> getDominantYear ( ArrayList<SemTime> myTimes) {
        ArrayList<SemTime> domYearTimes = new ArrayList<SemTime>();
        HashMap<String, Integer> yearCount = new HashMap<String, Integer>();
        HashMap<String, ArrayList<SemTime>> yearMap = new HashMap<String, ArrayList<SemTime>>();
        Integer topYear = 0;
        String topYearString = "";
        for (int i = 0; i < myTimes.size(); i++) {
            SemTime semTime = myTimes.get(i);
            String year = semTime.getOwlTime().getYear();
            if (year.isEmpty()) {
                year = semTime.getOwlTimeBegin().getYear();
            }
            if (year.isEmpty()) {
                year = semTime.getOwlTimeEnd().getYear();
            }
            if (!year.isEmpty()) {
                if (yearMap.containsKey(year)) {
                    ArrayList<SemTime> times = yearMap.get(year);
                    times.add(semTime);
                    yearMap.put(year, times);
                }
                else {
                    ArrayList<SemTime> times = new ArrayList<SemTime>();
                    times.add(semTime);
                    yearMap.put(year, times);
                }
                if (yearCount.containsKey(year)) {
                    Integer cnt = yearCount.get(year);
                    cnt++;
                    yearCount.put(year, cnt);
                    if (cnt>topYear) {
                        topYear = cnt;
                        topYearString = year;
                    }
                }
                else {
                    yearCount.put(year, 1);
                    if (topYear==0) {
                        topYearString = year;
                        topYear = 1;
                    }
                }
            }
        }
        domYearTimes = yearMap.get(topYearString);
        //System.out.println("topYearString = " + topYearString);
        return domYearTimes;
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

    public ArrayList<SemTime> getMyDominantSemTimes() {
        return getDominantYear(mySemTimes);
    }



    public void setMySemTimes(ArrayList<SemTime> mySemTimes) {
        this.mySemTimes = mySemTimes;
    }

    public void addMySemTime(SemTime mySemTime) {
        this.mySemTimes.add(mySemTime);
    }

    public ArrayList<SemActor> getMySemActors() {
        return mySemActors;
    }

    public void setMySemActors(ArrayList<SemActor> mySemActors) {
        this.mySemActors = mySemActors;
    }

    public void addMySemActor(SemActor mySemActor) {
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


    public void mergeObjects (CompositeEvent event) {
         for (int i = 0; i < event.getMySemActors().size(); i++) {
            SemActor semActor1 = event.getMySemActors().get(i);
            boolean match = false;
            for (int j = 0; j < this.getMySemActors().size(); j++) {
                SemActor semActor2 = this.getMySemActors().get(j);
                if (semActor1.getURI().equals(semActor2.getURI())) {
                  //  System.out.println("adding semActor1 = " + semActor1.getURI());
                  //  System.out.println("adding semActor2 = " + semActor2.getURI());
                    semActor2.mergeSemObject(semActor1);
                    match = true;
                    break;
                }
            }
            if (!match) {
               //  System.out.println("adding semActor1 = " + semActor1.getURI());
                 this.mySemActors.add(semActor1);
            }
        }


        for (int i = 0; i < event.getMySemTimes().size(); i++) {
            SemTime semTime1 = event.getMySemTimes().get(i);
            boolean match = false;
            for (int j = 0; j < this.getMySemTimes().size(); j++) {
                SemTime semTime2 = this.getMySemTimes().get(j);
                if (semTime1.getOwlTime().matchTimeExact(semTime2.getOwlTime())) {
                 //   System.out.println("semTime1 = " + semTime1.getURI());
                 //   System.out.println("semTime2 = " + semTime2.getURI());
                    semTime2.mergeSemObject(semTime1);
                    match = true;
                    break;
                }
                else if (semTime1.getOwlTimeBegin().matchTimeExact(semTime2.getOwlTimeBegin())) {
                 //   System.out.println("semTime1 = " + semTime1.getURI());
                 //   System.out.println("semTime2 = " + semTime2.getURI());
                    semTime2.mergeSemObject(semTime1);
                    match = true;
                    break;
                }
                else if (semTime1.getOwlTimeEnd().matchTimeExact(semTime2.getOwlTimeEnd())) {
                 //   System.out.println("semTime1 = " + semTime1.getURI());
                 //   System.out.println("semTime2 = " + semTime2.getURI());
                    semTime2.mergeSemObject(semTime1);
                    match = true;
                    break;
                }
            }
            if (!match) {
              //   System.out.println("adding semTime1 = " + semTime1.getURI());
                 this.mySemTimes.add(semTime1);
            }
        }
    }

    public String toString () {
        String str = this.event.getId();
        str += this.event.getPhrase()+"\n";
        for (int i = 0; i < mySemActors.size(); i++) {
            SemActor semActor = mySemActors.get(i);
            str += "\t"+semActor.getId()+"\n";
        }
        for (int i = 0; i < mySemTimes.size(); i++) {
            SemTime semTime = mySemTimes.get(i);
            str += "\t"+semTime.getId()+"\n";
        }
        for (int i = 0; i < mySemRelations.size(); i++) {
            SemRelation semRelation = mySemRelations.get(i);
            str += "\t"+semRelation.getSubject()+":"+semRelation.getPredicates().toString()+":"+semRelation.getObject()+"\n";
        }
        return str;
    }

}
