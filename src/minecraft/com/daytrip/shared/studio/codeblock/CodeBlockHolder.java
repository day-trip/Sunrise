package com.daytrip.shared.studio.codeblock;

import java.util.ArrayList;
import java.util.List;

public class CodeBlockHolder {
    private final List<CodeBlock> codeBlocks = new ArrayList<>();

    public CodeBlockHolder() {

    }

    public void evaluate() {
        for(CodeBlock codeBlock : codeBlocks) {
            codeBlock.evaluate();
        }
    }

    public List<CodeBlock> getCodeBlocks() {
        return codeBlocks;
    }

    public void addCodeBlock(CodeBlock codeBlock) {
        codeBlocks.add(codeBlock);
    }

    @Override
    public String toString() {
        return "CodeBlockHolder{" +
                "codeBlocks=" + codeBlocks +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodeBlockHolder)) return false;

        CodeBlockHolder that = (CodeBlockHolder) o;

        return codeBlocks.equals(that.codeBlocks);
    }

    @Override
    public int hashCode() {
        return codeBlocks.hashCode();
    }
}
