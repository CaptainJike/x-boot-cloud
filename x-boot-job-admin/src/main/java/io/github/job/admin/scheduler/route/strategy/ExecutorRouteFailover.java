package io.github.job.admin.scheduler.route.strategy;

import io.github.job.admin.scheduler.scheduler.XxlJobScheduler;
import io.github.job.admin.scheduler.route.ExecutorRouter;
import io.github.job.admin.util.I18nUtil;
import io.github.job.core.biz.ExecutorBiz;
import io.github.job.core.biz.model.ReturnT;
import io.github.job.core.biz.model.TriggerParam;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFailover extends ExecutorRouter {

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {

        StringBuffer beatResultSB = new StringBuffer();
        for (String address : addressList) {
            // beat
            ReturnT<String> beatResult = null;
            try {
                ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(address);
                beatResult = executorBiz.beat();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                beatResult = ReturnT.ofFail(e.getMessage() );
            }
            beatResultSB.append( (beatResultSB.length()>0)?"<br><br>":"")
                    .append(I18nUtil.getString("jobconf_beat") + "：")
                    .append("<br>address：").append(address)
                    .append("<br>code：").append(beatResult.getCode())
                    .append("<br>msg：").append(beatResult.getMsg());

            // beat success
            if (beatResult.isSuccess()) {

                beatResult.setMsg(beatResultSB.toString());
                beatResult.setContent(address);
                return beatResult;
            }
        }
        return ReturnT.ofFail( beatResultSB.toString());

    }
}
