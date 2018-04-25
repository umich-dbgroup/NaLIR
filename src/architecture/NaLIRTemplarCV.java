package architecture;

import com.esotericsoftware.minlog.Log;
import components.FeedbackGenerator;
import dataStructure.Query;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.umich.templar.config.TemplarConfig;
import edu.umich.templar.db.Database;
import edu.umich.templar.db.MatchedDBElement;
import edu.umich.templar.db.el.AttributeAndPredicate;
import edu.umich.templar.db.el.DBElement;
import edu.umich.templar.log.LogCountGraph;
import edu.umich.templar.log.LogLevel;
import edu.umich.templar.log.graph.LogGraph;
import edu.umich.templar.log.graph.LogGraphTree;
import edu.umich.templar.main.settings.Params;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import rdbms.RDBMS;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NaLIRTemplarCV {
    public static Document tokens;
    public static LexicalizedParser lexiParser;
    public static Query query;

    public static void execute(RDBMS db, Database templarDB, String queryInput, LogGraph logGraph) throws Exception {
        query = new Query(queryInput, db.schemaGraph);
        components.StanfordNLParser.parse(query, lexiParser);
        components.NodeMapper.phraseProcess(query, db, tokens, templarDB, logGraph);

        // In entity resolution, we remove tokens that refer to the same entity
        components.EntityResolution.entityResolute(query);

        /* Handle join paths */
        List<DBElement> els = new ArrayList<>();
        for (MatchedDBElement mel : query.interp.getElements()) {
            els.add(logGraph.modifyElementForLevel(mel.getEl()));
        }

        LogGraph logGraphClone = logGraph.deepClone();
        logGraphClone.forkSchemaGraph(els, new ArrayList<>());
        LogGraphTree steinerTree = logGraphClone.steiner(els);
        query.interp.setJoinPath(steinerTree.getJoinPath());
        System.out.println(query.interp.getJoinPath());

        /* Done handling join paths */

        components.TreeStructureAdjustor.treeStructureAdjust(query, db);
        components.Explainer.explain(query);
        components.SQLTranslator.translate(query, db);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: NaLIRTemplarCV <testset> <full/no_const/no_const_op>");
            System.exit(1);
        }
        String dbName = args[0];
        String prefix = "/Users/cjbaik/dev/templar/data/" + dbName + "/" + dbName;
        String nlqFile = prefix + "_all_nalir.nlqs";
        String sqlFile = prefix + "_all.sqls";
        String fkpkFile = prefix + ".fkpk.json";
        String mainAttrsFile = prefix + ".main_attrs.json";
        String projAttrsFile = prefix + ".proj_attrs.json";
        // String candCacheFilename = prefix + ".cands.cache";

        LogLevel logLevel = null;
        if (args[1].equalsIgnoreCase("full")) {
            logLevel = LogLevel.FULL;
        } else if (args[1].equalsIgnoreCase("no_const")) {
            logLevel = LogLevel.NO_CONST;
        } else if (args[1].equalsIgnoreCase("no_const_op")) {
            logLevel = LogLevel.NO_CONST_OP;
        } else {
            throw new RuntimeException("Unknown LogLevel type.");
        }

        // included join by default
        // Boolean includeJoin = Boolean.valueOf(args[2]);


        lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            tokens = builder.parse(new File("/Users/cjbaik/dev/NaLIR/src/zfiles/tokens.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Read config for database info
        Database db = new Database(TemplarConfig.getProperty("dbhost"),
                TemplarConfig.getIntegerProperty("dbport"),
                TemplarConfig.getProperty("dbuser"),
                TemplarConfig.getProperty("dbpassword"),
                dbName, fkpkFile, mainAttrsFile, projAttrsFile);

        List<String> sqls = new ArrayList<>();
        try {
            List<String> answerFileLines = FileUtils.readLines(new File(sqlFile), "UTF-8");
            for (String line : answerFileLines) {
                sqls.add(line.trim().split("\t")[0].trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> nlqs = new ArrayList<>();
        try {
            nlqs = FileUtils.readLines(new File(nlqFile), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Integer> shuffleIndexes = new ArrayList<>();
        for (int i = 0; i < nlqs.size(); i++) {
            shuffleIndexes.add(i);
        }
        Collections.shuffle(shuffleIndexes, new Random(Params.RANDOM_SEED));

        // Initialize based on number of folds
        List<List<String>> nlqFolds = new ArrayList<>();
        List<List<String>> sqlFolds = new ArrayList<>();
        List<List<Integer>> shuffleIndexFolds = new ArrayList<>();
        for (int i = 0; i < Params.NUM_FOLDS_CV; i++) {
            nlqFolds.add(new ArrayList<>());
            sqlFolds.add(new ArrayList<>());
            shuffleIndexFolds.add(new ArrayList<>());
        }

        // pass in nlq/sql pairs to each fold
        for (int i = 0; i < shuffleIndexes.size(); i++) {
            int foldIndex = i % Params.NUM_FOLDS_CV;
            nlqFolds.get(foldIndex).add(nlqs.get(shuffleIndexes.get(i)));
            sqlFolds.get(foldIndex).add(sqls.get(shuffleIndexes.get(i)));
            shuffleIndexFolds.get(foldIndex).add(shuffleIndexes.get(i));
        }

        // MAIN LOOP
        for (int i = 0; i < Params.NUM_FOLDS_CV; i++) {
            Log.info("===== FOLD " + i + " =====");
            List<String> curFoldNLQs = nlqFolds.get(i);
            List<String> curFoldSQLs = sqlFolds.get(i);

            LogCountGraph logCountGraph = new LogCountGraph(db, logLevel);

            // Analyze everything in SQLs excluding current
            List<String> sqlLog = new ArrayList<>(sqls);
            sqlLog.removeAll(curFoldSQLs);

            logCountGraph.analyzeSQLs(sqlLog);

            LogGraph logGraph = new LogGraph(db, logCountGraph);

            try {
                RDBMS nalirDB = new RDBMS(dbName);
                for (String nlq : curFoldNLQs) {
                    System.out.println("QUERY " + (nlqs.indexOf(nlq) + 1) + ": " + nlq);
                    try {
                        execute(nalirDB, db, nlq, logGraph);
                        System.out.println(FeedbackGenerator.feedbackGenerate(nalirDB.history, query));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
