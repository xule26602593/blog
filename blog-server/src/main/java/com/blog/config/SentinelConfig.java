package com.blog.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.Collections;

@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initRules() {
        // 熔断规则：基于异常比例
        DegradeRule degradeRule = new DegradeRule("aiService")
            .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
            .setCount(0.5)
            .setTimeWindow(10)
            .setMinRequestAmount(5)
            .setStatIntervalMs(1000);

        // 流控规则：QPS 限流
        FlowRule flowRule = new FlowRule("aiService")
            .setGrade(RuleConstant.FLOW_GRADE_QPS)
            .setCount(10);

        DegradeRuleManager.loadRules(Collections.singletonList(degradeRule));
        FlowRuleManager.loadRules(Collections.singletonList(flowRule));
    }
}
