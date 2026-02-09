package io.github.job.admin.scheduler.alarm;

import io.github.job.admin.model.XxlJobInfo;
import io.github.job.admin.model.XxlJobLog;

/**
 * @author xuxueli 2020-01-19
 */
public interface JobAlarm {

    /**
     * job alarm
     *
     * @param info
     * @param jobLog
     * @return
     */
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog);

}
