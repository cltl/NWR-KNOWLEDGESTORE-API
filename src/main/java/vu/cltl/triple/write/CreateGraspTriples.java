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

    
    static String testparameters = "--source chat1 --author-name Piek --event-label bite --actor-label rabbit --perspective CERTAIN;SCARED;NEGATIVE;BELIEF";
    static public void main (String[] args) {
        String authorName = "";
        String authorURI = "";
        String sourceId = "";
        String perspective = "";
        String eventLabel = "";
        String eventUri = "";
        String actorLabel = "";
        String actorUri = "";
        if (args.length==0) {
            args = testparameters.split(" ");
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
            else if (arg.equals("--actor-label") && args.length>(i+1)) {
                actorLabel = args[i+1];
            }
            else if (arg.equals("--actor-uri") && args.length>(i+1)) {
                actorUri = args[i+1];
            }
            else if (arg.equals("--perspective") && args.length>(i+1)) {
                perspective = args[i+1];
            }
        }
        if (authorURI.isEmpty()) {
            authorURI = friendsUri + authorName;
        }

        String str = graspTripleString(sourceId, authorURI, authorName, perspective, eventLabel, eventUri, actorLabel, actorUri );
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

        SemObject semEvent = new SemObject("EVENT");
        semEvent.setId(eventUri);
        semEvent.addPhraseCounts(eventLabel);

        NafMention nafMention  = new NafMention();
        nafMention.setOffSetStart("0");
        nafMention.setOffSetEnd(new Integer(eventLabel.length()).toString());
        nafMention.setBaseUri(sourceId+"#");
        semEvent.addNafMention(nafMention);

        compositeEvent.setEvent(semEvent);

        SemObject semObject = new SemObject("ACTOR");
        semObject.setId(actorUri);
        semObject.addPhraseCounts(actorLabel);
        compositeEvent.addMySemActor(semObject);

        SemRelation semRelation = new SemRelation();
        semRelation.setId(sourceId);
        semRelation.setSubject(semEvent.getURI());
        semRelation.addPredicate(Sem.hasActor.getLocalName());
        semRelation.setObject(semObject.getURI());
        compositeEvent.addMySemRelation(semRelation);
        
        SemRelation timeRelation = new SemRelation();
        timeRelation.setId(sourceId);
        timeRelation.setSubject(semEvent.getURI());
        timeRelation.addPredicate(Sem.hasTime.getLocalName());
        timeRelation.setObject(owlTime.getDateStringURI(worldDate));
        compositeEvent.addMySemRelation(timeRelation);

        return compositeEvent;

    }

    static public String graspTripleString (String sourceId,
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

}

