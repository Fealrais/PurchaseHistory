package com.angelp.purchasehistory;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@HiltAndroidTest
public class PurchaseInstrumentedTest {
    @Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Before
    public void init() {
        hiltRule.inject();
    }

    @Test
    public void happyPath() {

    }

}
