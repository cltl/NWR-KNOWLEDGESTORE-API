package vu.cltl.triple.write;

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
    static final String talkUri = "http://cltl.nl/leolani/talk/";
    static final String friendsUri = "http://cltl.nl/leolani/friends/";
    static final String worldUri = "http://cltl.nl/leolani/world/";
    static final String worldDate = "http://cltl.nl/leolani/date/";

    static public void main (String[] args) {
        String author = "Piek";
        ArrayList<String> values = new ArrayList<String>();
        values.add("CERTAIN");
        values.add("SCARED");
        values.add("NEGATIVE");
        values.add("BELIEF");
        String str = graspTripleString(author, values, "bite", "rabbit");
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
                                              String actorLabel,
                                              OwlTime owlTime) {
        CompositeEvent compositeEvent = new CompositeEvent();

        SemObject semEvent = new SemObject("EVENT");
        semEvent.setId(worldUri+eventLabel);
        semEvent.addPhraseCounts(eventLabel);

        NafMention nafMention  = new NafMention();
        nafMention.setOffSetStart("0");
        nafMention.setOffSetEnd(new Integer(eventLabel.length()).toString());
        nafMention.setBaseUri(sourceId+"#");
        semEvent.addNafMention(nafMention);

        compositeEvent.setEvent(semEvent);

        SemObject semObject1 = new SemObject("ACTOR");
        semObject1.setId(worldUri+actorLabel);
        semObject1.addPhraseCounts(actorLabel);
        compositeEvent.addMySemActor(semObject1);

        SemRelation semRelation = new SemRelation();
        semRelation.setId(sourceId);
        semRelation.setSubject(semEvent.getURI());
        semRelation.addPredicate(Sem.hasActor.getLocalName());
        semRelation.setObject(semObject1.getURI());
        compositeEvent.addMySemRelation(semRelation);
        
        SemRelation timeRelation = new SemRelation();
        timeRelation.setId(sourceId);
        timeRelation.setSubject(semEvent.getURI());
        timeRelation.addPredicate(Sem.hasTime.getLocalName());
        timeRelation.setObject(owlTime.getDateStringURI(worldDate));
        compositeEvent.addMySemRelation(timeRelation);

        return compositeEvent;

    }

    static String graspTripleString (String author,
                                     ArrayList<String> perspectiveValues,
                                     String event,
                                     String actor) {

        String rdfString = "";

        JenaSerialization.createModels("http://cltl.nl/leolani/world/");
        JenaSerialization.prefixModels();
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniWorld", "http://cltl.nl/leolani/world/");
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniTalk", "http://cltl.nl/leolani/talk/");
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniTime", "http://cltl.nl/leolani/date/");

        SemObject semAuthor = new SemObject("ACTOR");
        semAuthor.setId(author);
        semAuthor.setLabel(author);

        OwlTime owlTime = new OwlTime();
        owlTime.initNow();
        owlTime.addToJenaModelOwlTimeInstant(JenaSerialization.instanceModel,worldDate);

        String sourceId = talkUri+author+owlTime.getDateString();
        
        ArrayList<CompositeEvent> semEvents = new ArrayList<CompositeEvent>();
        CompositeEvent compositeEvent = makeCompositeEvent(sourceId, event, actor, owlTime);
        semEvents.add(compositeEvent);
        JenaSerialization.addJenaCompositeEvents( semEvents, VERBOSE_MENTION);

        JenaSerialization.addSourceMetaData(sourceId, author);
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
            JenaSerialization.addPerspectiveToJenaDataSet(ResourcesUri.prov, nafUri, sourceId, perspectiveValues);
        }

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            RDFDataMgr.write(os, JenaSerialization.ds, RDFFormat.TRIG_PRETTY);
            rdfString = new String(os.toByteArray(),"UTF-8");
            os.close();
        } catch (Exception e) {
            //  e.printStackTrace();
        }
        return rdfString;
    }

}

