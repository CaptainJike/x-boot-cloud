package io.github.module.sys.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import lombok.experimental.UtilityClass;

/**
 * 密码加密工具类
 * 可以根据自己的业务需要, 修改加密算法(如加盐等)
 */
@UtilityClass
public class PwdUtil {
    public static String encrypt(String str, String salt) {
        if (CharSequenceUtil.isEmpty(str)) {
            return "";
        }

        // 第一步: 2次MD5, 这一步可以放前端完成
//        String step1 = SecureUtil.md5(SecureUtil.md5(str));

        // 第二步: 拼接salt
        String step2 = salt + str + salt;

        // 第三步: SHA256
        return SecureUtil.sha256(step2);
    }

    public static void main(String[] args) {
        String salt = IdUtil.fastSimpleUUID();
        System.out.println(salt);
        String pwd = SecureUtil.md5(SecureUtil.md5("admin"));
        System.out.println(encrypt(pwd, salt));

    }

}
