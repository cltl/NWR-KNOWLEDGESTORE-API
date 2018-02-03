package vu.cltl.triple.write;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import vu.cltl.triple.objects.*;

import java.util.ArrayList;

/**
 * Created by piek on 9/25/14.
 */
public class JenaSerialization {
    static public boolean DEBUG = false;
    static Dataset ds = null;
    static Model graspModel = null;
    static Model instanceModel = null;

    static public void createModels (String world) {
        ds = TDBFactory.createDataset();
        graspModel = ds.getNamedModel(ResourcesUri.grasp + "perspectives");
        instanceModel = ds.getNamedModel("http://cltl.nl/leolani/world/"+"instances");
    }

    static void prefixModels () {
        Model defaultModel = ds.getDefaultModel();
        ResourcesUri.prefixSimpleModel(defaultModel);
        ResourcesUri.prefixModelGrasp(defaultModel);
    }


    static public String addPerspectiveToJenaDataSet (String mentionId, String sourceId, String valueString) {
        /*
            <https://web.archive.org/web/20150906024829/http://www.naturalnews.com/049351_measles_outbreak_MMR_vaccine_Disneyland.html/doc_attribution/attr1_13_22>
            rdf:value               grasp:CERTAIN , grasp:POS , grasp:positive , grasp:FUTURE ;
            grasp:isAttributionFor  <https://web.archive.org/web/20150906024829/http://www.naturalnews.com/049351_measles_outbreak_MMR_vaccine_Disneyland.html#char=1986,1987> , <https://web.archive.org/web/20150906024829/http://www.naturalnews.com/049351_measles_outbreak_MMR_vaccine_Disneyland.html#char=3252,3258> ;
            prov:wasDerivedFrom     <https://web.archive.org/web/20150906024829/http://www.naturalnews.com/049351_measles_outbreak_MMR_vaccine_Disneyland.html> .

         */

        String attrId = sourceId;
        String [] values = valueString.split(";");
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            attrId+=value;

        }
        Resource attributionResource = graspModel.createResource(attrId);

        Resource attrType = graspModel.createResource(ResourcesUri.grasp+"Attribution") ;
        attributionResource.addProperty(RDF.type,attrType );

        Resource mentionResource = graspModel.createResource(mentionId);
        Property property = graspModel.createProperty(ResourcesUri.grasp, "isAttributionFor");
        attributionResource.addProperty(property, mentionResource);

        Resource sourceResource = graspModel.createResource(sourceId);
        property = graspModel.createProperty(ResourcesUri.grasp,"wasDerivedFrom" );
        attributionResource.addProperty(property, sourceResource);

        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            Resource valueResource = graspModel.createResource(ResourcesUri.grasp+value);;
            attributionResource.addProperty(RDF.value, valueResource);
        }
        return attrId;
    }


    static public void addSourceMetaData(String sourceId, String authorUri, String time) {
        Resource subject = graspModel.createResource(sourceId);
        Property property = graspModel.createProperty(ResourcesUri.prov, "wasAttributedTo");
        Resource object = graspModel.createResource(authorUri);
        Resource type = graspModel.createResource(ResourcesUri.grasp+"Chat") ;
        subject.addProperty(property, object);
        object = graspModel.createResource(time);
        subject.addProperty(RDF.type, type);
        subject.addProperty(Sem.hasTime, object);
    }


    static public void addJenaCompositeEvent (
            CompositeEvent compositeEvent,
            boolean VERBOSE_MENTIONS) {

            compositeEvent.getEvent().addToJenaModelLabels(instanceModel, Sem.Event, VERBOSE_MENTIONS);

            //  System.out.println("ACTORS");
            for (int  i = 0; i < compositeEvent.getMySemActors().size(); i++) {
                SemObject semActor = compositeEvent.getMySemActors().get(i);
                semActor.addToJenaModelLabels(instanceModel, Sem.Actor, VERBOSE_MENTIONS);
            }

            for (int j = 0; j < compositeEvent.getMySemRelations().size(); j++) {
                SemRelation semRelation = compositeEvent.getMySemRelations().get(j);
                semRelation.addSemToJenaDataSet(instanceModel, ds, VERBOSE_MENTIONS);
            }
    }

    static public void addJenaCompositeEvents (
            ArrayList<CompositeEvent> compositeEvents,
            boolean VERBOSE_MENTIONS) {
        for (int c = 0; c < compositeEvents.size(); c++) {
            CompositeEvent compositeEvent = compositeEvents.get(c);
            addJenaCompositeEvent( compositeEvent, VERBOSE_MENTIONS);
        }
    }

    static public void addJenaSemRelation (SemRelation semRelation, boolean VERBOSE_MENTIONS) {
        semRelation.addSemToJenaDataSet(instanceModel, ds, VERBOSE_MENTIONS);
    }
    static public void addJenaRelation (SemRelation semRelation, boolean VERBOSE_MENTIONS) {
        semRelation.addSemToJenaDataSet(instanceModel, ds, VERBOSE_MENTIONS);
    }

    static public void addJenaObject (SemObject subject, String type, boolean VERBOSE_MENTIONS) {
        Resource typeResource = null;
        if (!type.isEmpty()) {
            typeResource = instanceModel.createResource(type);
        }
        subject.addToJenaModelLabels(instanceModel, typeResource, VERBOSE_MENTIONS);
    }

  










}
