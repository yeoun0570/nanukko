package nanukko.nanukko_back.dto.file;

import lombok.Getter;

@Getter
public enum FileDirectoryType {
    CHAT("chat"),
    PROFILE("profile"),
    SELL("sell");

    private final String directory;

    FileDirectoryType(String directory) {
        this.directory = directory;
    }
}
