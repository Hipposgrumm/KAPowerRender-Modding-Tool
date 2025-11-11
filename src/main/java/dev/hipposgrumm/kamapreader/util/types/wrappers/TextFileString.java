package dev.hipposgrumm.kamapreader.util.types.wrappers;

public class TextFileString {
    private String name;
    private String contents;

    public TextFileString(String name, String contents) {
        this.name = name;
        this.contents = contents;
    }

    public String getName() {
        return name;
    }

    public String getContents() {
        return contents;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
