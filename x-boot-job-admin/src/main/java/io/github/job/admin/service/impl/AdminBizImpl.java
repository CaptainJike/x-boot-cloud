package io.github.job.admin.service.impl;

import io.github.job.admin.scheduler.thread.JobCompleteHelper;
import io.github.job.admin.scheduler.thread.JobRegistryHelper;
import io.github.job.core.biz.AdminBiz;
import io.github.job.core.biz.model.HandleCallbackParam;
import io.github.job.core.biz.model.RegistryParam;
import io.github.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xuxueli 2017-07-27 21:54:20
 */
@Service
public class AdminBizImpl implements AdminBiz {


    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return JobCompleteHelper.getInstance().callback(callbackParamList);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registry(registryParam);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registryRemove(registryParam);
    }

}
