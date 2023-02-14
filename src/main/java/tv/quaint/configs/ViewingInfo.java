package tv.quaint.configs;

import lombok.Getter;
import lombok.Setter;

public class ViewingInfo {
    @Getter @Setter
    private String permission;
    @Getter @Setter
    private String togglePermission;

    public ViewingInfo(String permission, String togglePermission) {
        this.permission = permission;
        this.togglePermission = togglePermission;
    }
}
