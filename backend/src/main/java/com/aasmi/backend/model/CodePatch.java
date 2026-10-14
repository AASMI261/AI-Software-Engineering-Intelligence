package com.aasmi.backend.model;

public class CodePatch {

    private String filePath;
    private int lineNumber;
    private String oldCode;
    private String newCode;
    private String explanation;

    public CodePatch() {
    }

    public CodePatch(
            String filePath,
            int lineNumber,
            String oldCode,
            String newCode,
            String explanation) {

        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.oldCode = oldCode;
        this.newCode = newCode;
        this.explanation = explanation;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getOldCode() {
        return oldCode;
    }

    public void setOldCode(String oldCode) {
        this.oldCode = oldCode;
    }

    public String getNewCode() {
        return newCode;
    }

    public void setNewCode(String newCode) {
        this.newCode = newCode;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}