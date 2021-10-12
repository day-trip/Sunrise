package com.daytrip.shared.studio;

import com.daytrip.shared.studio.codeblock.CodeBlockHolder;

public class CustomHack {
    private String name;

    private final CodeBlockHolder codeBlockHolder = new CodeBlockHolder();

    public CustomHack() {

    }

    public CustomHack(String name) {
        this.name = name;
    }

    public void evaluate() {
        codeBlockHolder.evaluate();
    }

    public String getName() {
        return name;
    }

    public CodeBlockHolder getCodeBlockHolder() {
        return codeBlockHolder;
    }

    @Override
    public String toString() {
        return "CustomHack{" +
                "name='" + name + '\'' +
                ", codeBlockHolder=" + codeBlockHolder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomHack)) return false;

        CustomHack that = (CustomHack) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return codeBlockHolder.equals(that.codeBlockHolder);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + codeBlockHolder.hashCode();
        return result;
    }
}
