/*
 *Copyright 2014 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package es.ehu.si.ixa.pipe.convert;


import ixa.kaflib.KAFDocument;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.xml.sax.SAXException;

/**
 * ixa-pipe-convert.
 * 
 * @author ragerri
 * @version 2014-10-28
 * 
 */

public class CLI {

  /**
   * Get dynamically the version of ixa-pipe-nerc by looking at the MANIFEST
   * file.
   */
  private final static String version = CLI.class.getPackage()
      .getImplementationVersion();

  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

    Namespace parsedArguments = null;

    // create Argument Parser
    ArgumentParser parser = ArgumentParsers.newArgumentParser(
        "ixa-pipe-convert-" + version + ".jar").description(
        "ixa-pipe-convert-" + version + " converts corpora formats.\n");
    
    parser.addArgument("--ancora2treebank").help("Converts ancora constituent parsing annotation into " +
    		"Penn Treebank bracketing format.\n");

    parser.addArgument("--treebank2tokens").help("Converts Penn Treebank into tokenized oneline text.\n");
    
    parser.addArgument("--treebank2WordPos").help("Converts Penn Treebank into Apache OpenNLP POS training format.\n");
    
    parser.addArgument("--normalizePennTreebank").help("Normalizes Penn Treebank removing -NONE- nodes " +
    		"and funcional tags.\n");
   
    parser.addArgument("--filterNameTypes").help("Filter Name Entity types.\n");
    parser.addArgument("--neTypes").help("Choose named entity type to use with the --filterNameTypes option.\n");
    
    parser.addArgument("--printNED").help("Prints named entity string if NED link available in NAF.\n");
    parser.addArgument("--removeEntities").help("Removes the entity NAF layer.\n");
    
    parser.addArgument("--lemmaDict2POSDict").help("Create POSTagger OpenNLP dictionary from " +
    		"lemmatizer dictionary.\n");
    parser.addArgument("--addLemmaDict2POSDict").nargs(2).help("Aggregate a lemmatizer dictionary to a POSTagger OpenNLP " +
    		"dictionary: first input is lemmatizer dictionary and second output the XML dictionary to be expanded.\n");
    
    parser.addArgument("--yelpGetText").help("Extract text attribute from JSON yelp dataset");
 
    parser.addArgument("--nafToCoNLL02").help("Convert NAF to CoNLL02 format.\n");
    parser.addArgument("--nafToCoNLL03").help("Convert NAF to CoNLL03 format.\n");
    
    parser.addArgument("--absaSemEvalATE").help("Convert ABSA SemEval 2014 Aspect Term Extraction to OpenNLP NER annotation.\n");
    parser.addArgument("--absaSemEvalOTE").help("Convert ABSA SemEval 2015 Opinion Target Extraction to OpenNLP NER annotation.\n");
    
    parser.addArgument("--absaSemEvalText")
        .action(Arguments.storeTrue())
        .help("Extract text sentences from ABSA SemEval corpora.\n");
    
    parser.addArgument("--brownClean").help("Remove paragraph if 90% of its characters are not lowercase.\n");
    /*
     * Parse the command line arguments
     */

    // catch errors and print help
    try {
      parsedArguments = parser.parseArgs(args);
      System.err.println("CLI options: " + parsedArguments);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.out
          .println("Run java -jar target/ixa-pipe-converter-" + version + ".jar -help for details");
      System.exit(1);
    }
    
    if (parsedArguments.getString("treebank2WordPos") != null) {
      File inputTree = new File(parsedArguments.getString("treebank2WordPos"));
      Convert converter = new Convert();
      converter.treebank2WordPos(inputTree);
    }
    
    else if (parsedArguments.get("ancora2treebank") != null) { 
      File inputXML = new File(parsedArguments.getString("ancora2treebank"));
      Convert converter = new Convert();
      converter.processAncoraConstituentXMLCorpus(inputXML);
    }
    
    else if (parsedArguments.getString("treebank2tokens") != null) {
      File inputTree = new File(parsedArguments.getString("treebank2tokens"));
      Convert converter = new Convert();
      converter.treebank2tokens(inputTree);
    }
    else if (parsedArguments.get("normalizePennTreebank") != null) {
      File inputTree = new File(parsedArguments.getString("normalizePennTreebank"));
      Convert converter = new Convert();
      converter.getCleanPennTrees(inputTree);
    }
    else if (parsedArguments.get("filterNameTypes") != null) {
      String neTypes = parsedArguments.getString("neTypes");
      String inputFile = parsedArguments.getString("filterNameTypes");
      Convert converter = new Convert();
      converter.filterNameTypes(inputFile, neTypes);
    }
    else if (parsedArguments.getString("printNED") != null) {
      File inputDir = new File(parsedArguments.getString("printNED"));
      Convert converter = new Convert();
      converter.getNEDFromNAF(inputDir);
    }
    else if (parsedArguments.getString("removeEntities") != null) {
      File inputDir = new File(parsedArguments.getString("removeEntities"));
      Convert converter = new Convert();
      converter.removeEntities(inputDir);
    }
    else if (parsedArguments.getString("lemmaDict2POSDict") != null) {
      File inputDir = new File(parsedArguments.getString("lemmaDict2POSTaggerDict"));
      Convert converter = new Convert();
      converter.convertLemmaToPOSDict(inputDir);
    }
    else if (parsedArguments.getList("addLemmaDict2POSDict") != null) {
      List<Object> fileArgs = parsedArguments.getList("addLemmaDict2POSDict");
      File lemmaDict = new File((String) fileArgs.get(0));
      File xmlDict = new File((String) fileArgs.get(1));
      Convert converter = new Convert();
      converter.addLemmaToPOSDict(lemmaDict, xmlDict);
    }
    else if (parsedArguments.get("yelpGetText") != null) {
      String inputFile = parsedArguments.getString("yelpGetText");
      Convert converter = new Convert();
      converter.getYelpText(inputFile);
    }
    else if (parsedArguments.get("nafToCoNLL02") != null) {
      File inputDir = new File(parsedArguments.getString("nafToCoNLL02"));
      Convert converter = new Convert();
      converter.nafToCoNLL02(inputDir);
    }
    else if (parsedArguments.get("nafToCoNLL03") != null) {
      File inputDir = new File(parsedArguments.getString("nafToCoNLL03"));
      Convert converter = new Convert();
      converter.nafToCoNLL03(inputDir);
    }
    else if (parsedArguments.get("absaSemEvalATE") != null) {
      String inputFile = parsedArguments.getString("absaSemEvalATE");
      Convert converter = new Convert();
      converter.absaSemEvalToNER(inputFile);
    }
    else if (parsedArguments.get("absaSemEvalOTE") != null) {
      String inputFile = parsedArguments.getString("absaSemEvalOTE");
      Convert converter = new Convert();
      //converter.absaSemEvalToNER2015(inputFile);
      converter.absaSemEvalToMultiClassNER2015(inputFile);
    }
    else if (parsedArguments.get("absaSemEvalText")) {
      BufferedReader breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
      Convert converter = new Convert();
      converter.absaSemEvalText(breader);
      breader.close();
    }
    else if (parsedArguments.get("brownClean") != null) {
      File inputFile = new File(parsedArguments.getString("brownClean"));
      Convert converter = new Convert();
      converter.brownClusterClean(inputFile);
      
    }
  }
}
