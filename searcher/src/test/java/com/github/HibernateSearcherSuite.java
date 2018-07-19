package com.github;

import com.github.tests.CriteriaTest;
import com.github.tests.EntitySearcherTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CriteriaTest.class,
        EntitySearcherTest.class
})
public class HibernateSearcherSuite {
}
