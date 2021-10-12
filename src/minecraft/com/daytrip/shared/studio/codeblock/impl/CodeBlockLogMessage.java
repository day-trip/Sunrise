package com.daytrip.shared.studio.codeblock.impl;

import com.daytrip.shared.math.Vec2;
import com.daytrip.shared.studio.codeblock.CodeBlock;

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
