package com.linsh.lshutils.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.Fragment;

import com.linsh.lshutils.utils.Basic.LshApplicationUtils;
import com.linsh.lshutils.utils.Basic.LshIOUtils;
import com.linsh.lshutils.utils.Basic.LshLogUtils;
import com.linsh.lshutils.utils.Basic.LshStringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Senh Linsh on 17/6/7.
 */

public class LshIntentUtils {

    /**
     * 获取分享文本的意图
     */
    public static Intent getShareTextIntent(String content) {
        return new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, content)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「桌面主页」的意图
     */
    public static Intent getHomeIntent() {
        return new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME);
    }

    /**
     * 获取跳转「选择文件」的意图
     */
    public static Intent getPickFileIntent() {
        return getPickIntent("file/*");
    }

    /**
     * 获取跳转「选择文件」的意图
     */
    public static Intent getPickFileIntent(String fileExtension) {
        String type = LshMimeTypeUtils.getMimeTypeFromExtension(fileExtension);
        if (type == null) {
            return null;
        }
        return getPickIntent(type);
    }

    /**
     * 获取跳转「选择图片」的意图
     */
    public static Intent getPickPhotoIntent() {
        return getPickIntent("image/*");
    }

    /**
     * 获取跳转「选择视频」的意图
     */
    public static Intent getPickVideoIntent() {
        return getPickIntent("video/*");
    }

    /**
     * 获取跳转「选择音频」的意图
     */
    public static Intent getPickAudioIntent() {
        return getPickIntent("audio/*");
    }

    /**
     * 获取跳转「选择...」的意图
     */
    public static Intent getPickIntent(String type) {
        return new Intent(Intent.ACTION_GET_CONTENT)
                .setType(type)
                .addCategory(Intent.CATEGORY_OPENABLE);
    }

    /**
     * 获取跳转「系统相机」的意图
     */
    public static Intent getTakePhotoIntent(File outputFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, LshFileProviderUtils.getUriForFile(outputFile));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));
        }
        return intent;
    }

    /**
     * 获取跳转「系统剪裁」的意图
     */
    public static Intent getCropPhotoIntent(File inputFile, File outputFile, int aspectX, int aspectY, int outputX, int outputY) {
        Uri inputUri;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            inputUri = LshFileProviderUtils.getUriForFile(inputFile);
        } else {
            inputUri = Uri.fromFile(inputFile);
        }
        return getCropPhotoIntent(inputUri, Uri.fromFile(outputFile), aspectX, aspectY, outputX, outputY);
    }

    /**
     * 获取跳转「系统剪裁」的意图
     */
    public static Intent getCropPhotoIntent(Uri inputUri, Uri outputUri, int aspectX, int aspectY, int outputX, int outputY) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(inputUri, "image/*");
        intent.putExtra("crop", "true");
        // 指定输出宽高比
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        // 指定输出宽高
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        // 指定输出路径和文件类型
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);
        return intent;
    }

    /**
     * 获取跳转「安装应用」的意图
     */
    public static Intent getInstallAppIntent(File apkFile) {
        if (apkFile == null || !apkFile.exists() || !apkFile.isFile())
            return null;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            uri = LshFileProviderUtils.getUriForFile(apkFile);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(apkFile);
        }
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        return intent;
    }

    /**
     * 获取跳转「卸载应用」的意图
     */
    public static Intent getUninstallAppIntent(String packageName) {
        if (LshStringUtils.isEmpty(packageName)) return null;

        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * 获取跳转「应用」的意图
     */
    public static Intent getLaunchAppIntent(String packageName) {
        return LshContextUtils.getPackageManager().getLaunchIntentForPackage(packageName);
    }


    /**
     * 获取跳转「应用组件」的意图
     */
    public static Intent getComponentIntent(final String packageName, final String className) {
        return new Intent(Intent.ACTION_VIEW)
                .setComponent(new ComponentName(packageName, className))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「拨号界面」的意图
     */
    public static Intent getDialIntent(String phoneNumber) {
        return new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「拨打电话」的意图
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.CALL_PHONE"/>}</p>
     */
    @RequiresPermission(value = Manifest.permission.CALL_PHONE)
    public static Intent getCallIntent(String phoneNumber) {
        return new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNumber))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「发送短信」的意图
     */
    public static Intent getSendSmsIntent(String phoneNumber, String content) {
        return new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber))
                .putExtra("sms_body", content)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「联系人」的意图
     */
    public static Intent getContactsIntent() {
        return new Intent(Intent.ACTION_VIEW)
                .setData(ContactsContract.Contacts.CONTENT_URI);
    }

    /**
     * 获取跳转「联系人详情」的意图
     */
    public static Intent getContactDetailIntent(long contactId, String lookupKey) {
        Uri data = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
        return new Intent(Intent.ACTION_VIEW)
                .setDataAndType(data, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
    }

    /**
     * 获取跳转「设置界面」的意图
     */
    public static Intent getSettingIntent() {
        return new Intent(Settings.ACTION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「应用详情」的意图
     */
    public static Intent getAppDetailsSettingsIntent(String packageName) {
        return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「应用列表」的意图
     */
    public static Intent getAppsIntent() {
        return new Intent(Settings.ACTION_APPLICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「 Wifi 设置」的意图
     */
    public static Intent getWifiSettingIntent() {
        return new Intent(Settings.ACTION_WIFI_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「网络设置」的意图
     */
    public static Intent getWirelessSettingIntent() {
        return new Intent(Settings.ACTION_WIRELESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「无障碍设置」的意图
     */
    public static Intent getAccessibilitySettingIntent() {
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    //================================================ goto ================================================//

    /**
     * 跳转:「安装应用」界面
     */
    public static void gotoInstallApp(File apkFile) {
        startActivity(getInstallAppIntent(apkFile));
    }

    /**
     * 跳转:「卸载应用」界面
     */
    public static void gotoUninstallApp(String packageName) {
        startActivity(getUninstallAppIntent(packageName));
    }

    /**
     * 跳转: 某APP
     */
    public static void gotoApp(String packageName) {
        if (LshStringUtils.isEmpty(packageName))
            return;
        startActivity(getLaunchAppIntent(packageName));
    }

    /**
     * 跳转:「系统桌面」
     */
    public static void gotoHome() {
        startActivity(getHomeIntent());
    }

    /**
     * 跳转:「拨号」界面
     */
    public static void gotoDial(String phoneNumber) {
        startActivity(getDialIntent(phoneNumber));
    }

    /**
     * 「打电话」
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.CALL_PHONE"/>}</p>
     */
    @RequiresPermission(value = Manifest.permission.CALL_PHONE)
    public static void gotoCallPhone(String phoneNumber) {
        startActivity(getCallIntent(phoneNumber));
    }

    /**
     * 跳转:「发送短信」界面
     */
    public static void gotoSendSms(String phoneNumber, String content) {
        startActivity(getSendSmsIntent(phoneNumber, content));
    }

    /**
     * 跳转:「联系人」界面
     */
    public static void gotoContacts() {
        startActivity(getContactsIntent());
    }

    /**
     * 跳转:「联系人详情」界面
     */
    public static void gotoContactDetail(long contactId, String lookupKey) {
        startActivity(getContactDetailIntent(contactId, lookupKey));
    }

    /**
     * 跳转:「系统选择文件」界面
     */
    public static void gotoPickFile(Activity activity, int requestCode) {
        startActivityForResult(activity, null, getPickFileIntent(), requestCode);
    }

    /**
     * 跳转:「系统选择文件」界面
     */
    public static void gotoPickFile(Fragment fragment, int requestCode) {
        startActivityForResult(null, fragment, getPickFileIntent(), requestCode);
    }

    /**
     * 跳转:「系统选择文件」界面
     */
    public static void gotoPickFile(Activity activity, int requestCode, String fileExtension) {
        startActivityForResult(activity, null, getPickFileIntent(fileExtension), requestCode);
    }

    /**
     * 跳转:「系统选择图片」界面
     */
    public static void gotoPickPhoto(Activity activity, int requestCode) {
        startActivityForResult(activity, null, getPickPhotoIntent(), requestCode);
    }

    /**
     * 跳转:「系统选择图片」界面
     */
    public static void gotoPickPhoto(Fragment fragment, int requestCode) {
        startActivityForResult(null, fragment, getPickPhotoIntent(), requestCode);
    }

    /**
     * 跳转:「系统选择视频」界面
     */
    public static void gotoPickVideo(Activity activity, int requestCode) {
        startActivityForResult(activity, null, getPickVideoIntent(), requestCode);
    }

    /**
     * 跳转:「系统选择视频」界面
     */
    public static void gotoPickVideo(Fragment fragment, int requestCode) {
        startActivityForResult(null, fragment, getPickVideoIntent(), requestCode);
    }

    /**
     * 跳转:「系统选择音频」界面
     */
    public static void gotoPickAudio(Activity activity, int requestCode) {
        startActivityForResult(activity, null, getPickAudioIntent(), requestCode);
    }

    /**
     * 跳转:「系统选择音频」界面
     */
    public static void gotoPickAudio(Fragment fragment, int requestCode) {
        startActivityForResult(null, fragment, getPickAudioIntent(), requestCode);
    }

    /**
     * 跳转:「系统拍照」界面
     */
    public static void gotoTakePhoto(Activity activity, int requestCode, File outputFile) {
        startActivityForResult(activity, null, getTakePhotoIntent(outputFile), requestCode);
    }

    /**
     * 跳转:「系统拍照」界面
     */
    public static void gotoTakePhoto(Fragment fragment, int requestCode, File outputFile) {
        startActivityForResult(null, fragment, getTakePhotoIntent(outputFile), requestCode);
    }

    /**
     * 跳转:「系统剪裁」界面
     */
    public static void gotoCropPhoto(Activity activity, int requestCode, File inputFile, File outputFile,
                                     int aspectX, int aspectY, int outputX, int outputY) {
        Intent intent = getCropPhotoIntent(inputFile, outputFile, aspectX, aspectY, outputX, outputY);
        startActivityForResult(activity, null, intent, requestCode);
    }

    /**
     * 跳转:「系统剪裁」界面
     */
    public static void gotoCropPhoto(Fragment fragment, int requestCode, File inputFile, File outputFile,
                                     int aspectX, int aspectY, int outputX, int outputY) {
        Intent intent = getCropPhotoIntent(inputFile, outputFile, aspectX, aspectY, outputX, outputY);
        startActivityForResult(null, fragment, intent, requestCode);
    }

    /**
     * 跳转:「系统剪裁」界面
     */
    public static void gotoCropPhoto(Activity activity, int requestCode, Uri inputUri, Uri outputUri,
                                     int aspectX, int aspectY, int outputX, int outputY) {
        Intent intent = getCropPhotoIntent(inputUri, outputUri, aspectX, aspectY, outputX, outputY);
        startActivityForResult(activity, null, intent, requestCode);
    }

    /**
     * 跳转:「系统剪裁」界面
     */
    public static void gotoCropPhoto(Fragment fragment, int requestCode, Uri inputUri, Uri outputUri,
                                     int aspectX, int aspectY, int outputX, int outputY) {
        Intent intent = getCropPhotoIntent(inputUri, outputUri, aspectX, aspectY, outputX, outputY);
        startActivityForResult(null, fragment, intent, requestCode);
    }

    /**
     * 跳转:「设置」界面
     */
    public static void gotoSetting() {
        LshContextUtils.startActivity(getSettingIntent());
    }

    public static void gotoAppDetailSetting(String packageName) {
        LshContextUtils.startActivity(getAppDetailsSettingsIntent(packageName));
    }

    /**
     * 跳转:「应用程序列表」界面
     */
    public static void gotoAppsSetting() {
        LshContextUtils.startActivity(getAppsIntent());
    }

    /**
     * 跳转:「Wifi列表」设置
     */
    public static void gotoWifiSetting() {
        LshContextUtils.startActivity(getWifiSettingIntent());
    }


    /**
     * 跳转:「飞行模式，无线网和网络设置」界面
     */
    public static void gotoWirelessSetting() {
        LshContextUtils.startActivity(getWirelessSettingIntent());
    }

    /**
     * 跳转:「无障碍设置」界面
     */
    public static void gotoAccessibilitySetting() {
        LshContextUtils.startActivity(getAccessibilitySettingIntent());
    }


    /**
     * 跳转: 「权限设置」界面
     * <p>
     * 根据各大厂商的不同定制而跳转至其权限设置
     * 目前已测试成功机型: 小米V7V8, 华为, 三星, 魅族; 测试失败: OPPO
     *
     * @return 成功跳转权限设置, 返回 true; 没有适配该厂商或不能跳转, 则自动默认跳转设置界面, 并返回 false
     */
    public static boolean gotoPermissionSetting() {
        boolean success = true;
        Intent intent = new Intent();
        String packageName = LshAppUtils.getPackageName();
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        switch (manufacturer) {
            case Manufacturer.HUAWEI:
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity"));
                break;
            case Manufacturer.MEIZU:
                intent.setAction("com.meizu.safe.security.SHOW_APPSEC");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra("packageName", packageName);
                break;
            case Manufacturer.XIAOMI:
                String rom = getMiuiVersion();
                if ("V6".equals(rom) || "V7".equals(rom)) {
                    intent.setAction("miui.intent.action.APP_PERM_EDITOR");
                    intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                    intent.putExtra("extra_pkgname", packageName);
                } else if ("V8".equals(rom)) {
                    intent.setAction("miui.intent.action.APP_PERM_EDITOR");
                    intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                    intent.putExtra("extra_pkgname", packageName);
                } else {
                    gotoAppDetailSetting(packageName);
                }
                break;
            case Manufacturer.SONY:
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity"));
                break;
            case Manufacturer.OPPO:
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.PermissionManagerActivity"));
                break;
            case Manufacturer.LETV:
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps"));
                break;
            case Manufacturer.LG:
                intent.setAction("android.intent.action.MAIN");
                intent.putExtra("packageName", packageName);
                ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
                intent.setComponent(comp);
                break;
            case Manufacturer.SAMSUNG:
            case Manufacturer.SMARTISAN:
                gotoAppDetailSetting(packageName);
                break;
            default:
                intent.setAction(Settings.ACTION_SETTINGS);
                LshLogUtils.i("没有适配该机型, 跳转普通设置界面");
                success = false;
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            LshApplicationUtils.getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            // 跳转失败, 前往普通设置界面
            LshIntentUtils.gotoSetting();
            success = false;
            LshLogUtils.i("无法跳转权限界面, 开始跳转普通设置界面");
        }
        return success;
    }

    private static void startActivity(Intent intent) {
        if (intent != null) {
            LshContextUtils.startActivity(intent);
        }
    }

    private static void startActivity(Activity activity, Fragment fragment, Intent intent) {
        if (intent == null) return;
        if (activity != null) {
            activity.startActivity(intent);
        } else if (fragment != null) {
            fragment.startActivity(intent);
        }
    }

    private static void startActivityForResult(Activity activity, Fragment fragment, Intent intent, int requestCode) {
        if (intent == null) return;
        if (activity != null) {
            activity.startActivityForResult(intent, requestCode);
        } else if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        }
    }

    private static String getMiuiVersion() {
        String propName = "ro.miui.ui.version.name";
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            LshIOUtils.close(input);
        }
        LshLogUtils.i("MiuiVersion = " + line);
        return line;
    }

    private interface Manufacturer {
        String HUAWEI = "huawei";    // 华为
        String MEIZU = "meizu";      // 魅族
        String XIAOMI = "xiaomi";    // 小米
        String SONY = "sony";        // 索尼
        String SAMSUNG = "samsung";  // 三星
        String LETV = "letv";        // 乐视
        String ZTE = "zte";          // 中兴
        String YULONG = "yulong";    // 酷派
        String LENOVO = "lenovo";    // 联想
        String LG = "lg";            // LG
        String OPPO = "oppo";        // oppo
        String VIVO = "vivo";        // vivo
        String SMARTISAN = "smartisan";        // 锤子
    }
}
