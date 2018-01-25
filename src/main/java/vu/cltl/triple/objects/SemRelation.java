package vu.cltl.triple.objects;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Piek
 * Date: 11/15/13
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemRelation implements Serializable {

    private String id;
    private ArrayList<String> predicates;
    private String subject;
    private String object;
    private ArrayList<NafMention> nafMentions;

    public SemRelation() {
        this.nafMentions = new ArrayList<NafMention>();
        this.id = "";
        this.object = "";
        this.subject = "";
        this.predicates = new ArrayList<String>();
    }

    public ArrayList<String> getPredicates() {
        return predicates;
    }

    public void addPredicate(String predicate) {
        if (!containsPredicateIgnoreCase(predicate)) {
            this.predicates.add(predicate);
        }
    }

    public boolean containsPredicateIgnoreCase (String predicate) {
        for (int i = 0; i < predicates.size(); i++) {
            String pred = predicates.get(i);
            if (pred.equalsIgnoreCase(predicate)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<NafMention> getNafMentions() {
        return nafMentions;
    }

    public void setNafMentions(ArrayList<NafMention> nafMentions) {
        this.nafMentions = nafMentions;
    }

    public void addMention(NafMention mention) {
        if (!mention.hasMention(this.getNafMentions())) {
            this.nafMentions.add(mention);
        }
    }

    public void addMentions(ArrayList<NafMention> mentions) {
        for (int i = 0; i < mentions.size(); i++) {
            NafMention nafMention = mentions.get(i);
            this.addMention(nafMention);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }


    public void addSemToJenaDataSet (Dataset ds) {
        Model relationModel = ds.getNamedModel(this.id);
        Resource subject = relationModel.createResource(this.getSubject());
        Resource object = relationModel.createResource(this.getObject());
        Property semProperty = null;
        for (int i = 0; i < predicates.size(); i++) {
            String predicate = predicates.get(i);
            semProperty = getSemRelationProperty(predicate);
            subject.addProperty(semProperty, object);
        }
    }

    public void addRelationToJenaDataSet (Dataset ds) {
            Model relationModel = ds.getNamedModel(this.id);
            Resource subject = relationModel.createResource(this.getSubject());
            Resource object = relationModel.createResource(this.getObject());
            for (int i = 0; i < predicates.size(); i++) {
                String predicate = predicates.get(i);
                Property semProperty = relationModel.createProperty(predicate);
                subject.addProperty(semProperty, object);
            }
        }


    public Property getSemRelationProperty (String type) {
        if (type.equals(Sem.hasTime.getLocalName())) {
            return Sem.hasTime;
        }
        if (type.equals(Sem.hasAtTime.getLocalName())) {
            return Sem.hasAtTime;
        }
        else if (type.equals(Sem.hasBeginTime.getLocalName())) {
            return Sem.hasBeginTime;
        }
        else if (type.equals(Sem.hasEndTime.getLocalName())) {
            return Sem.hasEndTime;
        }
        else if (type.equals(Sem.hasFutureTime.getLocalName())) {
            return Sem.hasFutureTime;
        }
        else if (type.equals(Sem.hasEarliestBeginTime.getLocalName())) {
            return Sem.hasEarliestBeginTime;
        }
        else if (type.equals(Sem.hasEarliestEndTime.getLocalName())) {
            return Sem.hasEarliestEndTime;
        }
        else if (type.equals(Sem.hasFutureTimeStamp.getLocalName())) {
            return Sem.hasFutureTimeStamp;
        }
        else if (type.equals(Sem.hasBeginTimeStamp.getLocalName())) {
            //BiographyNet uses sem:hasBeginTimeStamp
            return Sem.hasBeginTimeStamp;
        }
        else if (type.equals(Sem.hasEndTimeStamp.getLocalName())) {
            return Sem.hasEndTimeStamp;
        }
        else if (type.equals(Sem.hasEarliestBeginTimeStamp.getLocalName())) {
          //  return Sem.hasFutureTimeStamp;
            return Sem.hasEarliestBeginTimeStamp;
        }
        else if (type.equals(Sem.hasEarliestEndTimeStamp.getLocalName())) {
          //  return Sem.hasFutureTimeStamp;
            return Sem.hasEarliestEndTimeStamp;
        }
        else if (type.equals(Sem.hasPlace.getLocalName())) {
            return Sem.hasPlace;
        }
        else if (type.equals(Sem.hasActor.getLocalName())) {
            return Sem.hasActor;
        }
        else {
           // System.out.println("type = " + type);
            return Sem.hasSubType;
        }
    }


    public String toString () {
        String str = "";
        for (int i = 0; i < predicates.size(); i++) {
            String pred = predicates.get(i);
            str +=  subject+"#"+pred+"#"+object;
        }
        return str;
    }
}
