# NWR-TRIPLE-API
This module handles the interaction with the NewsReader KnowledgeStore. It supports both querying and posting as functions.

1. Querying

Queries the KnowledgeStore populated with NewsReader output or the TRiG files and represents the result as SEM-RDF or SEM-JSON

2. Posting Triples to the KnowledgeStore

You can post any ArrayList of Statements to the static address of the KnowledgeStore

vu.cltl.triple.write.WriteStatementsKnowledgeStore.storeTriples

static public void storeTriples (ArrayList<Statement> statements)
    
public static String ksAddress = "http://145.100.59.153:50053/";


3. Creating GRASP triples from communication

vu.cltl.triple.write.CreateGraspTriples.graspTripleString

static public String graspTripleString (String authorUri,
                                     String authorName,
                                     String perspectiveValues,
                                     String eventLabel,
                                     String eventUri,
                                     String actorLabel,
                                     String actorUri)
