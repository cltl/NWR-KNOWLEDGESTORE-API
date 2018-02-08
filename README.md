# NWR-TRIPLE-API
This module handles the interaction with the NewsReader KnowledgeStore. It supports both querying and posting as functions.

1. Querying

Queries the KnowledgeStore populated with NewsReader output or the TRiG files and represents the result as SEM-RDF or SEM-JSON

2. Posting Triples to the KnowledgeStore

You can post any ArrayList of Statements to the static address of the KnowledgeStore

vu.cltl.triple.write.WriteStatementsKnowledgeStore.storeTriples

static public void storeTriples (ArrayList<Statement> statements)


3. Creating GRASP triples from communication

vu.cltl.triple.write.CreateGraspTriples.graspTripleString

static public String graspTripleString (String sourceId, 
                                     String authorUri,
                                     String authorName,
                                     String perspectiveValues,
                                     String eventLabel,
                                     String eventUri,
                                     String actorLabel,
                                     String actorUri)

static public Dataset graspDataSet (String sourceId, String authorUri,
                                             String authorName,
                                             String perspectiveValues,
                                             String eventLabel,
                                             String eventUri,
                                             String actorLabel,
                                             String actorUri)
                                             
- sourceId = unique identifier for the communication event itself, e.g. today's chat at 11am between Piek and Leolani
- authorUri = Can be empty. If empty, Leolani will create a URI using the author name and <http://cltl.nl/leolani/friends/<NAME>>. If non empty, it will use whatever URI is provided.
- authorName = The name to address the author
- perspectiveValues = Values for perspectives concatentated with ";". This is free text now and will be formally defined in GRaSP.
- eventLabel = the word used to make reference to an event, e.g. "bite"
- eventUri = if the event itself is known provide the URI, otherwise leave empty and Leolani will create a unique identifier using the sourceId, author, time of conversation and the label
- actorLabel = the word used to make reference to the actor
- actorUri = the URI of the actor. If empty, a URI is created from the label


The storeGraspStatementInKnowledgeStore class shows how you can create the GRaSP triples and send them to the KnowledgeStore.

Usage:
--ks               IP address of the knowledge store
--source           unique identifier for the chat
--turn             unique identifier for the turn within the chat
--author-uri       <OPTIONAL> uri for the author of the turn. If omitted a uri is created from the author-name
--author-name      name of the author of the turn
--subject-label    word that represents the subject label
--subject-uri      <OPTIONAL>uri that represents the referent for the subject. If omitted a global uri is created from the label
--subject-type     <OPTIONAL>uri that represents the CLASS for the subject. CLASS information is not considered a statement but as interpretation
--predicate-uri    uri that represents the predicate
--object-label     word that represents the object label
--object-uri       <OPTIONAL>uri that represents the referent for the object. If omitted a global uri is created from the label
--object-type      <OPTIONAL>uri that represents the CLASS for the subject. CLASS information is not considered as a statement but as interpretation
--perspective      <OPTIONAL>list of perspective values separated by semicolons
--debug            <OPTIONAL>print out debug statements

Example:

static String testparameters4 = "--ks <address> --source chat2 --turn 2 --author-name Piek" +
        " --subject-label Selene --subject-type http://dbpedia.org/resource/Person" +
        " --predicate-uri http://www.semanticweb.org/leolani/N2MY/knows" +
        " --object-label Piek --object-type http://dbpedia.org/resource/Person" +
        " --perspective LIKE" +
        " --debug"
        ;

@prefix eso:   <http://www.newsreader-project.eu/domain-ontology#> .
@prefix leolaniFriends: <http://cltl.nl/leolani/friends/> .
@prefix leolaniWorld: <http://cltl.nl/leolani/world/> .
@prefix owl:   <http://www.w3.org/2002/07/owl#> .
@prefix ili:   <http://globalwordnet.org/ili/> .
@prefix fn:    <http://www.newsreader-project.eu/ontologies/framenet/> .
@prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix pb:    <http://www.newsreader-project.eu/ontologies/propbank/> .
@prefix leolaniTalk: <http://cltl.nl/leolani/talk/> .
@prefix leolaniTime: <http://cltl.nl/leolani/date/> .
@prefix dbp:   <http://dbpedia.org/resource/> .
@prefix grasp: <http://groundedannotationframework.org/grasp#> .
@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix wn:    <http://www.newsreader-project.eu/ontologies/pwn3.0/> .
@prefix graspAttribution: <http://groundedannotationframework.org/grasp/attribution#> .
@prefix sem:   <http://semanticweb.cs.vu.nl/2009/11/sem/> .
@prefix time:  <http://www.w3.org/TR/owl-time#> .
@prefix prov:  <http://www.w3.org/ns/prov#> .
@prefix graspSentiment: <http://groundedannotationframework.org/grasp/sentiment#> .

{ 
}

leolaniWorld:instances {
    leolaniTime:20180208
            a              time:DateTimeDescription ;
            time:day       "---08"^^<http://www.w3.org/2001/XMLSchema#gDay> ;
            time:month     "--02"^^<http://www.w3.org/2001/XMLSchema#gMonth> ;
            time:unitType  time:unitDay ;
            time:year      "2018"^^<http://www.w3.org/2001/XMLSchema#gYear> .
    
    leolaniWorld:Selene
            a                dbp:Person ;
            rdfs:label       "Selene:1" ;
            grasp:denotedBy  <http://cltl.nl/leolani/talk/chat2Piek20180208#char=0,6> ;
            skos:prefLabel   "Selene" .
    
    leolaniWorld:Piek
            a                dbp:Person ;
            rdfs:label       "Piek:1" ;
            grasp:denotedBy  <http://cltl.nl/leolani/talk/chat2Piek20180208#char=6,4> ;
            skos:prefLabel   "Piek" .
    
    leolaniTalk:e9d43662e71481ee2fbca9d266e6b199
            a                grasp:Statement ;
            grasp:denotedBy  <http://cltl.nl/leolani/talk/chat2Piek20180208#char=0,10&sentence=1&paragraph=2> .
}

leolaniTalk:e9d43662e71481ee2fbca9d266e6b199 {
    leolaniWorld:Selene
            <http://www.semanticweb.org/leolani/N2MY/knows>
                    leolaniWorld:Piek .
}

grasp:perspectives {
    leolaniTalk:chat2Piek20180208
            a                     grasp:Chat ;
            sem:hasTime           leolaniTime:20180208 ;
            prov:wasAttributedTo  leolaniFriends:Piek .
    
    leolaniTalk:chat2Piek20180208LIKE
            a                       grasp:Attribution ;
            rdf:value               grasp:LIKE ;
            grasp:isAttributionFor  <http://cltl.nl/leolani/talk/chat2Piek20180208#char=0,10&sentence=1&paragraph=2> ;
            grasp:wasDerivedFrom    leolaniTalk:chat2Piek20180208 .
}

The next query gives all triples stated by authors with the perspective values expressed.

SPARQL query:

prefix grasp: <http://groundedannotationframework.org/grasp#>

SELECT ?type ?s ?p ?o ?v ?author
WHERE {
	GRAPH ?g { ?s ?p ?o . }
	?s rdf:type ?type .
	
	?g grasp:denotedBy ?m .
	?a grasp:isAttributionFor ?m .
	?a rdf:value ?v .
	?a grasp:wasDerivedFrom ?turn .
	?turn prov:wasAttributedTo ?author .
}

