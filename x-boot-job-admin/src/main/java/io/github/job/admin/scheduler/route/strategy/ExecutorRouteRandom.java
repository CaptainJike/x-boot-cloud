package io.github.job.admin.scheduler.route.strategy;

import io.github.job.admin.scheduler.route.ExecutorRouter;
import io.github.job.core.biz.model.ReturnT;
import io.github.job.core.biz.model.TriggerParam;

import java.util.List;
import java.util.Random;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteRandom extends ExecutorRouter {

    private static Random localRandom = new Random();

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        String address = addressList.get(localRandom.nextInt(addressList.size()));
        return ReturnT.ofSuccess(address);
    }

}
