package io.github.job.admin.scheduler.route.strategy;

import io.github.job.admin.scheduler.route.ExecutorRouter;
import io.github.job.core.biz.model.ReturnT;
import io.github.job.core.biz.model.TriggerParam;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFirst extends ExecutorRouter {

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList){
        return ReturnT.ofSuccess(addressList.get(0));
    }

}
