package org.secfirst.umbrella;

import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;
import org.secfirst.umbrella.rss.api.Article;
import org.secfirst.umbrella.rss.api.RSSFeedService;

import java.util.ArrayList;

/**
 * Created by dougl on 16/01/2018.
 */
@RunWith(RobolectricTestRunner.class)
public class RSSFeedServiceTest extends InstrumentationTestCase {

    
    private RSSFeedService rssFeedService;

    @Before
    public void setUp() throws Exception {
        rssFeedService = new RSSFeedService();
        ShadowApplication.runBackgroundTasks();
        Robolectric.flushBackgroundThreadScheduler();
        ShadowLooper.runUiThreadTasks();
    }

    @Test
    public void onTaskCompleted() throws Throwable {
        String urlString = "https://www.pcworld.com/index.rss";
        rssFeedService.onFinish(new RSSFeedService.RSSEvent() {
            @Override
            public void onTaskInProgress() {
            }

            @Override
            public void onTaskCompleted(ArrayList<Article> list) {
                Assert.assertTrue(true);
            }

            @Override
            public void onError() {
                Assert.assertTrue(false);
            }
        });
        rssFeedService.execute(urlString);
    }

    @Test
    public void onError() {
        String urlString = "http://google.com";
        rssFeedService.onFinish(new RSSFeedService.RSSEvent() {
            @Override
            public void onTaskInProgress() {
            }

            @Override
            public void onTaskCompleted(ArrayList<Article> list) {
                Assert.assertTrue(false);
            }

            @Override
            public void onError() {
                Assert.assertTrue(true);
            }
        });
        rssFeedService.execute(urlString);
    }

    @Test
    public void onTaskInProgress() {
        String urlString = "https://www.pcworld.com/index.rss";
        rssFeedService.onFinish(new RSSFeedService.RSSEvent() {
            @Override
            public void onTaskInProgress() {
                Assert.assertTrue(true);
            }

            @Override
            public void onTaskCompleted(ArrayList<Article> list) {

            }

            @Override
            public void onError() {
                Assert.assertTrue(false);
            }
        });
        rssFeedService.execute(urlString);
    }

    @Test
    public void invalidRssUrl() {
        String urlString = "dddddddddd";
        rssFeedService.onFinish(new RSSFeedService.RSSEvent() {
            @Override
            public void onTaskInProgress() {
            }

            @Override
            public void onTaskCompleted(ArrayList<Article> list) {
                Assert.assertTrue(false);
            }

            @Override
            public void onError() {
                Assert.assertTrue(true);
            }
        });
        rssFeedService.execute(urlString);
    }
}
