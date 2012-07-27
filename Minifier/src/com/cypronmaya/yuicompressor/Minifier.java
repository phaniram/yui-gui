/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cypronmaya.yuicompressor;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import java.io.*;
import java.nio.charset.UnsupportedCharsetException;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

/**
 *
 * @author cypronmaya
 */
public class Minifier {

    // properties with default values
    protected String charset = "UTF-8";
    protected int lineBreakPosition = -1;
    protected boolean munge = false;
    protected boolean warn = true;
    protected boolean preserveAllSemiColons = true;
    // options
    protected boolean optimize = true;
    protected File outDir = null;
    protected boolean useSuffix = true;
    protected boolean useOutDir = false;
    // suffixes
    protected String jsSuffix = "-min.js";
    protected String cssSuffix = "-min.css";
    //log
    private StringBuilder sb = new StringBuilder();
    FilenameFilter js_filefilter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith(".js");
        }
    };
    FilenameFilter css_filefilter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith(".css");
        }
    };
    FilenameFilter js_css_filefilter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith(".js") || name.endsWith(".css");
        }
    };

    public File process(File in) {
        boolean isInFile = in.isFile();
        if (isInFile) {
            String fileName = in.getName();
            File inFile = in.getAbsoluteFile();
            FileType fileType = FileType.getFileType(fileName);
            File outFile = null;
            String newSuffix = (fileType.equals(FileType.JS_FILE)) ? jsSuffix : cssSuffix;
            String newfileName = fileName.replaceFirst(fileType.getSuffix() + "$", newSuffix);
            if (useSuffix) {
                if (useOutDir) {
                    if (outDir.isDirectory()) {
                        outFile = new File(outDir, newfileName);
                    }
                } else {
                    outFile = new File(inFile.getParentFile(), newfileName);
                }
            } else {
                if (useOutDir) {
                    if (outDir.isDirectory()) {
                        outFile = new File(outDir, fileName);
                    }
                } else {
                    outFile = inFile;
                }
            }
            compressFile(inFile, outFile, fileType);
            return outFile;
        }
        return null;
    }

    private void compressFile(File inFile, File outFile, FileType fileType) throws EvaluatorException {
        // do not recompress when outFile is newer
        // always recompress when outFile and inFile are exactly the same file
        if (outFile.isFile() && !inFile.getAbsolutePath().equals(outFile.getAbsolutePath())) {
            if (outFile.lastModified() >= inFile.lastModified()) {
                return;
            }
        }

        try {

            // prepare input file
            Reader in = openFile(inFile);
            // in.close();
            // in = null;
            // Close the input stream first, and then open the output stream,
            // in case the output file should override the input file.

            // prepare output file
            // outFile.getParentFile().mkdirs();
            // Writer out = new OutputStreamWriter(new FileOutputStream(outFile), charset);

            if (fileType.equals(FileType.JS_FILE)) {
                JavaScriptCompressor compressor = createJavaScriptCompressor(in);
                in.close();
                in = null;
                outFile.getParentFile().mkdirs();
                Writer out = new OutputStreamWriter(new FileOutputStream(outFile), charset);


                compressor.compress(out, lineBreakPosition, munge, warn, preserveAllSemiColons, !optimize);
                out.close();
                out = null;
            } else if (fileType.equals(FileType.CSS_FILE)) {
                CssCompressor compressor = new CssCompressor(in);
                in.close();
                in = null;
                outFile.getParentFile().mkdirs();
                Writer out = new OutputStreamWriter(new FileOutputStream(outFile), charset);
                compressor.compress(out, lineBreakPosition);
                out.close();
                out = null;
            }

            // close all streams
        } catch (IOException ioe) {
            log("I/O Error when compressing file" + ioe.toString());
        }
    }

    private JavaScriptCompressor createJavaScriptCompressor(Reader in) throws IOException {
        JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {

            private String getMessage(String source, String message, int line, int lineOffset) {
                String logMessage;
                if (line < 0) {
                    logMessage = (source != null) ? source + ":" : "" + message;
                } else {
                    logMessage = (source != null) ? source + ":" : "" + line + ":" + lineOffset + ":" + message;
                }
                return logMessage;
            }

            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                log(getMessage(sourceName, message, line, lineOffset));
            }

            public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
                log(getMessage(sourceName, message, line, lineOffset));

            }

            public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
                    int lineOffset) {
                log(getMessage(sourceName, message, line, lineOffset));
                return new EvaluatorException(message);
            }
        });
        return compressor;
    }

    private Reader openFile(File file) {
        Reader in = null;
        try {
            in = new InputStreamReader(new FileInputStream(file), charset);
        } catch (UnsupportedCharsetException uche) {
            log("Unsupported charset name: " + charset + uche.toString());
        } catch (IOException ioe) {
            log("I/O Error when reading input file" + ioe.toString());
        }
        return in;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setLineBreakPosition(int lineBreakPosition) {
        this.lineBreakPosition = lineBreakPosition;
    }

    public void setMunge(boolean munge) {
        this.munge = munge;
    }

    public void setWarn(boolean warn) {
        this.warn = warn;
    }

    public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
        this.preserveAllSemiColons = preserveAllSemiColons;
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    public void setJsSuffix(String jsSuffix) {
        this.jsSuffix = jsSuffix;
    }

    public void setCssSuffix(String cssSuffix) {
        this.cssSuffix = cssSuffix;
    }

    public StringBuilder getSb() {
        return sb;
    }

    public void setSb(StringBuilder sb) {
        this.sb = sb;
    }

    public void setOutDir(File outDir) {
        this.outDir = outDir;
    }

    public void setUseOutDir(boolean useOutDir) {
        this.useOutDir = useOutDir;
    }

    public void setUseSuffix(boolean useSuffix) {
        this.useSuffix = useSuffix;
    }

    private void log(String msg) {
        sb.append(msg).append("\n");
     //   System.out.println(msg);
    }

    public static void main(String args[]) {
        Minifier comp = new Minifier();

    }
}
