package vu.cltl.triple.write;

import com.hp.hpl.jena.query.Dataset;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import vu.cltl.triple.objects.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class CreateGraspTriples {
    static boolean VERBOSE_MENTION = false;
    static public final String talkUri = "http://cltl.nl/leolani/talk/";
    static public final String friendsUri = "http://cltl.nl/leolani/friends/";
    static public final String worldUri = "http://cltl.nl/leolani/world/";
    static public final String worldDate = "http://cltl.nl/leolani/date/";
    static Integer offsetStart = 0;
    
    static String testparameters1 = "--source chat1 --author-name Piek --event-label bite --event-type http://www.newsreader-project.eu/domain-ontology#Attack --actor-label rabbit --actor-type http://dbpedia.org/resource/Animal --perspective CERTAIN;SCARED;NEGATIVE;BELIEF";
    static String testparameters2 = "--source chat1 --author-name Piek --event-label Selene --event-type http://dbpedia.org/resource/Person --predicate-uri comesFrom --actor-label Mexico --actor-type http://dbpedia.org/resource/Country --perspective CERTAIN;SCARED;NEGATIVE;BELIEF";
    static public void main (String[] args) {
        String authorName = "";
        String authorURI = "";
        String sourceId = "";
        String predicate = "";
        String perspective = "";
        String eventLabel = "";
        String eventUri = "";
        String eventType = "";
        String actorLabel = "";
        String actorUri = "";
        String actorType = "";
        offsetStart = 0;
        if (args.length==0) {
            //args = testparameters1.split(" ");
            args = testparameters2.split(" ");
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--author-name") && args.length>(i+1)) {
                authorName = args[i+1];
            }
            else if (arg.equals("--source") && args.length>(i+1)) {
                sourceId = args[i+1];
            }
            else if (arg.equals("--event-label") && args.length>(i+1)) {
                eventLabel = args[i+1];
            }
            else if (arg.equals("--event-uri") && args.length>(i+1)) {
                eventLabel = args[i+1];
            }
            else if (arg.equals("--event-type") && args.length>(i+1)) {
                eventType = args[i+1];
            }
            else if (arg.equals("--predicate-uri") && args.length>(i+1)) {
                predicate = args[i+1];
            }
            else if (arg.equals("--actor-label") && args.length>(i+1)) {
                actorLabel = args[i+1];
            }
            else if (arg.equals("--actor-uri") && args.length>(i+1)) {
                actorUri = args[i+1];
            }
            else if (arg.equals("--actor-type") && args.length>(i+1)) {
                actorType = args[i+1];
            }
            else if (arg.equals("--perspective") && args.length>(i+1)) {
                perspective = args[i+1];
            }
        }
        if (authorURI.isEmpty()) {
            authorURI = friendsUri + authorName;
        }
        String str = "";
        if (predicate.isEmpty()) {
            str = graspEventTripleString(sourceId, authorURI, authorName, perspective, eventLabel, eventUri, actorLabel, actorUri);
        }
        else {
            str = graspEntityTripleString(sourceId, authorURI, authorName, perspective, eventLabel, eventUri, eventType, predicate, actorLabel, actorUri, actorType);
        }
        try {
            OutputStream fos = new FileOutputStream("leolani.rdf");
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static CompositeEvent makeCompositeEvent (String sourceId,
                                              String eventLabel,
                                              String eventUri,
                                              String actorLabel,
                                              String actorUri,
                                              OwlTime owlTime) {
        CompositeEvent compositeEvent = new CompositeEvent();
        SemObject semEvent = makeSemObject(sourceId, eventLabel, eventUri, "EVENT");
        compositeEvent.setEvent(semEvent);
        SemObject semObject = makeSemObject(sourceId, actorLabel, actorUri, "ACTOR");
        SemRelation semRelation = makeSemRelation(sourceId, semEvent, Sem.hasActor.getLocalName() , semObject);
        compositeEvent.addMySemRelation(semRelation);
        SemRelation timeRelation = makeTimeRelation(sourceId, semEvent,Sem.hasTime.getLocalName(), owlTime);
        compositeEvent.addMySemRelation(timeRelation);
        return compositeEvent;
    }


    static SemObject makeSemObject (String sourceId,
                                    String objectLabel,
                                    String objectUri,
                                    String type) {
        SemObject object = new SemObject(type);
        object.setId(objectUri);
        object.addPhraseCounts(objectLabel);

        NafMention nafMention  = new NafMention();
        nafMention.setOffSetStart(offsetStart.toString());
        nafMention.setOffSetEnd(new Integer(objectLabel.length()).toString());
        offsetStart+= new Integer(objectLabel.length());
        nafMention.setBaseUri(sourceId+"#");
        object.addNafMention(nafMention);
        return object;
    }

    static SemRelation makeSemRelation (String sourceId,
                                     SemObject subject,
                                     String property,
                                     SemObject object) {
               SemRelation semRelation = new SemRelation();
               semRelation.setId(sourceId);
               semRelation.setSubject(subject.getURI());
               semRelation.addPredicate(property);
               semRelation.setObject(object.getURI());
               return semRelation;
    }

    static SemRelation makeTimeRelation (String sourceId,
                                     SemObject subject,
                                     String property,
                                     OwlTime owlTime) {
         SemRelation timeRelation = new SemRelation();
         timeRelation.setId(sourceId);
         timeRelation.setSubject(subject.getURI());
         timeRelation.addPredicate(Sem.hasTime.getLocalName());
         timeRelation.setObject(owlTime.getDateStringURI(worldDate));
         return timeRelation;
    }

    static public String graspEventTripleString (String sourceId,
                                            String authorUri,
                                     String authorName,
                                     String perspectiveValues,
                                     String eventLabel,
                                     String eventUri,
                                     String actorLabel,
                                     String actorUri) {

        String rdfString = "";

        Dataset ds = graspDataSet(sourceId, authorUri, authorName, perspectiveValues, eventLabel,eventUri,actorLabel,actorUri);

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            RDFDataMgr.write(os, ds, RDFFormat.TRIG_PRETTY);
            rdfString = new String(os.toByteArray(),"UTF-8");
            os.close();
        } catch (Exception e) {
            //  e.printStackTrace();
        }
        return rdfString;
    }

    static public String graspEntityTripleString (String sourceId,
                                            String authorUri,
                                            String authorName,
                                            String perspectiveValues,
                                            String subjectLabel,
                                            String subjectUri,
                                            String subjectType,
                                            String predicateUri,
                                            String objectLabel,
                                            String objectUri,
                                            String objectType) {

        String rdfString = "";

        Dataset ds = graspDataSet(sourceId, authorUri, authorName, perspectiveValues, subjectLabel,subjectUri,subjectType, predicateUri, objectLabel,objectUri, objectType);

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            RDFDataMgr.write(os, ds, RDFFormat.TRIG_PRETTY);
            rdfString = new String(os.toByteArray(),"UTF-8");
            os.close();
        } catch (Exception e) {
            //  e.printStackTrace();
        }
        return rdfString;
    }

    static public Dataset graspDataSet (String sourceId, String authorUri,
                                             String authorName,
                                             String perspectiveValues,
                                             String eventLabel,
                                             String eventUri,
                                             String actorLabel,
                                             String actorUri) {

        JenaSerialization.createModels("http://cltl.nl/leolani/world/");
        JenaSerialization.prefixModels();
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniWorld", "http://cltl.nl/leolani/world/");
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniTalk", "http://cltl.nl/leolani/talk/");
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniTime", "http://cltl.nl/leolani/date/");
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniFriends", "http://cltl.nl/leolani/friends/");

        SemObject semAuthor = new SemObject("AUTHOR");
        semAuthor.setId(authorUri);
        semAuthor.addPhraseCounts(authorName);

        OwlTime owlTime = new OwlTime();
        owlTime.initNow();
        owlTime.addToJenaModelOwlTimeInstant(JenaSerialization.instanceModel,worldDate);

        String sourceUri = talkUri+sourceId+"#"+authorName+owlTime.getDateString();

        ArrayList<CompositeEvent> semEvents = new ArrayList<CompositeEvent>();
        if (eventUri.isEmpty()) {
            eventUri = worldUri + authorName + owlTime.getDateString() + eventLabel;
        }
        if (actorUri.isEmpty()) {
            actorUri = worldUri + actorLabel;
        }
        CompositeEvent compositeEvent = makeCompositeEvent(sourceUri, eventLabel, eventUri, actorLabel, actorUri, owlTime);
        semEvents.add(compositeEvent);
        JenaSerialization.addJenaCompositeEvents( semEvents, VERBOSE_MENTION);

        JenaSerialization.addSourceMetaData(sourceUri, authorUri, "http://cltl.nl/leolani/date/"+owlTime.getDateString());
        ArrayList<NafMention> mentions = compositeEvent.getEvent().getNafMentions();
        for (int i = 0; i < mentions.size(); i++) {
            NafMention nafMention =  mentions.get(i);
            String nafUri = "";
            if (VERBOSE_MENTION) {
                nafUri = nafMention.toStringFull();
            }
            else {
                nafUri = nafMention.toString();
            }
            JenaSerialization.addPerspectiveToJenaDataSet(ResourcesUri.prov, nafUri, sourceUri, perspectiveValues);
        }
        return JenaSerialization.ds;
    }


    static public Dataset graspDataSet (String sourceId, String authorUri,
                                             String authorName,
                                             String perspectiveValues,
                                             String subjectLabel,
                                             String subjectUri,
                                             String subjectType,
                                             String predicateUri,
                                             String objectLabel,
                                             String objectUri,
                                             String objectType) {

        JenaSerialization.createModels("http://cltl.nl/leolani/world/");
        JenaSerialization.prefixModels();
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniWorld", "http://cltl.nl/leolani/world/");
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniTalk", "http://cltl.nl/leolani/talk/");
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniTime", "http://cltl.nl/leolani/date/");
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniFriends", "http://cltl.nl/leolani/friends/");

        SemObject semAuthor = new SemObject("AUTHOR");
        semAuthor.setId(authorUri);
        semAuthor.addPhraseCounts(authorName);

        OwlTime owlTime = new OwlTime();
        owlTime.initNow();
        owlTime.addToJenaModelOwlTimeInstant(JenaSerialization.instanceModel,worldDate);

        String sourceUri = talkUri+sourceId+"#"+authorName+owlTime.getDateString();

        if (subjectUri.isEmpty()) {
            subjectUri = worldUri + authorName + owlTime.getDateString() + subjectLabel;
        }
        if (objectUri.isEmpty()) {
            objectUri = worldUri + authorName + owlTime.getDateString() + objectLabel;
        }

        SemObject subject = makeSemObject(sourceUri, subjectLabel, subjectUri, "ENTITY");
        SemObject object = makeSemObject(sourceUri, objectLabel, objectUri, "ENTITY");

        JenaSerialization.addJenaObject( subject, subjectType, VERBOSE_MENTION);
        JenaSerialization.addJenaObject( object, objectType,VERBOSE_MENTION);


        JenaSerialization.addSourceMetaData(sourceUri, authorUri, "http://cltl.nl/leolani/date/"+owlTime.getDateString());
        ArrayList<NafMention> mentions = subject.getNafMentions();
        for (int i = 0; i < mentions.size(); i++) {
            NafMention nafMention =  mentions.get(i);
            String nafUri = "";
            if (VERBOSE_MENTION) {
                nafUri = nafMention.toStringFull();
            }
            else {
                nafUri = nafMention.toString();
            }
            String attributionId = JenaSerialization.addPerspectiveToJenaDataSet(ResourcesUri.prov, nafUri, sourceUri, perspectiveValues);
            SemRelation relation = makeSemRelation(sourceUri, subject, predicateUri, object);
          //  SemRelation time = makeTimeRelation(sourceUri, subject, Sem.hasTime.getLocalName(), owlTime);
            JenaSerialization.addJenaRelation( relation, VERBOSE_MENTION);
          //  JenaSerialization.addJenaRelation( time, VERBOSE_MENTION);
        }
        return JenaSerialization.ds;
    }

}

