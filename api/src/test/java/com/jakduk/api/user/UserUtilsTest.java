package com.jakduk.api.user;

import com.jakduk.api.ApiApplicationTests;
import com.jakduk.api.common.util.UserUtils;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * Created by pyohwanjang on 2017. 2. 24..
 */
public class UserUtilsTest extends ApiApplicationTests {

    @Resource
    private UserUtils userUtils;

    @Ignore
    @Test
    public void getDaumProfile() {
        userUtils.getDaumProfile("58cffb956dae929e8285d26360b8a59a50c5c6f627f0f6efed6cd2abe43a19cc");
    }

    @Ignore
    @Test
    public void getFacebookProfile() {
        userUtils.getFacebookProfile("EAALwXK7RDAIBAOoadFujSGzUzgetp8kopPF2QfQP3gzKkzacmZBZBVuOadYQAwlMA4cdZByX1mTRLnvpgzEYiR1QxIZCsN6mRbHNd7ZA7YhYWGk51js6S4ddkQAUIzNKqYKZCbjRaRMwsyjiqjQuSsZANluuwMJTpfxASZAhMokRNU6OJcUS6mGJ");
    }
}
