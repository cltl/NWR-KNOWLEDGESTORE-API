package vu.cltl.triple.write;

import com.hp.hpl.jena.query.Dataset;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import vu.cltl.triple.objects.*;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class CreateGraspTriples {
    static boolean VERBOSE_MENTION = true;
    static public final String talkUri = "http://cltl.nl/leolani/talk/";
    static public final String friendsUri = "http://cltl.nl/leolani/friends/";
    static public final String worldUri = "http://cltl.nl/leolani/world/";
    static public final String worldDate = "http://cltl.nl/leolani/date/";
    static Integer offsetStart = 0;
    

    static String testparameters1 = "--source chat1 --turn 1 --author-name Piek " +
            "--subject-label bite --subject-type http://www.newsreader-project.eu/domain-ontology#Attack " +
            "--predicate-uri http://semanticweb.cs.vu.nl/2009/11/sem/hasActor " +
            "--object-label rabbit --object-type http://dbpedia.org/resource/Animal " +
            "--perspective CERTAIN;SCARED;NEGATIVE;BELIEF";

    static String testparameters2 = "--source chat1 --turn 1 --author-name Piek " +
            "--subject-label Selene --subject-type http://dbpedia.org/resource/Person " +
            "--predicate-uri comesFrom " +
            "--object-label Mexico --object-type http://dbpedia.org/resource/Country " +
            "--perspective CERTAIN;SCARED;NEGATIVE;BELIEF";
    static public void main (String[] args) {
        String authorName = "";
        String authorURI = "";
        String sourceId = "";
        String predicate = "";
        String perspective = "";
        String subjectLabel = "";
        String subjectUri = "";
        String subjectType = "";
        String objectLabel = "";
        String objectUri = "";
        String objectType = "";
        String turn = "";
        offsetStart = 0;
        if (args.length==0) {
           // args = testparameters1.split(" ");
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
            else if (arg.equals("--turn") && args.length>(i+1)) {
                turn = args[i+1];
            }
            else if (arg.equals("--subject-label") && args.length>(i+1)) {
                subjectLabel = args[i+1];
            }
            else if (arg.equals("--subject-uri") && args.length>(i+1)) {
                subjectLabel = args[i+1];
            }
            else if (arg.equals("--subject-type") && args.length>(i+1)) {
                subjectType = args[i+1];
            }
            else if (arg.equals("--predicate-uri") && args.length>(i+1)) {
                predicate = args[i+1];
            }
            else if (arg.equals("--object-label") && args.length>(i+1)) {
                objectLabel = args[i+1];
            }
            else if (arg.equals("--object-uri") && args.length>(i+1)) {
                objectUri = args[i+1];
            }
            else if (arg.equals("--object-type") && args.length>(i+1)) {
                objectType = args[i+1];
            }
            else if (arg.equals("--perspective") && args.length>(i+1)) {
                perspective = args[i+1];
            }
        }
        if (authorURI.isEmpty()) {
            authorURI = friendsUri + authorName;
        }
        if (!predicate.isEmpty()) {
            Dataset dataset  = null;
            dataset = CreateGraspTriples.graspDataSet(sourceId, turn, authorURI, authorName, perspective, subjectLabel, subjectUri, subjectType, predicate, objectLabel, objectUri, objectType);
            try {
                File ofile = new File ("leolani.rdf");
                if (ofile.exists()) {
                   // TrigUtil.mergeWithDataSet(dataset, ofile);
                }
                OutputStream fos = new FileOutputStream(ofile);
                RDFDataMgr.write(fos, dataset, RDFFormat.TRIG_PRETTY);
/*
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                String rdfString = new String(os.toByteArray(),"UTF-8");
                System.out.println("rdfString = " + rdfString);
                os.close();
*/
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /*

     */
    static CompositeEvent makeCompositeEvent (String sourceId,
                                              String turn,
                                              Integer sentenceId,
                                              Integer paragraphId,
                                              Integer offsetStart,
                                              Integer offsetEnd,
                                              String eventLabel,
                                              String eventUri,
                                              String actorLabel,
                                              String actorUri,
                                              OwlTime owlTime) {
        CompositeEvent compositeEvent = new CompositeEvent();
        SemObject semEvent = makeSemObject(sourceId, eventLabel, eventUri, "EVENT");
        compositeEvent.setEvent(semEvent);
        SemObject semObject = makeSemObject(sourceId, actorLabel, actorUri, "ACTOR");
        compositeEvent.addMySemActor(semObject);
        String statementId = sourceId+"statement"+turn;
        SemRelation semRelation = makeSemRelation(sourceId, statementId,
                semEvent, Sem.hasActor.getLocalName() , semObject,sentenceId, paragraphId,  offsetStart, offsetEnd);
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
                                        String statementId,
                                        SemObject subject,
                                        String property,
                                        SemObject object,
                                        Integer sentenceId,
                                        Integer paragraphId,
                                        Integer offsetStart,
                                        Integer offsetEnd) {
               SemRelation semRelation = new SemRelation();
               semRelation.setId(statementId);
               semRelation.setSubject(subject.getURI());
               semRelation.addPredicate(property);
               semRelation.setObject(object.getURI());
               NafMention nafMention = new NafMention();
               nafMention.setBaseUri(sourceId+"#");
               nafMention.setOffSetStart(offsetStart.toString());
               nafMention.setOffSetEnd(offsetEnd.toString());
               if (sentenceId>0) nafMention.setSentence(sentenceId.toString());
               if (paragraphId>0) nafMention.setParagraph(paragraphId.toString());
               semRelation.addMention(nafMention);
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


    static public String graspEntityTripleString (String sourceId,
                                            String turn,
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

        Dataset ds = graspDataSet(sourceId, turn, authorUri, authorName, perspectiveValues, subjectLabel,subjectUri,subjectType, predicateUri, objectLabel,objectUri, objectType);

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

    static public Dataset graspDataSet (     String sourceId,
                                             String turn,
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

        String sourceUri = talkUri+sourceId+authorName+owlTime.getDateString();

        if (subjectUri.isEmpty()) {
           // subjectUri = worldUri + authorName + owlTime.getDateString() + subjectLabel;
            subjectUri = worldUri + subjectLabel;
        }
        if (objectUri.isEmpty()) {
          //  objectUri = worldUri + authorName + owlTime.getDateString() + objectLabel;
            objectUri = worldUri + objectLabel;
        }

        SemObject subject = makeSemObject(sourceUri, subjectLabel, subjectUri, "ENTITY");
        SemObject object = makeSemObject(sourceUri, objectLabel, objectUri, "ENTITY");

        JenaSerialization.addJenaObject( subject, subjectType, VERBOSE_MENTION);
        JenaSerialization.addJenaObject( object, objectType,VERBOSE_MENTION);

        Integer sentence = 1;
        Integer paragraph = Integer.parseInt(turn);
        Integer offsetStart = 0;
        Integer offSetEnd = subjectLabel.length()+objectLabel.length();
        //String statementId = sourceUri+"statement"+turn;
        /// how unique should this URI be?
        /// we want to capture different attributions for the same triple so basically it is a triple ID

        String statementId = talkUri+subject.getURI()+predicateUri+object.getURI();
        String checkSum = getCheckSum(subject.getURI()+predicateUri+object.getURI());
        if (checkSum!=null) {
            statementId = talkUri+checkSum;
        }
        SemRelation semRelation = makeSemRelation(sourceUri, statementId, subject, predicateUri, object, sentence, paragraph, offsetStart, offSetEnd);

        JenaSerialization.addJenaRelation( semRelation, VERBOSE_MENTION);


        JenaSerialization.addSourceMetaData(sourceUri, authorUri, "http://cltl.nl/leolani/date/"+owlTime.getDateString());

        ArrayList<NafMention> mentions = semRelation.getNafMentions();
        for (int m = 0; m < mentions.size(); m++) {
            NafMention nafMention =  mentions.get(m);
            String nafUri = "";
            if (VERBOSE_MENTION) {
                nafUri = nafMention.toStringFull();
            }
            else {
                nafUri = nafMention.toString();
            }
            JenaSerialization.addPerspectiveToJenaDataSet(nafUri, sourceUri, perspectiveValues);
        }

        return JenaSerialization.ds;
    }

    static String getCheckSum(String str) {
        StringBuffer sb = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] digest = md.digest();
            sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    //////////////////////
    /*
        static public String graspEventTripleString (String sourceId,
                                                String turn,
                                                String authorUri,
                                         String authorName,
                                         String perspectiveValues,
                                         String subjectLabel,
                                         String subjectUri,
                                         String objectLabel,
                                         String objectUri) {

            String rdfString = "";

            Dataset ds = graspDataSet(sourceId, turn, authorUri, authorName, perspectiveValues, subjectLabel,subjectUri,objectLabel,objectUri);

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
    */

    /*static public Dataset graspDataSet (String sourceId, String turn, String authorUri,
                                              String authorName,
                                              String perspectiveValues,
                                              String subjectLabel,
                                              String subjectUri,
                                              String objectLabel,
                                              String objectUri) {

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

         String sourceUri = talkUri+sourceId+authorName+owlTime.getDateString();

         ArrayList<CompositeEvent> semEvents = new ArrayList<CompositeEvent>();
         if (subjectUri.isEmpty()) {
             subjectUri = worldUri + authorName + owlTime.getDateString() + subjectLabel;
         }
         if (objectUri.isEmpty()) {
             objectUri = worldUri + objectLabel;
         }
         Integer sentence = 1;
         Integer paragraph = Integer.parseInt(turn);
         Integer offsetStart = 0;
         Integer offSetEnd = subjectLabel.length()+objectLabel.length();
         CompositeEvent compositeEvent = makeCompositeEvent(sourceUri, turn, sentence, paragraph, offsetStart, offSetEnd, subjectLabel, subjectUri, objectLabel, objectUri, owlTime);
         semEvents.add(compositeEvent);
         JenaSerialization.addJenaCompositeEvents( semEvents, VERBOSE_MENTION);

         JenaSerialization.addSourceMetaData(sourceUri, authorUri, "http://cltl.nl/leolani/date/"+owlTime.getDateString());
         for (int i = 0; i < compositeEvent.getMySemRelations().size(); i++) {
             SemRelation semRelation = compositeEvent.getMySemRelations().get(i);
             ArrayList<NafMention> mentions = semRelation.getNafMentions();
             for (int m = 0; m < mentions.size(); m++) {
                 NafMention nafMention =  mentions.get(m);
                 String nafUri = "";
                 if (VERBOSE_MENTION) {
                     nafUri = nafMention.toStringFull();
                 }
                 else {
                     nafUri = nafMention.toString();
                 }
                 JenaSerialization.addPerspectiveToJenaDataSet(nafUri, sourceUri, perspectiveValues);
             }
         }

         return JenaSerialization.ds;
     }*/


}

