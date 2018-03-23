package karrel.com.btconnector.permission;

import android.content.Context;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

/**
 * Created by Rell on 2018. 3. 23..
 */

public class PermissionChecker implements PermissionCheckable {


    private final Context context;

    public PermissionChecker(Context context) {
        this.context = context;
    }

    @Override
    public void checkPermission(String[] permissions, PermissionListener listener) {
        TedPermission.with(context)
                .setPermissionListener(listener)
                .setRationaleMessage("블루투스를 이용하기위한 권한이 필요합니다.")
                .setPermissions(permissions)
                .check();
    }
}
