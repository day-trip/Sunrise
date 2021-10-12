package com.daytrip.studio.codeblock;

import com.daytrip.sunrise.util.math.Vec2;

public class CodeBlock {
    private CodeBlock child;

    private Vec2 screenPosition;

    public CodeBlock() {

    }

    public CodeBlock(Vec2 screenPosition) {
        this.screenPosition = screenPosition;
    }

    public void evaluate() {
        if(child != null) {
            child.evaluate();
        }
    }

    public void setChild(CodeBlock child) {
        this.child = child;
    }

    public CodeBlock getChild() {
        return child;
    }

    public Vec2 getScreenPosition() {
        return screenPosition;
    }

    public void setScreenPosition(Vec2 screenPosition) {
        this.screenPosition = screenPosition;
    }

    @Override
    public String toString() {
        return "CodeBlock{" +
                "child=" + child +
                ", screenPosition=" + screenPosition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodeBlock)) return false;

        CodeBlock codeBlock = (CodeBlock) o;

        if (child != null ? !child.equals(codeBlock.child) : codeBlock.child != null) return false;
        return screenPosition != null ? screenPosition.equals(codeBlock.screenPosition) : codeBlock.screenPosition == null;
    }

    @Override
    public int hashCode() {
        int result = child != null ? child.hashCode() : 0;
        result = 31 * result + (screenPosition != null ? screenPosition.hashCode() : 0);
        return result;
    }
}
