package vu.cltl.triple.write;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.kyotoproject.kaf.KafFactuality;
import org.openrdf.model.vocabulary.RDF;
import vu.cltl.triple.objects.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 9/25/14.
 */
public class JenaSerialization {
    static public boolean DEBUG = false;
    static Dataset ds = null;
    static Model graspModel = null;
    static Model provenanceModel = null;
    static Model instanceModel = null;



    static public void createModels () {
        ds = TDBFactory.createDataset();
        graspModel = ds.getNamedModel(ResourcesUri.nwr + "grasp");
        provenanceModel = ds.getNamedModel(ResourcesUri.nwr + "provenance");
        instanceModel = ds.getNamedModel(ResourcesUri.nwr+"instances");
        prefixModels();
    }

    static public void createSimpleModels () {
        ds = TDBFactory.createDataset();
        graspModel = ds.getNamedModel(ResourcesUri.nwr + "grasp");
        provenanceModel = ds.getNamedModel(ResourcesUri.nwr + "provenance");
        instanceModel = ds.getNamedModel(ResourcesUri.nwr+"instances");
        prefixSimpleModels();
    }


    static void prefixModels () {
        Model defaultModel = ds.getDefaultModel();
        ResourcesUri.prefixModel(defaultModel);
        ResourcesUri.prefixModelNwr(defaultModel);
        ResourcesUri.prefixModelGaf(defaultModel);
        ResourcesUri.prefixModelGaf(provenanceModel);
        ResourcesUri.prefixModel(instanceModel);
        ResourcesUri.prefixModelNwr(instanceModel);
        ResourcesUri.prefixModelGaf(instanceModel);

    }

    static void prefixSimpleModels () {
        Model defaultModel = ds.getDefaultModel();
        ResourcesUri.prefixSimpleModel(defaultModel);
        ResourcesUri.prefixModelNwr(defaultModel);
        ResourcesUri.prefixModelGaf(defaultModel);
        ResourcesUri.prefixModelGaf(provenanceModel);
        ResourcesUri.prefixSimpleModel(instanceModel);
        ResourcesUri.prefixModelNwr(instanceModel);
        ResourcesUri.prefixModelGaf(instanceModel);

    }

    static public void addJenaPerspectiveObjects(String attrBase, String namespace, String property,
                                            ArrayList<PerspectiveObject> perspectiveObjects, int cnt) {
        HashMap<String, ArrayList<PerspectiveObject>> map = new HashMap<String, ArrayList<PerspectiveObject>>();
        for (int i = 0; i < perspectiveObjects.size(); i++) {
            PerspectiveObject perspectiveObject = perspectiveObjects.get(i);
            String source = perspectiveObject.getSourceEntity().getURI();
            if (map.containsKey(source)) {
                ArrayList<PerspectiveObject> sourcePerspectives = map.get(source);
                sourcePerspectives.add(perspectiveObject);
                map.put(source, sourcePerspectives);
            }
            else {
                ArrayList<PerspectiveObject> sourcePerspectives = new ArrayList<PerspectiveObject>();
                sourcePerspectives.add(perspectiveObject);
                map.put(source, sourcePerspectives);
            }
        }

        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        int kCnt = 0;
        while (keys.hasNext()) {
            String key = keys.next();
            kCnt++;
            String attrId = attrBase + "attr"+kCnt+"_" + cnt;
            ArrayList<PerspectiveObject> sourcePerspectives = map.get(key);
            addToJenaDataSet(graspModel, namespace, property, attrId, sourcePerspectives, key);
        }
    }



