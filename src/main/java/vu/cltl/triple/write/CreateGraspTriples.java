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

    static final String talkUri = "http://cltl.nl/leolani/talk/";
    static final String friendsUri = "http://cltl.nl/leolani/friends/";
    static final String worldUri = "http://cltl.nl/leolani/world/";

    static public void main (String[] args) {
        String talk = talkUri+"chat1";
        String author = friendsUri+"Piek";
        ArrayList<String> values = new ArrayList<String>();
        values.add("CERTAIN");
        values.add("SCARED");
        values.add("NEGATIVE");
        values.add("BELIEF");
        String str = graspTripleString(author, values,talk, "bite", "rabbit");
        try {
            OutputStream fos = new FileOutputStream("leolani.rdf");
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static CompositeEvent makeCompositeEvent (String sourceId, String eventLabel, String actorLabel, OwlTime owlTime) {
        CompositeEvent compositeEvent = new CompositeEvent();

        SemEvent semEvent = new SemEvent();
        semEvent.setId(worldUri+eventLabel);
        semEvent.addPhraseCounts(eventLabel);
        compositeEvent.setEvent(semEvent);

        SemActor semObject1 = new SemActor("ACTOR");
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
        timeRelation.setObject(owlTime.getDateStringURI(sourceId));
        compositeEvent.addMySemRelation(timeRelation);

        return compositeEvent;

    }

    static PerspectiveObject makePerspectiveSourceObject(SemObject semEvent,
                                                             String sourceUri) {
        PerspectiveObject perspectiveObject = new PerspectiveObject();
        perspectiveObject.setDocumentUri(sourceUri);
        perspectiveObject.setPredicateId(semEvent.getURI());
        perspectiveObject.setEventString(semEvent.getPhrase());
/*
        KafParticipant sourceParticipant = new KafParticipant();
        KafParticipant targetParticipant = new KafParticipant();
        perspectiveObject.setSource(sourceParticipant);
        perspectiveObject.setTarget(targetParticipant);
*/
        return perspectiveObject;
    }

    static PerspectiveObject makePerspectiveAuthorObject(SemActor semActor,
                                                         SemObject semEvent,
                                                         String sourceUri) {
        PerspectiveObject perspectiveObject = new PerspectiveObject();
        perspectiveObject.setDocumentUri(sourceUri);
        perspectiveObject.setSourceEntity(semActor); /// this is not the document itself
        perspectiveObject.setPredicateId(semEvent.getURI());
        perspectiveObject.setEventString(semEvent.getPhrase());
        return perspectiveObject;
    }

/*    static Statement setProperty (String obj, Model model, URI subject, URI property) {
        Statement statement =
        try {
           String objUri = URLEncoder.encode(obj, "UTF-8");
           Resource object = model.createResource(ResourcesUri.nwrauthor+objUri);
           subject..addProperty(property, object);
        } catch (UnsupportedEncodingException e) {
           //  e.printStackTrace();
        }

    }*/

    static String graspTripleString (String author,
                                     ArrayList<String> perspectiveValues,
                                     String sourceId,
                                     String event,
                                     String actor) {


        JenaSerialization.createModels();
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniWorld", "http://cltl.nl/leolani/world/");
        JenaSerialization.ds.getDefaultModel().setNsPrefix("leolaniTalk", "http://cltl.nl/leolani/talk/");

        SemActor semAuthor = new SemActor("ACTOR");
        semAuthor.setId(author);
        semAuthor.setLabel(author);
        String rdfString = "";
        OwlTime owlTime = new OwlTime();
        owlTime.initNow();

        ArrayList<CompositeEvent> semEvents = new ArrayList<CompositeEvent>();
        CompositeEvent compositeEvent = makeCompositeEvent(sourceId, event, actor, owlTime);
        semEvents.add(compositeEvent);

        PerspectiveObject perspectiveObject = makePerspectiveAuthorObject(semAuthor,
                compositeEvent.getEvent(),
                sourceId);
        String attrBase = sourceId+"/"+"doc_attribution/";
        perspectiveObject.addToJenaDataSet(JenaSerialization.graspModel, ResourcesUri.prov, attrBase);

        JenaSerialization.addJenaCompositeEvents(sourceId, owlTime, semEvents, false);
        JenaSerialization.addSourceMetaData(sourceId, author);
        JenaSerialization.addPerspectiveToJenaDataSet(ResourcesUri.prov, compositeEvent.getEvent().getURI(), sourceId, perspectiveValues);
        /*String attrBase = sourceId+"/"+"source_attribution/";
        JenaSerialization.addJenaPerspectiveObjects(attrBase, ResourcesUri.grasp, "wasAttributedTo",sourcePerspectiveObjects, 1);

        attrBase = sourceId+"/"+"doc_attribution/";
        JenaSerialization.addJenaPerspectiveObjects(attrBase,
                ResourcesUri.prov, "wasDerivedFrom",
                authorPerspectiveObjects,
                sourcePerspectiveObjects.size()+1);*/

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

