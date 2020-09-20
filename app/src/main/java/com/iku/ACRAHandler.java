package com.iku;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraMailSender;
import org.acra.annotation.AcraToast;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.ToastConfigurationBuilder;
import org.acra.data.StringFormat;

@AcraCore(
        buildConfigClass = org.acra.BuildConfig.class,
        logcatArguments = {"-t", "200", "-v", "time"},
        reportFormat = StringFormat.KEY_VALUE_LIST,
        reportContent = {
                ReportField.USER_CRASH_DATE,
                ReportField.APP_VERSION_NAME,
                ReportField.APP_VERSION_CODE,
                ReportField.ANDROID_VERSION,
                ReportField.TOTAL_MEM_SIZE,
                ReportField.BUILD,
                ReportField.DISPLAY,
                ReportField.CRASH_CONFIGURATION,
                ReportField.INITIAL_CONFIGURATION,
                ReportField.PHONE_MODEL,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT})
@AcraMailSender(mailTo = "abhishek@printola.in")
@AcraToast(resText = R.string.acra_toast_text)
public class ACRAHandler extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);
        builder.setBuildConfigClass(BuildConfig.class).setReportFormat(StringFormat.KEY_VALUE_LIST);
        builder.getPluginConfigurationBuilder(ToastConfigurationBuilder.class).setResText(R.string.acra_toast_text);
        ACRA.init(this, builder);
    }
}