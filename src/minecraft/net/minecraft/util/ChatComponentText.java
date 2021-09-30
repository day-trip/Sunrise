package net.minecraft.util;

public class ChatComponentText extends ChatComponentStyle
{
    private final String text;

    public ChatComponentText(String msg)
    {
        text = msg;
    }

    /**
     * Gets the text of this component, without any special formatting codes added, for chat.
     */
    public String getUnformattedTextForChat()
    {
        return text;
    }

    /**
     * Creates a copy of this component.  Almost a deep copy, except the style is shallow-copied.
     */
    public ChatComponentText createCopy()
    {
        ChatComponentText chatcomponenttext = new ChatComponentText(text);
        chatcomponenttext.setChatStyle(getChatStyle().createShallowCopy());

        for (IChatComponent ichatcomponent : getSiblings())
        {
            chatcomponenttext.appendSibling(ichatcomponent.createCopy());
        }

        return chatcomponenttext;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (!(p_equals_1_ instanceof ChatComponentText))
        {
            return false;
        }
        else
        {
            ChatComponentText chatcomponenttext = (ChatComponentText)p_equals_1_;
            return text.equals(chatcomponenttext.getUnformattedTextForChat()) && super.equals(p_equals_1_);
        }
    }

    public String toString()
    {
        return "TextComponent{text='" + text + '\'' + ", siblings=" + siblings + ", style=" + getChatStyle() + '}';
    }
}
