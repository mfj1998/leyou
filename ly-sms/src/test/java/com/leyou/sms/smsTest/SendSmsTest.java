package com.leyou.sms.smsTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.leyou.common.constants.MQConstants.Exchange.SMS_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.VERIFY_CODE_KEY;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SendSmsTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void testSendSms() throws InterruptedException {
        Map<String,String> map = new HashMap<>();
        map.put("phone","15903873481");
        map.put("code","12345");
        amqpTemplate.convertAndSend(SMS_EXCHANGE_NAME, VERIFY_CODE_KEY, map);
        TimeUnit.SECONDS.sleep(5);
    }


}
