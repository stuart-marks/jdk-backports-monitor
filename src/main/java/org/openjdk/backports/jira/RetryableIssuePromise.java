/*
 * Copyright (c) 2018, Red Hat, Inc. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.backports.jira;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.util.concurrent.Promise;

import java.util.concurrent.TimeUnit;

public class RetryableIssuePromise {
    private final IssueRestClient cli;
    private final String key;
    private Promise<Issue> cur;

    public RetryableIssuePromise(IssueRestClient cli, String key) {
        this.cli = cli;
        this.key = key;
        this.cur = cli.getIssue(key);
    }

    public Issue claim() {
        for (int t = 0; t < 10; t++) {
            try {
                return cur.claim();
            } catch (Exception e) {
                try {
                    TimeUnit.MILLISECONDS.sleep((1 + t*t)*100);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                cur = cli.getIssue(key);
            }
        }
        return cur.claim();
    }
}
