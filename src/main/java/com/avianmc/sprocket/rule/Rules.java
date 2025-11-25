package com.avianmc.sprocket.rule;

import com.avianmc.sprocket.rule.rules.IsAirRule;
import com.avianmc.sprocket.rule.rules.NotAirRule;
import com.avianmc.sprocket.rule.rules.WithSupportRule;

public class Rules {

    public static final Rule NOT_AIR = new NotAirRule();
    public static final Rule IS_AIR = new IsAirRule();

    public static final Rule WITH_SUPPORT = new WithSupportRule();
}
