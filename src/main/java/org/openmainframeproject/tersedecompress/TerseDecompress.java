package org.openmainframeproject.tersedecompress;

/**
  Copyright Contributors to the TerseDecompress Project.
  SPDX-License-Identifier: Apache-2.0
**/
/*****************************************************************************/
/* Copyright 2018        IBM Corp.                                           */
/*                                                                           */
/*   Licensed under the Apache License, Version 2.0 (the "License");         */
/*   you may not use this file except in compliance with the License.        */
/*   You may obtain a copy of the License at                                 */
/*                                                                           */
/*     http://www.apache.org/licenses/LICENSE-2.0                            */
/*                                                                           */
/*   Unless required by applicable law or agreed to in writing, software     */
/*   distributed under the License is distributed on an "AS IS" BASIS,       */
/*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.*/
/*   See the License for the specific language governing permissions and     */
/*   limitations under the License.                                          */
/*****************************************************************************/
/*                                                                           */
/*  For problems and requirements please create a GitHub issue               */
/*                                                                           */
/*****************************************************************************/
/*                                                                           */
/*  Author: Iain Lewis                          August 2004 (version 3)      */
/*                                                                           */
/*****************************************************************************/
/* Version 4 with editorial changes for publication as open source code      */
/*          Klaus Egeler, Boris Barth  (clientcenter@de.ibm.com)             */
/*****************************************************************************/
/* Version 5: support for variable length binary records                     */
/*          Andrew Rowley, Black Hill Software                               */
/*          Mario Bezzi, Watson Walker                                       */
/*****************************************************************************/

import java.io.*;
import java.util.zip.GZIPOutputStream;

class TerseDecompress {

    private static final String DetailedHelp = new String(
            "Usage: \"TerseDecompress <input file> <output file> [-b]\"\n\n"
           +"Java TerseDecompress will decompress a file compressed using the terse program on z/OS\n"
           +"Default mode is text mode, which will attempt EBCDIC -> ASCII conversion\n"
           +"If no <output file> is provided, it will default to either\n"
           +" 1) if <input file.trs> then <input file>\n"
           +" 2) if <input file> and text mode, then <input file.txt>\n"
           +" 3) if <input file> and binary mode, then <input file.bin>\n"
           +"Options:\n"
           +"-b flag turns on binary mode, no conversion will be attempted\n"
           +"-h or --help prints this message\n"
          );
	
    private static final String Version = new String ("Version 5, May 2024");
    private String inputFileName = null;
    private String outputFileName = null;
    private boolean isHelpRequested = false;
    private boolean textMode = true;
	
	private void printUsageAndExit() {
		System.out.println(DetailedHelp);
		System.out.println(Version);
        System.exit(0);
	}	
	
    private void process (String args[]) throws Exception {
		parseArgs(args);
        
        if (isHelpRequested == true) 
            printUsageAndExit();	
        
    	if (inputFileName == null)
    		printUsageAndExit();

        if (outputFileName == null) {
            if (inputFileName.toLowerCase().endsWith(".trs") )
                outputFileName = inputFileName.substring(0, inputFileName.length() - 4);
            else {
                if (textMode)
                    outputFileName = inputFileName.concat(".txt");
                else
                    outputFileName = inputFileName.concat(".bin");
            }
        }
                
        System.out.println("Attempting to decompress input file (" + inputFileName + ") to output file (" + outputFileName + ")");
	
        TerseDecompresser outputWriter = null;
        try {
            if (outputFileName.endsWith(".gz"))
                outputWriter = TerseDecompresser.create(new FileInputStream(inputFileName), new GZIPOutputStream(new FileOutputStream(outputFileName), 8192, true));
            else 
                outputWriter = TerseDecompresser.create(new FileInputStream(inputFileName), new FileOutputStream(outputFileName)); 

			outputWriter.TextFlag = textMode;
			outputWriter.decode();
		}
        catch( IOException e) {
            System.out.println("Got exception while decompressing input file (" + inputFileName + ").\nError message:\n" + e.toString());
        }
        finally {
            if (outputWriter != null)
                outputWriter.close();
        }

        System.out.println("Processing completed");
    }

    private void parseArgs(String args[]) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h") || args[i].equals("--help")) {
                isHelpRequested = true;
            }
            else if (args[i].equals("-b")) {
                textMode = false;
            }
            // first non-flag argument is the input file name
            else if (inputFileName == null) {
                inputFileName = args[i];
            }
            // second non-flag argument is the output file name
            else if (outputFileName == null) {
                outputFileName = args[i];
            }
            else // we have more args than we know what to do with
            {
                isHelpRequested = true;
                break;
            }
        }
    }
	
    public static void main (String args[]) throws Exception {

        TerseDecompress tersed = new TerseDecompress();
        tersed.process(args);
    }

}
