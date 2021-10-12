package com.daytrip.studio.codeblock.impl;

import com.daytrip.sunrise.util.math.Vec2;
import com.daytrip.studio.codeblock.CodeBlock;

public class CodeBlockLogMessage extends CodeBlock {
    private final String message;

    public CodeBlockLogMessage() {
        message = "";
    }

    public CodeBlockLogMessage(Vec2 screenPosition, String message) {
        super(screenPosition);
        this.message = message;
    }

    @Override
    public void evaluate() {
        System.out.println(message);
        super.evaluate();
    }

    public String getMessage() {
        return message;
    }
}
