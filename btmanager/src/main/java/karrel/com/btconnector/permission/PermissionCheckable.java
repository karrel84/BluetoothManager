package karrel.com.btconnector.permission;

import com.gun0912.tedpermission.PermissionListener;

/**
 * Created by Rell on 2018. 3. 23..
 */

public interface PermissionCheckable {
    // 퍼미션이 승인되었는가에 대한 리턴
    void checkPermission(String[] permissions, PermissionListener listener);
}
