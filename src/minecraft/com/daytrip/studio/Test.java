package com.daytrip.studio;

import com.daytrip.sunrise.util.math.Vec2;
import com.daytrip.studio.codeblock.CodeBlockType;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class Test {
    public static void main(String[] args) {
        CustomHack customHack = new CustomHack("a custom hack");
        customHack.getCodeBlockHolder().addCodeBlock(CodeBlockType.LOG_MESSAGE_BLOCK.create(new Vec2(0, 0), "hello"));
        customHack.evaluate();
        String str = new JSONSerializer().prettyPrint(true).deepSerialize(customHack);
        System.out.println(str);
        CustomHack c = new JSONDeserializer<CustomHack>().deserialize(str);
        System.out.println(c.toString());
        c.evaluate();
    }
}