    static public void addPerspectiveToJenaDataSet (String ns, String attrId, String eventId, ArrayList<String> values) {
        /*
        mentionId2      hasAttribution         attributionId1
                        gaf:generatedBy        mentionId3
        attributionId1  rdf:value              CERTAIN_POS_FUTURE
                        rdf:value              POSITIVE
                        prov:wasAttributedTo   doc-uri
                        gaf:wasAttributedTo    dbp:Zetsche

         */

        Resource sourceResource = graspModel.createResource(eventId);
        Resource attributionResource = graspModel.createResource(attrId);
        Property property = graspModel.createProperty(ns, "wasAttributedTo");
        attributionResource.addProperty(property, sourceResource);
        Property valueProperty = graspModel.createProperty(ResourcesUri.grasp,"hasAttribution" );

        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            Resource valueResource = graspModel.createResource(ResourcesUri.grasp+value);;
            attributionResource.addProperty(valueProperty, valueResource);
        }
    }

    static public void addToJenaDataSet (Model model, String ns, String property,
                                         String attrId, ArrayList<PerspectiveObject> perspectives, String sourceURI) {
        /*
        mentionId2      hasAttribution         attributionId1
                        gaf:generatedBy        mentionId3
        attributionId1  rdf:value              CERTAIN_POS_FUTURE
                        rdf:value              POSITIVE
                        prov:wasAttributedTo   doc-uri
                        gaf:wasAttributedTo    dbp:Zetsche

         */
        /*
http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=254,261
	gafAttribution:CERTAIN,NON_FUTURE
		http://dbpedia.org/resource/Caesars_Entertainment_Corporation ;
		gaf:generatedBy http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209.


http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209
			gafAttribution:CERTAIN,NON_FUTURE
				doc-uri;

doc-uri
	prov:wasAttributedTo author;
	prov:wasAttributedTo journal.
         */

        // System.out.println("sourceURI = " + sourceURI);
        // System.out.println("perspectives = " + perspectives.size());
        HashMap<String, ArrayList<NafMention>> mentionMap = new HashMap<String, ArrayList<NafMention>>();
        for (int p = 0; p < perspectives.size(); p++) {
            PerspectiveObject perspectiveObject = perspectives.get(p);
            if ((perspectiveObject.getTargetEventMentions().size()>0)) {
                //// We collect all factualities from all the target mentions for this perspective
                ArrayList<KafFactuality> allFactualities = new ArrayList<KafFactuality>();
                for (int i = 0; i < perspectiveObject.getTargetEventMentions().size(); i++) {
                    NafMention mention = perspectiveObject.getTargetEventMentions().get(i);
                    ////
                    for (int j = 0; j < mention.getFactuality().size(); j++) {
                        KafFactuality kafFactuality = mention.getFactuality().get(j);
                        allFactualities.add(kafFactuality);
                    }
                }
                KafFactuality kafFactuality = NafMention.getDominantFactuality(allFactualities);
                ArrayList<String> factualityStringArray = KafFactuality.castToDefault();
                //System.out.println("factualityStringArray.toString() = " + factualityStringArray.toString());
                if (allFactualities.size() > 0) {
                    factualityStringArray= kafFactuality.getPredictionArrayList();
                    // System.out.println("factualityStringArray.toString() = " + factualityStringArray.toString());
                }
                String sentiment = NafMention.getDominantOpinion(perspectiveObject.getTargetEventMentions());
                if (!sentiment.isEmpty()) {
                    if (sentiment.equals("+")) {
                        factualityStringArray.add("positive");
                    } else if (sentiment.equals("-")) {
                        factualityStringArray.add("negative");
                    }
                }

                if (factualityStringArray.size()>0) {
                    String valueAray = "";
                    for (int i = 0; i < factualityStringArray.size(); i++) {
                        String v = factualityStringArray.get(i);
                        if (!valueAray.isEmpty())  valueAray+=",";
                        valueAray += v;
                    }
                    if (mentionMap.containsKey(valueAray)) {
                        ArrayList<NafMention> mentions = mentionMap.get(valueAray);
                        mentions.addAll(perspectiveObject.getTargetEventMentions());
                        mentionMap.put(valueAray, mentions);
                    }
                    else {
                        ArrayList<NafMention> mentions = perspectiveObject.getTargetEventMentions();
                        mentionMap.put(valueAray, mentions);
                    }
                }
                else {
                    //   System.out.println(" No perspectives for:"+sourceURI);
                }
            }
            else {
                //   System.out.println("no target mentions");
            }
        }
        // System.out.println("mentionMap.size() = " + mentionMap.size());
        if (mentionMap.size()>0) {
            int nAttribution = 0;
            Set keySet = mentionMap.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                nAttribution++;
                Resource sourceResource = model.createResource(sourceURI);
                Property aProperty = model.createProperty(ns, property);
                Resource attributionSubject = model.createResource(attrId+"_"+nAttribution);
                attributionSubject.addProperty(aProperty, sourceResource);
                // System.out.println("key = " + key);
                ArrayList<NafMention> mentions = mentionMap.get(key);
                String[] factValues = key.split(",");
                for (int i = 0; i < factValues.length; i++) {
                    String factValue = factValues[i];
                    Resource factualityResource = model.createResource(ResourcesUri.grasp + factValue);
                    aProperty = model.createProperty(RDF.NAMESPACE, RDF.VALUE.getLocalName());
                    attributionSubject.addProperty(aProperty, factualityResource);
                }
                for (int j = 0; j < mentions.size(); j++) {
                    NafMention nafMention = mentions.get(j);
                    ////
                    Resource mentionObject = model.createResource(nafMention.toString());
                    aProperty = model.createProperty(ResourcesUri.grasp, "isAttributionFor");
                    attributionSubject.addProperty(aProperty, mentionObject);
                }
            }
        }
    }


    static public void addJenaPerspectiveObjectsArray(String attrBase, String namespace,
                                            ArrayList<PerspectiveObject> perspectiveObjects, int cnt) {
        for (int i = 0; i < perspectiveObjects.size(); i++) {
            PerspectiveObject perspectiveObject = perspectiveObjects.get(i);
            String attrId = attrBase+"Attr"+cnt+i;
            perspectiveObject.addToJenaDataSet(graspModel, namespace, attrId);
        }
    }

    static public void addSourceMetaData(String sourceId, String authorUri) {
        Resource subject = graspModel.createResource(sourceId);
        Property property = graspModel.createProperty(ResourcesUri.prov, "wasAttributedTo");
        Resource object = graspModel.createResource(authorUri);
        subject.addProperty(property, object);
    }


    static public void addJenaCompositeEvent (
            String sourceId,
            OwlTime owlTime,
            CompositeEvent compositeEvent,
            boolean VERBOSE_MENTIONS) {

            compositeEvent.getEvent().addToJenaModel(instanceModel, Sem.Event, VERBOSE_MENTIONS);

            //  System.out.println("ACTORS");
            for (int  i = 0; i < compositeEvent.getMySemActors().size(); i++) {
                SemActor semActor = (SemActor) compositeEvent.getMySemActors().get(i);
                semActor.addToJenaModel(instanceModel, Sem.Actor, VERBOSE_MENTIONS);
            }

            owlTime.addToJenaModelOwlTimeInstant(instanceModel, sourceId);
            for (int j = 0; j < compositeEvent.getMySemRelations().size(); j++) {
                SemRelation semRelation = compositeEvent.getMySemRelations().get(j);
                semRelation.addSemToJenaDataSet(ds, provenanceModel);
            }
    }

    static public void addJenaCompositeEvents (
            String sourceId,
            OwlTime owlTime,
            ArrayList<CompositeEvent> compositeEvents,
            boolean VERBOSE_MENTIONS) {
        for (int c = 0; c < compositeEvents.size(); c++) {
            CompositeEvent compositeEvent = compositeEvents.get(c);
            addJenaCompositeEvent(sourceId, owlTime, compositeEvent, VERBOSE_MENTIONS);
        }
    }

  










}
