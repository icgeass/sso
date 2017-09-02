
package com.zeroq6.common.counter;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CounterConfigMap {


    public CounterConfigMap(){}

    public CounterConfigMap(List<CounterConfig> counterConfigList){
        if(null != counterConfigList && !counterConfigList.isEmpty()){
            configMap = new HashMap<String, CounterConfig>();
            for(CounterConfig counterConfig : counterConfigList){
                if (StringUtils.isBlank(counterConfig.getType()) || StringUtils.isBlank(counterConfig.getMsgLock()) || StringUtils.isBlank(counterConfig.getMsgTryFailed())) {
                    throw new RuntimeException("计数器type, msgLock, msgTryFailed不能为空, " + JSON.toJSONString(counterConfig));
                }
                if(configMap.containsKey(counterConfig.getType())){
                    throw new RuntimeException("计数器type配置重复, type: " + counterConfig.getType());
                }
                configMap.put(counterConfig.getType(), counterConfig);
            }
        }else{
            throw new RuntimeException("构造传入计数器配置列表不能为空");
        }
    }

    //
    private Map<String, CounterConfig> configMap;

    public Map<String, CounterConfig> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, CounterConfig> configMap) {
        this.configMap = configMap;
    }

    // /// 工具调用


    public int getMaxTimes(String type) {
        return getConfig(type).getMaxTimes();
    }

    public int getLockSeconds(String type) {
        return getConfig(type).getLockSeconds();
    }

    public int getCacheSeconds(String type) {
        return getConfig(type).getCacheSeconds();
    }


    public String getMsgTryFailed(String type) {
        return getConfig(type).getMsgTryFailed();
    }

    public String getMsgLock(String type) {
        return getConfig(type).getMsgLock();
    }

    public String getCacheKeyPrefix(String type) {
        return getConfig(type).getCacheKeyPrefix();
    }

    public String getDatePatternString(String type){
        return getConfig(type).getDatePatternString();
    }

    public int getPriority(String type){
        return getConfig(type).getPriority();
    }




    //////////////////////////////////
    public CounterConfig getConfig(String type){
        return getConfigMap().get(type);
    }

    public void assertType(String type){
        if(StringUtils.isBlank(type)){
            throw new RuntimeException("type不能为空, type: " + type);
        }
        if(null == getConfigMap() || !getConfigMap().containsKey(type)){
            throw new RuntimeException("type未配置, type: " + type);
        }
    }

}
