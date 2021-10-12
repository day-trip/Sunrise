package com.daytrip.studio.codeblock;

import com.daytrip.studio.codeblock.impl.CodeBlockLogMessage;

import java.awt.*;

public enum CodeBlockType {
    LOG_MESSAGE_BLOCK(CodeBlockLogMessage.class, "Log Message", Color.BLUE)
    ;

    private static int nextId;

    private final int id;
    private final Class<CodeBlock> type;
    private final String name;
    private final Color color;

    <T extends CodeBlock>CodeBlockType(Class<T> type, String name, Color color) {
        id = getNextId();
        this.type = (Class<CodeBlock>) type;
        this.name = name;
        this.color = color;
    }

    public CodeBlock create(Object... params) {
        try {
            Class<?>[] classParams = new Class[params.length];
            int i = 0;
            for(Object param : params) {
                classParams[i] = param.getClass();
                i++;
            }
            return type.getConstructor(classParams).newInstance(params);
        } catch (Exception e) {
            return null;
        }
    }

    public static CodeBlockType byId(int id) {
        for(CodeBlockType codeBlockType : values()) {
            if(codeBlockType.id == id) {
                return codeBlockType;
            }
        }
        return null;
    }

    private static int getNextId() {
        nextId++;
        return nextId;
    }

    public Class<CodeBlock> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public int getId() {
        return id;
    }
}
